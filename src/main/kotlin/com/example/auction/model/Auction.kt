package com.example.auction.model

import com.example.auction.model.AuctionState.closed
import com.example.auction.model.AuctionState.open
import com.example.auction.model.AuctionState.settled
import com.example.auction.model.MonetaryAmount.Companion.ZERO
import com.example.pii.UserId
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementInstruction
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asFailure
import dev.forkhandles.result4k.asSuccess
import java.math.RoundingMode.DOWN
import java.math.RoundingMode.UP
import java.util.Currency

data class Auction(
    val rules: AuctionRules,
    val seller: UserId,
    val description: String,
    val currency: Currency,
    val reserve: MonetaryAmount,
    val commission: MonetaryAmount,
    val chargePerBid: MonetaryAmount,
    val id: AuctionId,
    val state: AuctionState,
    val bids: List<Bid>,
    val winner: AuctionWinner?
) {
    fun saved(newId: AuctionId) =
        copy(id = newId)

    fun placeBid(
        buyer: UserId,
        bid: Money,
    ): Result4k<Auction, AuctionError> {
        if (buyer == seller) {
            return BadRequestException("shill bidding detected by $seller").asFailure()
        }
        if (bid.currency != currency) {
            return  BadRequestException("bid in wrong currency, should be $currency").asFailure()
        }
        if (bid.amount == ZERO) {
            return BadRequestException("zero bid").asFailure()
        }
        if (state != open) {
            return WrongStateException("auction $id is closed").asFailure()
        }

        return copy(bids = bids + Bid(buyer, bid.amount)).asSuccess()
    }

    fun close(): Auction {
        return copy(state = closed, winner = decideWinner())
    }
    
    fun decideWinner() = rules.decideWinner(this)
    
    fun settled(): Auction {
        if (state == open) {
            throw WrongStateException("auction $id not closed")
        }
        
        return copy(state = settled)
    }
    
    fun settlement(): SettlementInstruction? {
        val winner = this.winner ?: return null
        
        val commissionCharges = when {
            commission == ZERO -> emptyList()
            else -> listOf(
                Charge(
                    fee = "commission",
                    from = seller,
                    amount = Money(winner.owed * commission, currency).toWholeMinorUnits(DOWN)
                )
            )
        }
        
        val bidCharges = when {
            chargePerBid == ZERO -> emptyList()
            else -> bids
                .groupingBy { it.buyer }
                .eachCount()
                .map { (buyer, count) ->
                    Charge(
                        fee = "bids",
                        from = buyer,
                        unit = Money(chargePerBid, currency),
                        quantity = count,
                        total = Money(chargePerBid * count, currency).toWholeMinorUnits(UP)
                    )
                }
                .sortedBy { it.from.value }
        }
        
        return SettlementInstruction(
            order = OrderId(id),
            collect = Collection(
                from = winner.winner,
                to = seller,
                amount = Money(winner.owed, currency).toWholeMinorUnits(UP)
            ),
            charges = commissionCharges + bidCharges
        )
    }
}
