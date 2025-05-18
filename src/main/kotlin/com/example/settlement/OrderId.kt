package com.example.settlement

import com.example.auction.model.AuctionId

@JvmInline
value class OrderId(val value: String) : Comparable<OrderId> {
    constructor(auctionId: AuctionId) : this("auction:$auctionId")
    
    override fun toString() = value
    override fun compareTo(other: OrderId) = value.compareTo(other.value)
}
