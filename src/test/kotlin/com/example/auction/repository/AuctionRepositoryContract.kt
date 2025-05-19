package com.example.auction.repository

import com.example.auction.EUR
import com.example.auction.acceptance.EUR
import com.example.auction.model.Auction
import com.example.auction.model.AuctionId
import com.example.auction.model.AuctionWinner
import com.example.auction.model.Bid
import com.example.auction.model.BidId
import com.example.auction.model.blindAuction
import com.example.auction.model.MonetaryAmount
import com.example.auction.model.reverseAuction
import com.example.auction.model.vickreyAuction
import com.example.pii.UserId
import dev.forkhandles.result4k.orThrow
import kotlin.test.assertNotEquals
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

interface AuctionRepositoryContract {
    val repository: AuctionRepository

    @Test
    fun `save a new auction`() {
        val auction = repository.addAuction(newBlindAuction())
        val auctionId = auction.id
        val loaded = load(auctionId)

        assertEquals(auction.seller, loaded.seller, "seller")
        assertEquals(auction.description, loaded.description, "description")
        assertTrue(auction.reserve.compareTo(loaded.reserve) == 0, "reserve")
        assertEquals(auction.currency, loaded.currency, "currency")
        assertNull(auction.winner, "winner")
    }

    @Test
    fun `returns null when no auction found`() {
        assertNull(repository.getAuction(AuctionId(99)), "loaded")
    }

    @Test
    fun `adding bids`() {
        val auction = repository.addAuction(newBlindAuction())
            .placeBid(alice, 1.EUR).orThrow()
            .placeBid(bob, 2.EUR).orThrow()
            .let { repository.updateAuction(it) }

        assertNotEquals(BidId.NONE, auction.bids[0].id, "bids[0]")
        assertNotEquals(BidId.NONE, auction.bids[1].id, "bids[1]")
        assertNotEquals(auction.bids[0], auction.bids[1], "bid ids are not equal")

        val auctionId = auction.id
        val loaded = repository.getAuction(auction.id)
            ?: fail("could not reload auction $auctionId")

        assertEquals(alice, loaded.bids[0].buyer)
        assertEquals(MonetaryAmount("1.00"), loaded.bids[0].amount)

        assertEquals(bob, loaded.bids[1].buyer)
        assertEquals(MonetaryAmount("2.00"), loaded.bids[1].amount)
    }

    @Test
    fun `saving and loading the winner`() {
        val auction = repository.addAuction(newBlindAuction())
            .placeBid(alice, 1.EUR).orThrow()
            .placeBid(bob, 2.EUR).orThrow()
            .let { repository.updateAuction(it) }

        val auctionId = auction.id
        run {
            val loaded = load(auctionId).copy(
                winner = AuctionWinner(winner = bob, owed = MonetaryAmount("2.00"))
            )
            repository.updateAuction(loaded)
        }

        run {
            val loaded = load(auctionId)
            assertNotNull(loaded.winner)
            assertEquals(AuctionWinner(bob, MonetaryAmount("2.00")), loaded.winner)
        }
    }

    @Test
    fun `listing open auctions`() {
        val a1 = repository.addAuction(newBlindAuction(1))
        val a2 = repository.addAuction(newVickreyAuction(2))
        val a3 = repository.addAuction(newReverseAuction(3))
        val a4 = repository.addAuction(newBlindAuction(4))

        val loaded = repository.listOpenAuctions(4, after = a1.id.predecessor())

        assertEquals(4, loaded.count())
        assertEquals(listOf(a1.id, a2.id, a3.id, a4.id), loaded.map { it.id })
    }

    @Test
    fun `listing auctions for settlement`() {
        val auctions = (1..8).map { i ->
            repository.addAuction(newBlindAuction(i))
        }

        val closedAuctions = repository.listOpenAuctions(count = 3, after = auctions[1].id)
            .map {
                it.close().let { closed ->
                    repository.updateAuction(closed)
                }
                repository.getAuction(it.id)
                    ?: fail("could not reload closed auction")
            }

        val forSettlement = repository.listForSettlement(10, after = auctions[1].id)

        assertEquals(3, forSettlement.count())
        assertEquals(closedAuctions.map { it.id }, forSettlement)
    }

    fun load(auctionId: AuctionId): Auction {
        return repository.getAuction(auctionId)
            ?: fail("could not load auction $auctionId")
    }

    private fun AuctionId.predecessor() = AuctionId(value - 1)

    private operator fun Iterable<Bid>.get(bidder: UserId) =
        firstOrNull { it.buyer == bidder }
            ?: fail("no bid found for $bidder")

    companion object {
        fun newBlindAuction(n: Int = 1) = blindAuction(
            seller = UserId.newId(),
            description = "description-$n",
            currency = EUR,
            reserve = MonetaryAmount(1),
        )

        fun newVickreyAuction(n: Int = 1) = vickreyAuction(
            seller = UserId.newId(),
            description = "description-$n",
            currency = EUR,
            reserve = MonetaryAmount(1),
        )

        fun newReverseAuction(n: Int = 1) = reverseAuction(
            seller = UserId.newId(),
            description = "description-$n",
            currency = EUR,
        )

        val alice = UserId("alice")
        val bob = UserId("bob")
    }
}
