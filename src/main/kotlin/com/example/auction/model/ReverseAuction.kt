package com.example.auction.model

import com.example.auction.model.AuctionRules.Reverse
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

fun ReverseAuction(
    seller: UserId,
    description: String,
    currency: Currency,
    reserve: MonetaryAmount = MonetaryAmount.ZERO,
    commission: MonetaryAmount = MonetaryAmount.ZERO,
    chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    id: AuctionId = AuctionId.NONE,
    bids: MutableList<Bid> = mutableListOf(),
    state: AuctionState = open,
    winner: AuctionWinner? = null
) = Auction(
    rules = Reverse,
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

fun reverseAuctionWinner(auction: Auction): AuctionWinner? {
    return auction.bids
        .filter { it.amount >= auction.reserve }
        .groupBy { it.amount }
        .values
        .mapNotNull { it.singleOrNull() }
        .minByOrNull { it.amount }
        ?.toWinner()
}
