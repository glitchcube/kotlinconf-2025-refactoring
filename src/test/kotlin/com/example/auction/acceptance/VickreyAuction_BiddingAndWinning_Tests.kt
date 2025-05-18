package com.example.auction.acceptance

import com.example.auction.service.Sold
import kotlin.test.Test
import kotlin.test.assertEquals


interface VickreyAuction_BiddingAndWinning_Tests : AuctionTesting {
    @Test
    fun `highest bidder wins and pays second highest price`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        val barry = aUser("barry")
        
        val auction = sandra.createVickreyAuction("An 1840 Penny Black", 3000.EUR)
        
        barry.bid(auction, 3300.EUR)
        bob.bid(auction, 3100.EUR)
        brenda.bid(auction, 3200.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(barry, 3200.EUR))
    }
    
    @Test
    fun `sole bidder wins by default and pays their bid`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        
        val auction = sandra.createVickreyAuction("British Guiana One Cent Magenta", 3000.EUR)
        
        bob.bid(auction, 3100.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(bob, 3100.EUR))
    }
    
    @Test
    fun `first winning bid wins when multiple winning bids and pays amount of second place bid which is also the highest price`() {
        val sandra = aUser("sandra")
        
        val beccy = aUser("beccy")
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        val barry = aUser("barry")
        
        val auction = sandra.createVickreyAuction("A Twopenny Blue", 3000.EUR)
        
        beccy.bid(auction, 3100.EUR)
        brenda.bid(auction, 3300.EUR)
        bob.bid(auction, 3200.EUR)
        barry.bid(auction, 3300.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(brenda, 3300.EUR))
    }
    
    @Test
    fun `only bidder's last bid is considered`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createVickreyAuction("An Inverted Jenny", 100.EUR)
        
        bob.bid(auction, 300.EUR)
        brenda.bid(auction, 200.EUR)
        bob.bid(auction, 100.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(actual = result, expected = Sold(brenda, 100.EUR))
    }
}

