package com.example.auction

import com.example.auction.model.AuctionId
import com.example.auction.model.BlindAuction
import com.example.auction.model.MonetaryAmount
import com.example.auction.model.WrongStateException
import com.example.pii.UserId
import kotlin.test.Test
import kotlin.test.assertFailsWith

class AuctionErrorCasesTest {
    @Test
    fun `cannot mark an open auction as settled`() {
        val a = BlindAuction(
            id = AuctionId(2),
            seller = UserId.newId(),
            description = "the-auction",
            currency = EUR,
            reserve = MonetaryAmount("100.00")
        )
        
        assertFailsWith<WrongStateException> {
            a.settled()
        }
    }
}