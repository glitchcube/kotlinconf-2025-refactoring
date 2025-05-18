package com.example.auction.acceptance

import com.example.auction.model.MonetaryAmount
import com.example.auction.model.Money
import com.example.auction.percent
import com.example.auction.service.CreateAuctionRequest
import com.example.auction.service.CreateBlindAuctionRequest
import com.example.auction.service.CreateVickreyAuctionRequest
import com.example.auction.service.Sold
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementInstruction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

interface SealedBidAuction_Charges_Tests : AuctionTesting {
    @Test
    fun `blind auction charges seller commission calculated as fraction of winning bid`() {
        test(
            CreateBlindAuctionRequest(
                aUser("sandra"),
                "A Henry Moore bronze",
                100.EUR,
                commission = 20.percent
            ),
            winningBid = 130.EUR,
            expectedCommissionAmount = 26.EUR
        )
    }
    
    @Test
    fun `vickrey auction charges seller commission calculated as fraction of winning bid`() {
        test(
            CreateVickreyAuctionRequest(
                aUser("sandra"),
                "A book of 2025 Paddington Bear stamps",
                100.EUR,
                commission = 15.percent
            ),
            winningBid = 130.EUR,
            expectedCommissionAmount = 18.EUR
        )
    }
    
    @Test
    fun `blind auction commission is a fraction and so does not need to be in whole minor currency units`() {
        test(
            CreateBlindAuctionRequest(
                aUser("sandra"),
                "A book of 2025 Paddington Bear stamps",
                100.EUR,
                commission = MonetaryAmount("0.005")
            ),
            winningBid = 200.EUR,
            expectedCommissionAmount = 1.EUR
        )
    }
    
    @Test
    fun `vickrey auction commission is a fraction and so does not need to be in whole minor currency units`() {
        test(
            CreateVickreyAuctionRequest(
                aUser("sandra"),
                "A book of 2025 Paddington Bear stamps",
                100.EUR,
                commission = MonetaryAmount("0.005")
            ),
            winningBid = 200.EUR,
            expectedCommissionAmount = "0.60".EUR
        )
    }
    
    fun test(
        request: CreateAuctionRequest,
        winningBid: Money,
        expectedCommissionAmount: Money
    ) {
        val sandra = request.seller
        val bill = aUser("bill")
        val brenda = aUser("brenda")
        val will = aUser("will")
        
        val auction = createAuction(request)
        bill.bid(auction, 110.EUR)
        brenda.bid(auction, 120.EUR)
        will.bid(auction, winningBid)
        
        val result = sandra.closeAuction(auction)
        assertIs<Sold>(result, "result")
        
        assertEquals(
            actual = settlementOf(auction),
            expected = SettlementInstruction(
                order = OrderId(auction),
                collect = Collection(
                    from = will,
                    to = sandra,
                    amount = result.amount
                ),
                charges = listOf(
                    Charge(
                        fee = "commission",
                        from = sandra,
                        amount = expectedCommissionAmount
                    )
                )
            )
        )
    }
}
