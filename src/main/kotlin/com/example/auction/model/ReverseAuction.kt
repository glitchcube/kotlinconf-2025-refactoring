package com.example.auction.model

import com.example.auction.model.AuctionRules.Reverse
import com.example.auction.model.AuctionState.open
import com.example.pii.UserId
import java.util.*

class ReverseAuction(
    seller: UserId,
    description: String,
    currency: Currency,
    reserve: MonetaryAmount = MonetaryAmount.ZERO,
    commission: MonetaryAmount = MonetaryAmount.ZERO,
    chargePerBid: MonetaryAmount = MonetaryAmount.ZERO,
    id: AuctionId = AuctionId.NONE,
    bids: MutableList<Bid> = mutableListOf(),
    state: AuctionState = open,
    winner: AuctionWinner? = null,
) : Auction(
    seller = seller,
    description = description,
    currency = currency,
    reserve = reserve,
    commission = commission,
    chargePerBid = chargePerBid,
    id = id,
    bids = bids,
    state = state,
    winner = winner,
    rules = Reverse
)
