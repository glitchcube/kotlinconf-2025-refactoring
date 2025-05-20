package com.example.auction.model

import com.example.auction.model.AuctionRules.Blind
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.Currency

fun BlindAuction(
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
) = Auction(
    rules = Blind,
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


fun blindAuctionWinner(auction: Auction) =
    auction.bids
        .associateBy { it.buyer }
        .values
        .maxByOrNull { it.amount }
        ?.takeIf { it.amount >= auction.reserve }?.toWinner()
