package com.example.auction.acceptance

import com.example.auction.Money
import com.example.auction.model.AuctionId
import com.example.auction.model.MonetaryAmount
import com.example.auction.model.Money
import com.example.auction.percent
import com.example.auction.service.AuctionResult
import com.example.auction.service.AuctionSummary
import com.example.auction.service.CreateAuctionRequest
import com.example.auction.service.CreateBlindAuctionRequest
import com.example.auction.service.CreateReverseAuctionRequest
import com.example.auction.service.CreateVickreyAuctionRequest
import com.example.pii.UserId
import com.example.settlement.SettlementInstruction
import java.util.Currency


val Int.EUR get() = Money(this, Currency.getInstance("EUR")).toWholeMinorUnits()
val Int.GBP get() = Money(this, Currency.getInstance("GBP")).toWholeMinorUnits()

val String.EUR get() = Money(this, Currency.getInstance("EUR")).toWholeMinorUnits()
val String.GBP get() = Money(this, Currency.getInstance("EUR")).toWholeMinorUnits()

interface AuctionTesting {
    fun aUser(name: String): UserId
    
    fun UserId.createBlindAuction(
        description: String,
        reserve: Money,
        commission: MonetaryAmount = 10.percent
    ): AuctionId =
        createAuction(CreateBlindAuctionRequest(this, description, reserve, commission))
    
    fun UserId.createVickreyAuction(
        description: String,
        reserve: Money,
        commission: MonetaryAmount = 10.percent
    ): AuctionId =
        createAuction(CreateVickreyAuctionRequest(this, description, reserve, commission))
    
    fun UserId.createReverseAuction(
        description: String,
        reserve: Money = 0.EUR,
        chargePerBid: Money
    ) = createAuction(
        CreateReverseAuctionRequest(
            seller = this,
            description = description,
            reserve = reserve,
            chargePerBid = chargePerBid
        )
    )
    
    fun createAuction(rq: CreateAuctionRequest): AuctionId
    
    fun UserId.bid(auction: AuctionId, amount: Money)
    
    fun UserId.closeAuction(auction: AuctionId): AuctionResult
    
    fun UserId.listAuctions(count: Int? = null, after: AuctionId? = null): List<AuctionSummary>
    fun settlementOf(auction: AuctionId): SettlementInstruction
}
