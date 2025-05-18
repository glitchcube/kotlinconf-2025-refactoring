package com.example.auction.acceptance

import com.example.auction.model.BadRequestException
import com.example.pii.UserId
import kotlin.test.Test
import kotlin.test.assertFailsWith

interface UserVerificationTests : AuctionTesting {
    @Test
    fun `invalid seller is rejected`() {
        val eve = UserId("invalid-user-id")
        
        assertFailsWith<BadRequestException> {
            eve.createBlindAuction(description = "sneaky auction", reserve = 100.EUR)
        }
    }
    
    @Test
    fun `invalid bidder is rejected`() {
        val sandra = aUser("sandra")
        val auction = sandra.createBlindAuction(description = "A Louis XIV chair", reserve = 100.EUR)
        
        val eve = UserId("invalid-user-id")
        
        assertFailsWith<BadRequestException> {
            eve.bid(auction, 100.EUR)
        }
    }
}
