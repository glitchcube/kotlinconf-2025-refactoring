package com.example.auction.service

import com.example.auction.model.AuctionId
import com.example.auction.repository.AuctionRepository
import com.example.settlement.Settlement
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.Success
import dev.forkhandles.result4k.onFailure
import dev.forkhandles.result4k.orThrow
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class AuctionSettlementService(
    private val repository: AuctionRepository,
    private val settlement: Settlement,
    @Value("\${auction.settlement.batch-size:100}")
    val batchSize: Int = 100
) {
    @Scheduled(fixedDelayString = "\${auction.settlement.period}")
    fun requestSettlements() {
        var lastAuctionId = settleAuctions(batchSize).orThrow()
        while (lastAuctionId != null) {
            lastAuctionId = settleAuctions(batchSize, after = lastAuctionId).orThrow()
        }
    }

    private fun settleAuctions(batchSize: Int, after: AuctionId = AuctionId.NONE): Result4k<AuctionId?, Exception> {
        val batch = repository.listForSettlement(batchSize, after = after)
        for (id in batch) {
            val auction = repository.getAuction(id) ?: continue
            val instruction = auction.settlement() ?: continue

            settlement.settle(instruction)
            val updated = auction.settled().onFailure { return it }
            repository.updateAuction(updated)
        }
        return Success(batch.lastOrNull())
    }
}

