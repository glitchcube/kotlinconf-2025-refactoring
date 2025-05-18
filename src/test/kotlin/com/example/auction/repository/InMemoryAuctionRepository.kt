package com.example.auction.repository

import com.example.auction.model.Auction
import com.example.auction.model.AuctionId
import com.example.auction.model.AuctionId.Companion.NONE
import com.example.auction.model.AuctionState
import com.example.auction.model.AuctionState.closed
import com.example.auction.model.AuctionState.open
import com.example.auction.model.BidId
import kotlin.test.assertEquals


class InMemoryAuctionRepository : AuctionRepository {
    private val auctions = mutableMapOf<AuctionId,Auction>()
    private var nextBidId = 1L
    
    override fun addAuction(auction: Auction): Auction {
        assertEquals(NONE, auction.id, "auction already has an ID")
        val saved = auction.copy(id = AuctionId(auctions.size + 1L))
        auctions[saved.id] = saved
        return saved
    }
    
    override fun getAuction(id: AuctionId) =
        auctions[id]
    
    override fun updateAuction(auction: Auction): Auction {
        val updated = auction.copy(bids = auction.bids.map {
            if (it.id == BidId.NONE) {
                it.copy(id = BidId(nextBidId++))
            } else it
        })
        auctions[updated.id] = updated
        return updated
    }
    
    override fun listOpenAuctions(count: Int, after: AuctionId) =
        listAuctions(open, count, after)

    override fun listForSettlement(count: Int, after: AuctionId) =
        listAuctions(closed, count, after).map { it.id }

    private fun listAuctions(state: AuctionState, count: Int, after: AuctionId): List<Auction> =
        auctions.values
            .filter { it.id > after && it.state == state }
            .take(count)
}


class InMemoryAuctionRepositoryTest : AuctionRepositoryContract {
    override val repository = InMemoryAuctionRepository()
}
