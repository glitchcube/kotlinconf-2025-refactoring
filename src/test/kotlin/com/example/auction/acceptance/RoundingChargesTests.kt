package com.example.auction.acceptance

import com.example.auction.EUR
import com.example.auction.Money
import com.example.auction.model.MonetaryAmount
import kotlin.test.Test
import kotlin.test.assertEquals


interface RoundingChargesTests : AuctionTesting {
    @Test
    fun `rounds commission down`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        
        val auction = sandra.createBlindAuction(
            description = "Windsor Castle",
            reserve = 10.EUR,
            commission = MonetaryAmount("0.1")
        )
        
        bob.bid(auction, "99.99".EUR)
        
        sandra.closeAuction(auction)
        
        val settlement = settlementOf(auction)
        
        assertEquals(
            MonetaryAmount("9.99"),
            settlement.charges.single().total.amount
        )
    }
    
    @Test
    fun `rounds bidding charges up`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        
        val a = sandra.createReverseAuction(
            description = "Blenheim Palace",
            chargePerBid = Money("0.125", EUR)
        )
        
        bob.bid(a, 1.EUR)
        bob.bid(a, 2.EUR)
        bob.bid(a, 3.EUR)
        
        sandra.closeAuction(a)
        
        val s = settlementOf(a)
        
        assertEquals(MonetaryAmount("0.38"), s.charges.single().total.amount)
    }
}

