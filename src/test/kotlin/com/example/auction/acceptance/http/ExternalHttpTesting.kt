package com.example.auction.acceptance.http

import com.example.auction.model.AuctionId
import com.example.auction.web.AuctionModule
import com.example.pii.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.hsqldb.jdbc.JDBCDataSource
import org.junit.jupiter.api.Tag
import org.opentest4j.TestAbortedException
import java.net.URI
import java.util.UUID

fun env(name: String): String =
    System.getenv(name)
        ?: throw TestAbortedException("environment variable $name not set, test skipped")

@Tag("external")
@Tag("http")
abstract class ExternalHttpTesting : HttpAuctionTesting(), RequiresDatabaseAccess {
    override val baseUri: URI = URI(env("AUCTION_URL"))
    val piiVaultUrl = URI(env("PII_VAULT_URL"))
    val dbUrl = env("DB_URL")
    
    val testId = UUID.randomUUID().toString()
    
    override val objectMapper: ObjectMapper = jacksonObjectMapper().apply {
        registerModule(AuctionModule())
    }
    
    override val dataSource = JDBCDataSource().apply {
        setURL(dbUrl)
    }
    
    override fun aUser(name: String): UserId {
        val userId = UserId("${name}_$testId")
        post<Unit>(piiVaultUrl.resolve("/users/${userId}").toString())
        return userId
    }
    
    override fun settlementOf(auction: AuctionId) =
        TODO("Not yet implemented")
}