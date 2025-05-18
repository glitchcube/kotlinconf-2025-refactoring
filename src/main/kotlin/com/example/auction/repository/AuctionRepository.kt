package com.example.auction.repository

import com.example.auction.model.Auction
import com.example.auction.model.AuctionId

interface AuctionRepository {
    fun getAuction(id: AuctionId): Auction?

    fun addAuction(auction: Auction)
    fun updateAuction(auction: Auction)

    fun listOpenAuctions(count: Int, after: AuctionId = AuctionId.NONE): List<Auction>
    fun listForSettlement(count: Int, after: AuctionId = AuctionId.NONE): List<AuctionId>
}
