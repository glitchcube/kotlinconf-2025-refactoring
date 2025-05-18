package com.example.auction.repository

import com.example.auction.model.*
import com.example.auction.model.AuctionRules.Blind
import com.example.auction.model.AuctionRules.Reverse
import com.example.auction.model.AuctionRules.Vickrey
import com.example.auction.model.AuctionState.valueOf
import com.example.pii.UserId
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.util.Currency
import javax.sql.DataSource
import kotlin.jvm.optionals.getOrNull


// language=sql
private const val selectAuction = """
    SELECT
        A.ID,
        A.RULES,
        A.SELLER,
        A.DESCRIPTION,
        A.RESERVE,
        A.CURRENCY,
        A.STATE,
        A.COMMISSION,
        A.CHARGE_PER_BID,
        W.WINNER,
        W.OWED
    FROM AUCTION AS A
    LEFT OUTER JOIN AUCTION_WINNER AS W ON W.AUCTION = A.ID
"""

@Component
class SpringJdbcAuctionRepository(dataSource: DataSource) : AuctionRepository {
    private val jdbcClient = JdbcClient.create(dataSource)
    
    override fun addAuction(auction: Auction): Auction {
        val keyHolder = GeneratedKeyHolder()
        
        jdbcClient
            .sql(
                """
                INSERT INTO AUCTION
                    (RULES, SELLER, DESCRIPTION, CURRENCY, RESERVE, STATE, COMMISSION, CHARGE_PER_BID)
                VALUES
                    (:rules, :seller, :description, :currency, :reserve, :state, :commission, :chargePerBid)
                """
            )
            .param("rules", auction.rules.name)
            .param("seller", auction.seller.value)
            .param("description", auction.description)
            .param("currency", auction.currency.currencyCode)
            .param("reserve", auction.reserve.repr)
            .param("state", auction.state.name)
            .param("commission", auction.commission.repr)
            .param("chargePerBid", auction.chargePerBid.repr)
            .update(keyHolder)
        
        val newId = AuctionId(keyHolder.key?.toLong() ?: error("no ID generated"))
        val saved = auction.copy(id = newId)
        insertNewBids(saved)
        return saved
    }
    
    override fun updateAuction(auction: Auction) {
        jdbcClient.sql(
            // language=sql
            """
            UPDATE AUCTION
            SET STATE = :state
            WHERE ID = :id
            """
        )
            .param("id", auction.id.value)
            .param("state", auction.state.name)
            .update()
        
        insertNewBids(auction)
        
        auction.winner?.let { winner ->
            jdbcClient.sql(
                """
                MERGE INTO AUCTION_WINNER W
                USING (VALUES (:auctionId, :winner, :owed))
                AS vals(AUCTION, WINNER, OWED)
                ON W.AUCTION = vals.AUCTION
                WHEN NOT MATCHED THEN INSERT VALUES vals.AUCTION, vals.WINNER, vals.OWED
                """
            )
                .param("auctionId", auction.id.value)
                .param("winner", winner.winner.value)
                .param("owed", winner.owed.repr)
                .update()
        }
    }
    
    private fun insertNewBids(auction: Auction) {
        val updatedBids = auction.bids.map { bid ->
            if (bid.id == BidId.NONE) {
                val updated = insertBid(auction, bid)
                updated
            } else
                bid
        }
        auction.bids = updatedBids.toMutableList()
    }
    
    private fun insertBid(auction: Auction, bid: Bid): Bid {
        val keyHolder = GeneratedKeyHolder()
        
        jdbcClient
            .sql(
                """
                INSERT INTO BID (AUCTION, BIDDER, AMOUNT)
                VALUES (:auctionId, :bidder, :amount)
                """
            )
            .param("auctionId", auction.id.value)
            .param("bidder", bid.buyer.value)
            .param("amount", bid.amount.repr)
            .update(keyHolder)
        return bid.copy(id = BidId(keyHolder.key?.toLong() ?: error("no ID generated")))
    }
    
    override fun getAuction(id: AuctionId): Auction? {
        val bids = loadBids(id)
        return jdbcClient
            .sql(
                // language=sql
                """
                $selectAuction
                WHERE ID = :id
                """
            )
            .param("id", id.value)
            .query { row, _ -> row.toAuction(bids) }
            .optional()
            .getOrNull()
    }
    
    override fun listOpenAuctions(count: Int, after: AuctionId): List<Auction> =
        jdbcClient
            .sql(
                // language=sql
                """
                $selectAuction
                WHERE ID > :id
                AND STATE = 'open'
                ORDER BY ID
                LIMIT :count
                """
            )
            .param("id", after.value)
            .param("count", count)
            .query { row, _ -> row.toAuction(mutableListOf()) }
            .list()
    
    override fun listForSettlement(count: Int, after: AuctionId) =
        jdbcClient
            .sql(
                // language=sql
                """
                SELECT ID
                FROM AUCTION
                WHERE ID > :id
                AND STATE = 'closed'
                ORDER BY ID
                LIMIT :count
                FOR UPDATE
                """
            )
            .param("id", after.value)
            .param("count", count)
            .query { row, _ -> AuctionId(row.getLong("ID")) }
            .list()
    
    private fun loadBids(auctionId: AuctionId): MutableList<Bid> =
        jdbcClient
            .sql(
                // language=sql
                """
                SELECT
                    BID.ID AS ID,
                    BID.BIDDER AS BIDDER,
                    BID.AMOUNT AS AMOUNT,
                    AUCTION.CURRENCY AS CURRENCY
                FROM BID
                JOIN AUCTION ON BID.AUCTION = AUCTION.ID
                WHERE AUCTION.ID = :auctionId
                ORDER BY BID.ID
                """
            )
            .param("auctionId", auctionId.value)
            .query { row, _ -> row.toBid() }
            .list()
}

private fun ResultSet.toBid(): Bid {
    val currency = Currency.getInstance(getString("CURRENCY"))
    return Bid(
        id = BidId(getLong("ID")),
        buyer = UserId(getString("BIDDER")),
        amount = MonetaryAmount(getBigDecimal("AMOUNT").setScale(currency.defaultFractionDigits))
    )
}

private fun ResultSet.toAuction(bids: MutableList<Bid>): Auction {
    val currency = Currency.getInstance(getString("CURRENCY"))
    val rules = AuctionRules.valueOf(getString("RULES"))
    val winnerIdValue = getString("WINNER")
    val winner = if (winnerIdValue != null) {
        AuctionWinner(
            UserId(winnerIdValue),
            MonetaryAmount(getBigDecimal("OWED").setScale(currency.defaultFractionDigits))
        )
    } else null
    return Auction(
        rules = rules,
        seller = UserId(getString("SELLER")),
        description = getString("DESCRIPTION"),
        currency = currency,
        reserve = MonetaryAmount(getBigDecimal("RESERVE").setScale(currency.defaultFractionDigits)),
        commission = MonetaryAmount(getBigDecimal("COMMISSION")),
        chargePerBid = MonetaryAmount(getBigDecimal("CHARGE_PER_BID")),
        id = AuctionId(getLong("ID")),
        bids = bids,
        state = AuctionState.valueOf(getString("STATE")),
        winner = winner
    )
}
