package com.example.auction.model

@JvmInline
value class BidId(val value: Long) : Comparable<BidId> {
    override fun toString() = value.toString()
    override fun compareTo(other: BidId) = value.compareTo(other.value)
    
    companion object {
        val NONE = BidId(0)
    }
}