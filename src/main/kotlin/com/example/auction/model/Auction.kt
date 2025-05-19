package com.example.auction.model

import com.example.auction.model.AuctionState.closed
import com.example.auction.model.AuctionState.open
import com.example.auction.model.MonetaryAmount.Companion.ZERO
import com.example.pii.UserId
import com.example.settlement.Charge
import com.example.settlement.Collection
import com.example.settlement.OrderId
import com.example.settlement.SettlementInstruction
import dev.forkhandles.result4k.*
import java.math.RoundingMode.DOWN
import java.math.RoundingMode.UP
import java.util.Currency

sealed class AuctionError() {
    abstract val message: String
    data class BadRequest(override val message: String) : AuctionError()
    data class WrongState(override val message: String) : AuctionError()
}

fun Result<Auction, AuctionError>.asExceptionFailure(): Result<Auction, RuntimeException> =
    mapFailure {
        when (it) {
            is AuctionError.BadRequest -> BadRequestException(it.message)
            is AuctionError.WrongState -> WrongStateException(it.message)
        }
    }

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
    val winner: AuctionWinner?,
) {

    fun placeBid(
        buyer: UserId,
        bid: Money,
    ): Result<Auction, AuctionError> {
        if (buyer == seller) {
            return Failure(AuctionError.BadRequest("shill bidding detected by $seller"))
        }
        if (bid.currency != currency) {
            return Failure(AuctionError.BadRequest("bid in wrong currency, should be $currency"))
        }
        if (bid.amount == ZERO) {
            return Failure(AuctionError.BadRequest("zero bid"))
        }
        if (state != open) {
            return Failure(AuctionError.WrongState("auction $id is closed"))
        }

        return Success(this.copy(bids = bids + Bid(buyer, bid.amount)))
    }

    fun close(): Auction = copy(state = closed, winner = decideWinner())

    protected fun decideWinner(): AuctionWinner? = rules.decideWinner(this)

    fun settled(): Result4k<Auction, WrongStateException> {
        if (state == open) {
            return Failure(WrongStateException("auction $id not closed"))
        }
        return Success(copy(state = AuctionState.settled))
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

    override fun toString(): String {
        return "${this::class.simpleName}(seller=$seller, description='$description', currency=$currency, reserve=$reserve, commission=$commission, chargePerBid=$chargePerBid, id=$id, state=$state, rules=$rules, winner=$winner)"
    }
}
