package com.example.auction.model

import com.example.pii.UserId

data class Bid(
    val buyer: UserId,
    val amount: MonetaryAmount,
    val id: BidId = BidId.NONE
)
