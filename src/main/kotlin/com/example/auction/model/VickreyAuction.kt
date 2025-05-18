package com.example.auction.model

import com.example.auction.model.AuctionRules.Vickrey
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

class VickreyAuction(
    override var seller: UserId,
    override var description: String,
    override var currency: Currency,
    override var reserve: MonetaryAmount,
    override var commission: MonetaryAmount = MonetaryAmount.ZERO,
    override var chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    override var id: AuctionId = AuctionId.NONE,
    override var bids: MutableList<Bid> = mutableListOf(),
    override var state: AuctionState = open,
    override var winner: AuctionWinner? = null
) : Auction() {
    override val rules = Vickrey
    
    override fun decideWinner(): AuctionWinner? {
        return bids
            .associateBy { it.buyer }
            .values
            .sortedByDescending { it.amount }
            .take(2)
            .run {
                when {
                    isEmpty() -> null
                    last().amount < reserve -> null
                    else -> AuctionWinner(first().buyer, last().amount)
                }
            }
    }
}