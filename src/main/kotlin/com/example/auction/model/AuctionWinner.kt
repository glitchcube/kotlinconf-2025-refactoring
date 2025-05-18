package com.example.auction.model

import com.example.pii.UserId

data class AuctionWinner(
    val winner: UserId,
    val owed: MonetaryAmount
)

fun Bid.toWinner() = AuctionWinner(buyer, amount)
