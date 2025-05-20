package com.example.auction.model

import com.example.auction.model.AuctionState.closed
import com.example.auction.model.AuctionState.open
import com.example.auction.model.MonetaryAmount.Companion.ZERO
import com.example.pii.UserId
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementInstruction
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
    var state: AuctionState,
    val bids: List<Bid>,
    var winner: AuctionWinner?
) {
    fun saved(newId: AuctionId) =
        copy(id = newId)
    
    fun placeBid(buyer: UserId, bid: Money): Auction {
        if (buyer == seller) {
            throw BadRequestException("shill bidding detected by $seller")
        }
        if (bid.currency != currency) {
            throw BadRequestException("bid in wrong currency, should be $currency")
        }
        if (bid.amount == ZERO) {
            throw BadRequestException("zero bid")
        }
        if (state != open) {
            throw WrongStateException("auction $id is closed")
        }
        
        return copy(bids = bids + Bid(buyer, bid.amount))
    }
    
    fun close() {
        state = closed
        winner = decideWinner()
    }
    
    fun decideWinner() = rules.decideWinner(this)
    
    fun settled() {
        if (state == open) {
            throw WrongStateException("auction $id not closed")
        }
        
        state = AuctionState.settled
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
