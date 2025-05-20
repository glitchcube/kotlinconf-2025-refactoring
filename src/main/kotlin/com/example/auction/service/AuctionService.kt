package com.example.auction.service

import com.example.auction.model.Auction
import com.example.auction.model.AuctionId
import dev.forkhandles.result4k.Result4k

interface AuctionService {
    fun listAuctions(count: Int, after: AuctionId): List<AuctionSummary>
    fun createAuction(rq: CreateAuctionRequest): AuctionId
    fun placeBid(auctionId: AuctionId, bid: BidRequest): Result4k<Auction, Exception>
    fun closeAuction(auctionId: AuctionId): AuctionResult
}