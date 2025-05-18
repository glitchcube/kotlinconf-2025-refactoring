package com.example.auction.acceptance.http

import com.example.auction.acceptance.AuctionTesting
import com.example.auction.acceptance.EUR
import com.example.auction.model.AuctionId
import org.junit.jupiter.api.Tag
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.Test
import kotlin.test.assertEquals

interface ConcurrentBiddingTests : AuctionTesting, RequiresDatabaseAccess {
    @Test
    @Tag("slow")
    fun `no concurrency`() {
        testTransactions(1)
    }
    
    @Test
    @Tag("slow")
    fun `concurrent bidders in the same auction`() {
        testTransactions(2)
    }
    
    private fun testTransactions(concurrentRequests: Int) {
        val sandra = aUser("sandra")
        val auction = sandra.createBlindAuction("Concurrency test (${ZonedDateTime.now()})", 100.EUR)
        
        val count = 50
        val placed = AtomicInteger(0)
        
        val failure = AtomicReference<Throwable>()
        Executors.newFixedThreadPool(concurrentRequests).use { executor ->
            (1..count).forEach { i ->
                val bidder = aUser("bidder-$i")
                executor.submit {
                    if (failure.get() == null) try {
                        bidder.bid(auction, (100 + i).EUR)
                        placed.incrementAndGet()
                    } catch (t: Throwable) {
                        failure.set(t)
                    }
                }
            }
            
            executor.shutdown()
            executor.awaitTermination(10, SECONDS)
        }
        
        failure.get()?.let { throw it }
        
        assertEquals(count, placed.get(), "bids placed")
        
        sandra.closeAuction(auction)
        
        assertEquals(count, bidCount(auction), "bid count")
    }
    
    private fun bidCount(auction: AuctionId) : Int {
        dataSource.connection.use { connection ->
            connection.prepareStatement(
                "SELECT COUNT(*) FROM BID WHERE AUCTION = ?"
            ).use { statement ->
                statement.setLong(1, auction.value)
                statement.executeQuery().use { resultSet ->
                    resultSet.next()
                    return resultSet.getInt(1)
                }
            }
        }
    }
}
