package com.example.auction.acceptance.mem

import com.example.auction.acceptance.AuctionTesting
import com.example.auction.model.AuctionId
import com.example.auction.model.Money
import com.example.auction.repository.InMemoryAuctionRepository
import com.example.auction.service.AuctionServiceImpl
import com.example.auction.service.AuctionSettlementService
import com.example.auction.service.AuctionSummary
import com.example.auction.service.BidRequest
import com.example.auction.service.CreateAuctionRequest
import com.example.pii.UserId
import com.example.pii.UserIdValidator
import com.example.settlement.SettlementInstruction
import com.example.simulators.pii_vault.PiiVaultSimulatorService
import com.example.simulators.settlement.SettlementSimulatorService
import com.example.simulators.settlement.get
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.orThrow


abstract class DomainModelOnlyTesting : AuctionTesting {
    private val piiVault = PiiVaultSimulatorService()
    private val userIdValidator = object : UserIdValidator {
        override fun isValid(userId: UserId): Boolean {
            return piiVault.isValid(userId)
        }
    }
    
    private val settlementSimulatorService = SettlementSimulatorService()
    
    private val repository = InMemoryAuctionRepository()
    private val service = AuctionServiceImpl(repository, userIdValidator)
    private val settlementTask = AuctionSettlementService(
        repository = repository,
        batchSize = 1,
        settlement = settlementSimulatorService)
    
    override fun aUser(name: String): UserId {
        val userId = UserId(name)
        piiVault.addUserId(userId)
        return userId
    }
    
    override fun createAuction(rq: CreateAuctionRequest): AuctionId =
        service.createAuction(rq)
    
    override fun UserId.bid(auction: AuctionId, amount: Money) {
        service.placeBid(auction, BidRequest(this, amount)).mapFailure { it.toException() }.orThrow()
    }
    
    override fun UserId.closeAuction(auction: AuctionId) =
        service.closeAuction(auction)
    
    override fun UserId.listAuctions(count: Int?, after: AuctionId?): List<AuctionSummary> =
        service.listAuctions(count ?: 25, after ?: AuctionId.NONE)
    
    override fun settlementOf(auction: AuctionId): SettlementInstruction {
        settlementTask.requestSettlements()
        return settlementSimulatorService[auction]
    }
}


