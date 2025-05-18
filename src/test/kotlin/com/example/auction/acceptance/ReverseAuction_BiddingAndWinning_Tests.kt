package com.example.auction.acceptance

import com.example.auction.service.Sold
import kotlin.test.Test
import kotlin.test.assertEquals

interface ReverseAuction_BiddingAndWinning_Tests : AuctionTesting {
    @Test
    fun `bidder who bid lowest unique price wins and pays their bid`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        val barry = aUser("barry")
        val wendy = aUser("wendy")
        
        val auction = sandra.createReverseAuction(
            description = "1955 Mercedes-Benz 300 SLR",
            chargePerBid = 2.EUR
        )
        
        barry.bid(auction, 300.EUR)
        bob.bid(auction, 300.EUR)
        bob.bid(auction, 100.EUR)
        wendy.bid(auction, 200.EUR)
        wendy.bid(auction, 100.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(wendy, 200.EUR))
    }
    
    @Test
    fun `sole bidder wins by default and pays their bid`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        
        val auction = sandra.createReverseAuction(
            description = "1957 Ferrari 335 S",
            chargePerBid = 2.EUR
        )
        
        bob.bid(auction, 100.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(bob, 100.EUR))
    }
}

