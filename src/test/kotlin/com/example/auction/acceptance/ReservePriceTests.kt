package com.example.auction.acceptance

import com.example.auction.service.Passed
import com.example.auction.service.Sold
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

interface ReservePriceTests : AuctionTesting {
    @Test
    fun `blind auction is passed if no bids meet reserve price`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createBlindAuction(
            description = "The Holy Grail",
            reserve = 100.EUR
        )
        
        bob.bid(auction, 90.EUR)
        brenda.bid(auction, 99.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(Passed, result)
    }
    
    @Test
    fun `vickrey auction is passed if no bids meet reserve price`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createVickreyAuction(
            description = "The Spear of Longinus",
            reserve = 100.EUR
        )
        
        bob.bid(auction, 90.EUR)
        brenda.bid(auction, 99.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(Passed, result)
    }
    
    @Test
    fun `vickrey auction is passed if second-highest bid is below reserve price`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        
        val auction = sandra.createVickreyAuction(
            description = "The Veil of Veronica",
            reserve = 100.EUR
        )
        
        bob.bid(auction, 101.EUR)
        brenda.bid(auction, 99.EUR)
        
        val result = sandra.closeAuction(auction)
        
        assertEquals(Passed, result)
    }
    
    @Test
    fun `reverse auction ignores bids below reserve price`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        val belinda = aUser("belinda")
        
        val auction = sandra.createReverseAuction(
            description = "The Sudarium of Oviedo",
            chargePerBid = 2.EUR,
            reserve = 50.EUR)
        
        bob.bid(auction, 40.EUR)
        belinda.bid(auction, 60.EUR)
        
        val result = sandra.closeAuction(auction)
        assertEquals(Sold(belinda, 60.EUR), result)
    }
    
    @Test
    fun `reverse auction is passed if no bids meet reserve price`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        val belinda = aUser("belinda")
        
        val auction = sandra.createReverseAuction(
            description = "The Heart of St Camillus",
            chargePerBid = 2.EUR,
            reserve = 50.EUR)
        
        bob.bid(auction, 30.EUR)
        belinda.bid(auction, 40.EUR)
        
        val result = sandra.closeAuction(auction)
        assertEquals(Passed, result)
    }
}
