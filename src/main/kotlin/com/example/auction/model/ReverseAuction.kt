package com.example.auction.model

import com.example.auction.model.AuctionRules.Reverse
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

class ReverseAuction(
    override val seller: UserId,
    override val description: String,
    override val currency: Currency,
    override val reserve: MonetaryAmount = MonetaryAmount.ZERO,
    override val commission: MonetaryAmount = MonetaryAmount.ZERO,
    override val chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    override var id: AuctionId = AuctionId.NONE,
    override val bids: MutableList<Bid> = mutableListOf(),
    override var state: AuctionState = open,
    override var winner: AuctionWinner? = null
) : Auction() {
    override val rules = Reverse
    
    override fun decideWinner(): AuctionWinner? {
        return bids
            .filter { it.amount >= reserve }
            .groupBy { it.amount }
            .values
            .mapNotNull { it.singleOrNull() }
            .minByOrNull { it.amount }
            ?.toWinner()
    }
}
