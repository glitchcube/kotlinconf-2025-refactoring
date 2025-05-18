package com.example.auction.service

import com.example.auction.model.AuctionId
import com.example.auction.model.AuctionRules
import com.example.auction.model.MonetaryAmount
import com.example.auction.model.Money
import com.example.pii.UserId
import java.util.Currency

data class AuctionSummary(
    val id: AuctionId,
    val rules: AuctionRules,
    val seller: UserId,
    val description: String,
    val currency: Currency,
    val reserve: Money,
    val commission: MonetaryAmount,
    val chargePerBid: Money
)

