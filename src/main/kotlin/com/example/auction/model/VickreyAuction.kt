package com.example.auction.model

import com.example.auction.model.AuctionRules.Vickrey
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

open class VickreyAuction(
    seller: UserId,
    description: String,
    currency: Currency,
    reserve: MonetaryAmount,
    commission: MonetaryAmount = MonetaryAmount.ZERO,
    chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    id: AuctionId = AuctionId.NONE,
    bids: MutableList<Bid> = mutableListOf(),
    state: AuctionState = open,
    winner: AuctionWinner? = null
) : Auction(
    rules = Vickrey,
    seller = seller,
    description = description,
    currency = currency,
    reserve = reserve,
    commission = commission,
    chargePerBid = chargePerBid,
    id = id,
    state = state,
    bids = bids,
    winner = winner
)

fun vickreyAuctionWinner(auction: Auction): AuctionWinner? {
    return auction.bids
        .associateBy { it.buyer }
        .values
        .sortedByDescending { it.amount }
        .take(2)
        .run {
            when {
                isEmpty() -> null
                last().amount < auction.reserve -> null
                else -> AuctionWinner(first().buyer, last().amount)
            }
        }
}
