package com.example.auction.service

import com.example.auction.model.AuctionId
import com.example.auction.repository.AuctionRepository
import com.example.settlement.Settlement
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
        var lastAuctionId = settleAuctions(batchSize)
        while (lastAuctionId != null) {
            lastAuctionId = settleAuctions(batchSize, after = lastAuctionId)
        }
    }

    fun settleAuctions(batchSize: Int, after: AuctionId = AuctionId.NONE): AuctionId? {
        val batch = repository.listForSettlement(batchSize, after = after)

        for (id in batch) {
            val auction = repository.getAuction(id) ?: continue
            val instruction = auction.settlement() ?: continue

            settlement.settle(instruction)
            repository.updateAuction(auction.settled())
        }

        return batch.lastOrNull()
    }
}

