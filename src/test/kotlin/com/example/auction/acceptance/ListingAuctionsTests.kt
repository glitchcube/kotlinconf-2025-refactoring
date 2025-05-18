package com.example.auction.acceptance

import com.example.auction.model.AuctionId
import kotlin.test.Test
import kotlin.test.assertEquals


interface ListingAuctionsTests : AuctionTesting {
    @Test
    fun `listing auctions`() {
        val sandra = aUser("sandra")
        val simon = aUser("simon")
        
        val bob = aUser("bob")
        
        val id1 = sandra.createBlindAuction("First edition Pride and Prejudice", 100.EUR)
        val id2 = simon.createBlindAuction("First edition The Great Gatsby", 200.EUR)
        
        val auctions = bob.listAuctions(after = AuctionId(id1.value - 1), count = 100)
        
        assertEquals(listOf(id1, id2), auctions.map { it.id })
    }
    
    @Test
    fun `paging through auctions`() {
        val sandra = aUser("sandra")
        
        val bob = aUser("bob")
        
        val firstAuction = sandra.createBlindAuction("The Codex Leicester", 1.EUR)
        (2..7).forEach { i ->
            sandra.createBlindAuction("Auction $i", i.EUR)
        }
        
        val firstPage = bob.listAuctions(count = 5, after = AuctionId(firstAuction.value - 1))
        assertEquals(5, firstPage.count())
        
        val secondPage = bob.listAuctions(5, after = firstPage.last().id)
        assertEquals(2, secondPage.count())
    }
}
