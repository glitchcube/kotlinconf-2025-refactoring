package com.example.auction.service

import com.example.auction.model.AuctionError
import com.example.auction.model.AuctionId
import com.example.auction.model.BadRequestException
import com.example.auction.model.toException
import dev.forkhandles.result4k.Result4k

sealed class AuctionServiceError {
    abstract val message: String
    abstract fun toException(): Exception
    class InvalidUser(override val message: String ): AuctionServiceError() {
        override fun toException() = BadRequestException(message)
    }
    class InvalidAuction(override val message: String ): AuctionServiceError() {
        override fun toException() = NotFoundException(message)
    }
    class AuctionError(val error: com.example.auction.model.AuctionError): AuctionServiceError() {
        override val message: String
            get() = error.message
        override fun toException() = error.toException()
    }
}

interface AuctionService {
    fun listAuctions(count: Int, after: AuctionId): List<AuctionSummary>
    fun createAuction(rq: CreateAuctionRequest): AuctionId
    fun placeBid(auctionId: AuctionId, bid: BidRequest): Result4k<Unit, AuctionServiceError>
    fun closeAuction(auctionId: AuctionId): AuctionResult
}