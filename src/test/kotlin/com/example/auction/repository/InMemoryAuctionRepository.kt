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
    
    override fun addAuction(auction: Auction) {
        assertEquals(NONE, auction.id, "auction already has an ID")
        auction.id = AuctionId(auctions.size + 1L)
        auctions[auction.id] = auction
    }
    
    override fun getAuction(id: AuctionId) =
        auctions[id]
    
    override fun updateAuction(auction: Auction) {
        auction.bids.forEach {
            if (it.id == BidId.NONE) {
                it.id = BidId(nextBidId++)
            }
        }
        
        // TODO: ask Nat if I can delete this line. It doesn't seem needed because we mutate the auction
        // auctions[auction.id] = auction
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
