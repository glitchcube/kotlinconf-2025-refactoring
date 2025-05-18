package com.example.auction.acceptance

import com.example.auction.service.Sold
import kotlin.test.Test
import kotlin.test.assertEquals


interface BlindAuction_BiddingAndWinning_Tests : AuctionTesting {
    @Test
    fun `highest bidder wins`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createBlindAuction("A genuine Vermeer", 100.EUR)
        
        bob.bid(auction, 100.EUR)
        brenda.bid(auction, 200.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(brenda, 200.EUR))
    }
    
    @Test
    fun `first winning bid wins when multiple winning bids`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createBlindAuction("A genuine Canaletto", 100.EUR)
        
        bob.bid(auction, 200.EUR)
        brenda.bid(auction, 200.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(bob, 200.EUR))
    }
    
    @Test
    fun `only bidder's last bid is considered`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createBlindAuction("A genuine Vettriano", 100.EUR)
        
        bob.bid(auction, 300.EUR)
        brenda.bid(auction, 200.EUR)
        bob.bid(auction, 100.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(brenda, 200.EUR))
    }
}

