package com.example.auction.model

import com.example.pii.UserId

class Bid(
    var buyer: UserId,
    var amount: MonetaryAmount,
    
    var id: BidId = BidId.NONE
) {
    override fun toString(): String {
        return "Bid(buyer=$buyer, amount=$amount, id=$id)"
    }
}

