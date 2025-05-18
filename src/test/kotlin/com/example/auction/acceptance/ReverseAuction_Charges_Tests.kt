package com.example.auction.acceptance

import com.example.auction.EUR
import com.example.auction.model.Money
import com.example.auction.service.Sold
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementInstruction
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

interface ReverseAuction_Charges_Tests : AuctionTesting {
    @Test
    fun `charge seller commission calculated as fraction of winning bid`() {
        val sandra = aUser("sandra")
        val bob = aUser("bob")
        val brenda = aUser("brenda")
        val will = aUser("will")
        
        val auction = sandra.createReverseAuction(
            description = "UK 6G mobile frequency license",
            chargePerBid = 2.EUR
        )
        
        brenda.bid(auction, 2.EUR)
        brenda.bid(auction, 3.EUR)
        bob.bid(auction, 1.EUR)
        bob.bid(auction, 3.EUR)
        bob.bid(auction, 5.EUR)
        bob.bid(auction, 7.EUR)
        will.bid(auction, 1.EUR)
        will.bid(auction, 2.EUR)
        will.bid(auction, 3.EUR)
        will.bid(auction, 4.EUR)
        will.bid(auction, 5.EUR)
        
        val result = sandra.closeAuction(auction)
        assertTrue(result is Sold)
        
        val settlement = settlementOf(auction)
        assertEquals(
            actual = settlement,
            expected = SettlementInstruction(
                order = OrderId(auction),
                collect = Collection(
                    from = will,
                    to = sandra,
                    amount = 4.EUR
                ),
                charges = listOf(
                    Charge(
                        fee = "bids",
                        from = bob,
                        unit = Money(BigDecimal("2.000000"), EUR),
                        quantity = 4,
                        total = 8.EUR
                    ),
                    Charge(
                        fee = "bids",
                        from = brenda,
                        unit = Money(BigDecimal("2.000000"), EUR),
                        quantity = 2,
                        total = 4.EUR
                    ),
                    Charge(
                        fee = "bids",
                        from = will,
                        unit = Money(BigDecimal("2.000000"), EUR),
                        quantity = 5,
                        total = 10.EUR
                    )
                )
            )
        )
    }
}
