package com.example.auction.service

import com.example.auction.model.AuctionId

interface AuctionService {
    fun listAuctions(count: Int, after: AuctionId): List<AuctionSummary>
    fun createAuction(rq: CreateAuctionRequest): AuctionId
    fun placeBid(auctionId: AuctionId, bid: BidRequest)
    fun closeAuction(auctionId: AuctionId): AuctionResult
}