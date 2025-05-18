package com.example.auction.service

import com.example.auction.model.Auction
import com.example.auction.model.AuctionId
import com.example.auction.model.BadRequestException
import com.example.auction.model.blindAuction
import com.example.auction.model.MonetaryAmount
import com.example.auction.model.MonetaryAmount.Companion.ZERO
import com.example.auction.model.Money
import com.example.auction.model.reverseAuction
import com.example.auction.model.vickreyAuction
import com.example.auction.repository.AuctionRepository
import com.example.pii.UserIdValidator
import org.springframework.dao.ConcurrencyFailureException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Isolation.SERIALIZABLE
import org.springframework.transaction.annotation.Transactional
import java.math.RoundingMode.DOWN
import java.math.RoundingMode.UP
import java.util.Currency
import kotlin.annotation.AnnotationRetention.RUNTIME


@Retention(RUNTIME)
@Transactional(isolation = SERIALIZABLE)
@Retryable(
    // Spring does not make it easy to implement serialisable transactions!
    retryFor = [ConcurrencyFailureException::class],
    exceptionExpression = """
        cause instanceof T(java.sql.SQLTransactionRollbackException)
        and cause.SQLState == '40001'
        """,
    maxAttempts = 10,
    backoff = Backoff(delay = 100, maxDelay = 2000, multiplier = 1.5, random = true)
)
annotation class ApiTransaction


@Component
class AuctionServiceImpl(
    private val repository: AuctionRepository,
    private val piiVault: UserIdValidator
) : AuctionService {
    @ApiTransaction
    override fun listAuctions(count: Int, after: AuctionId): List<AuctionSummary> {
        checkCount(count)
        return repository.listOpenAuctions(count, after)
            .map { it.summarise() }
    }
    
    @ApiTransaction
    override fun createAuction(rq: CreateAuctionRequest): AuctionId {
        if (!piiVault.isValid(rq.seller)) {
            throw BadRequestException("invalid user id ${rq.seller}")
        }
        
        val auction = newAuction(rq)
        return repository.addAuction(auction).id
    }
    
    private fun newAuction(rq: CreateAuctionRequest) = when (rq) {
        is CreateBlindAuctionRequest -> newAuction(rq)
        is CreateVickreyAuctionRequest -> newAuction(rq)
        is CreateReverseAuctionRequest -> newAuction(rq)
    }
    
    private fun newAuction(rq: CreateBlindAuctionRequest): Auction {
        checkPositiveAmount(rq.reserve.amount, "reserve")
        checkWholeMinorUnits(rq.reserve.amount, rq.reserve.currency, "reserve")
        checkPositiveAmount(rq.commission, "commission")
        
        return blindAuction(
            seller = rq.seller,
            description = rq.description,
            currency = rq.reserve.currency,
            reserve = rq.reserve.amount,
            commission = rq.commission,
        )
    }
    
    private fun newAuction(rq: CreateVickreyAuctionRequest): Auction {
        checkPositiveAmount(rq.reserve.amount, "reserve")
        checkWholeMinorUnits(rq.reserve.amount, rq.reserve.currency, "reserve")
        checkPositiveAmount(rq.commission, "commission")
        
        return vickreyAuction(
            seller = rq.seller,
            description = rq.description,
            currency = rq.reserve.currency,
            reserve = rq.reserve.amount,
            commission = rq.commission,
        )
    }
    
    private fun newAuction(rq: CreateReverseAuctionRequest): Auction {
        checkWholeMinorUnits(rq.reserve.amount, rq.reserve.currency, "reserve")
        checkPositiveAmount(rq.chargePerBid.amount, "charge per bid")
        
        if (rq.reserve.currency != rq.chargePerBid.currency) {
            throw BadRequestException("reserve and charge-per-bid must have same currency")
        }
        
        return reverseAuction(
            seller = rq.seller,
            description = rq.description,
            currency = rq.reserve.currency,
            reserve = rq.reserve.amount,
            commission = ZERO,
            chargePerBid = rq.chargePerBid.amount,
        )
    }
    
    @ApiTransaction
    override fun placeBid(auctionId: AuctionId, bid: BidRequest) {
        if (!piiVault.isValid(bid.buyer)) {
            throw BadRequestException("invalid user id ${bid.buyer}")
        }
        
        val auction = loadAuction(auctionId)
            .placeBid(bid.buyer, bid.amount)
        repository.updateAuction(auction)
    }
    
    @ApiTransaction
    override fun closeAuction(auctionId: AuctionId): AuctionResult {
        val auction = loadAuction(auctionId)
            .close()
        val updated = repository.updateAuction(auction)
        
        return when (val result = updated.winner) {
            null -> Passed
            else -> Sold(
                result.winner,
                Money(result.owed, updated.currency).toWholeMinorUnits(UP)
            )
        }
    }
    
    private fun loadAuction(auctionId: AuctionId): Auction {
        return repository.getAuction(auctionId)
            ?: throw NotFoundException("no auction found with id $auctionId")
    }
}

private fun checkCount(count: Int) {
    if (count <= 0) {
        throw BadRequestException("count must greater than zero")
    }
}

private fun checkPositiveAmount(amount: MonetaryAmount, description: String) {
    if (amount < ZERO) {
        throw BadRequestException("$description amount cannot be negative")
    }
}

private fun checkWholeMinorUnits(amount: MonetaryAmount, currency: Currency, description: String) {
    if (amount != amount.withScale(currency.defaultFractionDigits, DOWN)) {
        throw BadRequestException("$description cannot have fractional minor currency units")
    }
}


fun Auction.summarise() = AuctionSummary(
    id = id,
    rules = rules,
    seller = seller,
    description = description,
    currency = currency,
    reserve = Money(reserve, currency),
    commission = commission,
    chargePerBid = Money(chargePerBid, currency)
)
