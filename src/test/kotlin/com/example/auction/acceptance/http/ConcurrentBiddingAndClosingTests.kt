package com.example.auction.acceptance.http

import com.example.auction.EUR
import com.example.auction.acceptance.AuctionTesting
import com.example.auction.acceptance.EUR
import com.example.auction.model.Money
import com.example.auction.model.WrongStateException
import com.example.auction.repository.SpringJdbcAuctionRepository
import com.example.auction.service.AuctionResult
import com.example.auction.service.Sold
import org.junit.jupiter.api.Tag
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

interface ConcurrentBiddingAndClosingTests : AuctionTesting, RequiresDatabaseAccess {
    @Test
    @Tag("slow")
    fun `no concurrency`() {
        testTransactions(1)
    }
    
    @Test
    @Tag("slow")
    fun `concurrent bidding and closing`() {
        testTransactions(2)
    }
    
    private fun testTransactions(concurrentRequests: Int) {
        val sandra = aUser("sandra")
        val auctionId = sandra.createBlindAuction("Concurrency test (${ZonedDateTime.now()})", 100.EUR)
        
        val count = 10
        val result = AtomicReference<AuctionResult>()
        
        val failure = AtomicReference<Throwable>()
        Executors.newFixedThreadPool(concurrentRequests).use { executor ->
            (1..concurrentRequests).forEach { b ->
                val bidder = aUser("bidder-$b")
                executor.submit {
                    (1..count).forEach { i ->
                        if (failure.get() != null) {
                            return@submit
                        }
                        
                        try {
                            bidder.bid(auctionId, (100 + i).EUR)
                            if (b == 1 && i == count / 2) {
                                result.set(sandra.closeAuction(auctionId))
                            }
                        }
                        catch (e : WrongStateException) {
                            println(e)
                            return@submit
                        }
                        catch (t: Throwable) {
                            failure.set(t)
                            return@submit
                        }
                    }
                }
            }
            
            executor.shutdown()
            executor.awaitTermination(10, SECONDS)
        }
        
        failure.get()?.let { throw it }
        
        result.get().let { winner ->
            assertIs<Sold>(winner)
            
            val repo = SpringJdbcAuctionRepository(dataSource)
            val auction = repo.getAuction(auctionId)
                ?: fail("could not load auction ${auctionId}")
            val topBid = auction.bids.maxByOrNull { it.amount }
                ?: fail("no top bid")

            val topRecordedBid = Sold(
                topBid.buyer,
                Money(topBid.amount, EUR).toWholeMinorUnits())

            assertEquals(winner, topRecordedBid,
                "higher bid stored than was set as auction winner")
        }
    }
}
