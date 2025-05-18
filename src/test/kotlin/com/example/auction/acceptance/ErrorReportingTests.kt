package com.example.auction.acceptance

import com.example.auction.EUR
import com.example.auction.Money
import com.example.auction.model.BadRequestException
import com.example.auction.model.WrongStateException
import com.example.auction.percent
import kotlin.test.Test
import kotlin.test.assertFailsWith


interface ErrorReportingTests : AuctionTesting {
    @Test
    fun `cannot create a blind auction with negative reserve`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createBlindAuction("A genuine Monet", reserve = (-1).EUR)
        }
    }
    
    @Test
    fun `cannot create an blind auction with negative commision`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createBlindAuction("A genuine Monet", 100.EUR, commission = (-10).percent)
        }
    }
    
    @Test
    fun `cannot create a blind auction with a fractional reserve`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createBlindAuction("A genuine Monet", reserve = Money("1.123",EUR))
        }
    }
    
    @Test
    fun `cannot create a vickrey auction with negative reserve`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createVickreyAuction("A genuine Monet", reserve = (-1).EUR)
        }
    }
    
    @Test
    fun `cannot create an vickrey auction with negative commision`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createVickreyAuction("A genuine Manet", 100.EUR, commission = (-10).percent)
        }
    }
    
    @Test
    fun `cannot create a vickrey auction with a fractional reserve`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createVickreyAuction("A genuine Man Ray", reserve = Money("1.123",EUR))
        }
    }
    
    @Test
    fun `cannot create a reverse auction with negative charge per bid`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createReverseAuction(
                description = "A genuine Monet",
                chargePerBid = (-1).EUR
            )
        }
    }
    
    @Test
    fun `cannot create a reverse auction with different currency for reserve and charge-per-bid`() {
        val sandra = aUser("sandra")
        
        assertFailsWith<BadRequestException> {
            sandra.createReverseAuction(
                description = "A genuine Monet",
                reserve = 100.GBP,
                chargePerBid = 1.EUR
            )
        }
    }
    
    @Test
    fun `cannot bid in a different currency`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        
        val auction = sandra.createBlindAuction("A genuine Degas", 100.EUR)
        
        assertFailsWith<BadRequestException> {
            bob.bid(auction, 100.GBP)
        }
    }
    
    @Test
    fun `seller cannot bid in their own auction`() {
        val sandra = aUser("sandra")
        
        val auction = sandra.createBlindAuction("A genuine Vermeer", 100.EUR)
        
        assertFailsWith<BadRequestException> {
            sandra.bid(auction, 100.EUR)
        }
    }
    
    @Test
    fun `cannot bid zero`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        
        val auction = sandra.createBlindAuction("A genuine Cezanne", 0.EUR)
        
        assertFailsWith<BadRequestException> {
            bob.bid(auction, 0.EUR)
        }
    }
    
    @Test
    fun `cannot bid when auction is closed`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        
        val auction = sandra.createBlindAuction("A genuine Vermeer", 100.EUR)
        
        sandra.closeAuction(auction)
        
        assertFailsWith<WrongStateException> {
            bob.bid(auction, 100.EUR)
        }
    }
    
    @Test
    fun `cannot list a negative number of auctions`() {
        val una = aUser("una")
        assertFailsWith<BadRequestException> {
            una.listAuctions(-1)
        }
    }
    
    @Test
    fun `cannot list zero auctions`() {
        val una = aUser("una")
        assertFailsWith<BadRequestException> {
            una.listAuctions(-1)
        }
    }
}

