package com.example.auction.model

@JvmInline
value class AuctionId(val value: Long) : Comparable<AuctionId> {
    override fun toString() = value.toString()
    override fun compareTo(other: AuctionId) = value.compareTo(other.value)
    
    companion object {
        val NONE = AuctionId(0)
    }
}