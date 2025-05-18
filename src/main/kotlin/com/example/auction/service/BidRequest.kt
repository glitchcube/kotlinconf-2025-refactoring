package com.example.auction.service

import com.example.auction.model.Money
import com.example.pii.UserId

data class BidRequest(
    val buyer: UserId,
    val amount: Money
)