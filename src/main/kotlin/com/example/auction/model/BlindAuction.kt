package com.example.auction.model

import com.example.auction.model.AuctionRules.Blind
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

class BlindAuction(
    override val seller: UserId,
    override val description: String,
    override val currency: Currency,
    override val reserve: MonetaryAmount,
    override val commission: MonetaryAmount = MonetaryAmount.ZERO,
    override val chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    override var id: AuctionId = AuctionId.NONE,
    override var bids: MutableList<Bid> = mutableListOf(),
    override var state: AuctionState = open,
    override var winner: AuctionWinner? = null
) : Auction() {
    override val rules = Blind
    
    override fun decideWinner(): AuctionWinner? = rules.decideWinner(this)
}
