package com.example.auction.model

import com.example.auction.model.AuctionRules.Reverse
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

class ReverseAuction(
    override var seller: UserId,
    override var description: String,
    override var currency: Currency,
    override var reserve: MonetaryAmount = MonetaryAmount.ZERO,
    override var commission: MonetaryAmount = MonetaryAmount.ZERO,
    override var chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    override var id: AuctionId = AuctionId.NONE,
    override var bids: MutableList<Bid> = mutableListOf(),
    override var state: AuctionState = open,
    override var winner: AuctionWinner? = null
) : Auction() {
    override val rules = Reverse
    
    override fun decideWinner(): AuctionWinner? {
        val bidsByAmount = mutableMapOf<MonetaryAmount, MutableList<Bid>>()
        for (bid in bids) if (bid.amount >= reserve) {
            var bidGroup = bidsByAmount[bid.amount]
            if (bidGroup == null) {
                bidGroup = mutableListOf()
                bidsByAmount[bid.amount] = bidGroup
            }
            bidGroup.add(bid)
        }
        
        var lowestUniqueBid: Bid? = null
        
        for (bids in bidsByAmount.values) if (bids.size == 1) {
            val bid = bids.single()
            if (lowestUniqueBid == null || bid.amount < lowestUniqueBid.amount) {
                lowestUniqueBid = bid
            }
        }
        
        return lowestUniqueBid?.toWinner()
    }
}
