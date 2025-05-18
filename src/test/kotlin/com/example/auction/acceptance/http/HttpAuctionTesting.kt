package com.example.auction.acceptance.http

import com.example.auction.acceptance.AuctionTesting
import com.example.auction.model.AuctionId
import com.example.auction.model.BadRequestException
import com.example.auction.model.Money
import com.example.auction.model.WrongStateException
import com.example.auction.service.AuctionResult
import com.example.auction.service.AuctionSummary
import com.example.auction.service.BidRequest
import com.example.auction.service.CreateAuctionRequest
import com.example.auction.service.CreateAuctionResponse
import com.example.auction.service.NotFoundException
import com.example.pii.UserId
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ProblemDetail
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpRequest.BodyPublishers
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodySubscribers
import javax.sql.DataSource
import kotlin.jvm.optionals.getOrNull
import kotlin.test.AfterTest
import kotlin.test.fail


interface RequiresDatabaseAccess {
    val dataSource: DataSource
}

abstract class HttpAuctionTesting : AuctionTesting {
    abstract val baseUri: URI
    abstract val objectMapper: ObjectMapper
    
    val client = HttpClient.newHttpClient()
    
    @AfterTest
    fun cleanup() {
        client.close()
    }
    
    override fun createAuction(rq: CreateAuctionRequest): AuctionId {
        return post<CreateAuctionResponse>(
            "/auctions",
            rq
        )
            ?.auctionId
            ?: fail("could not create auction")
    }
    
    override fun UserId.bid(auction: AuctionId, amount: Money) {
        post<Unit>("/auctions/$auction/bids", BidRequest(this, amount))
            ?.let { fail("expected no body, but got ${it::class.simpleName}: $it") }
    }
    
    override fun UserId.closeAuction(auction: AuctionId): AuctionResult {
        return post("/auctions/$auction/closed")
            ?: fail("no body in response")
    }
    
    override fun UserId.listAuctions(count: Int?, after: AuctionId?): List<AuctionSummary> {
        val uri = UriComponentsBuilder.newInstance()
            .path("/auctions")
            .apply { if (count != null) queryParam("count", count) }
            .apply { if (after != null) queryParam("after", after) }
            .toUriString()
        return get(uri)
    }
    
    protected inline fun <reified T> get(uri: String): T =
        call<T>(uri, "GET") ?: fail("no body in response")
    
    protected inline fun <reified T> post(uri: String, request: Any? = null): T? =
        call<T>(uri, "POST", request)
    
    protected inline fun <reified T> call(
        uri: String,
        method: String,
        request: Any? = null
    ): T? {
        val httpRequest = HttpRequest.newBuilder()
            .uri(baseUri.resolve(uri))
            .method(
                method,
                request?.let { BodyPublishers.ofString(objectMapper.writeValueAsString(it)) }
                    ?: BodyPublishers.noBody()
            )
            .setHeader("Content-Type", "application/json")
            .setHeader("Accept", "application/json,application/problem+json")
            .build()
        
        val response = client.send(httpRequest) { BodySubscribers.ofString(Charsets.UTF_8) }
        
        return when (val status = response.statusCode()) {
            204 -> null // No Content
            
            in 200..299 -> {
                if (APPLICATION_JSON.isCompatibleWith(response.contentType())) {
                    objectMapper.readValue(response.body())
                } else {
                    fail("expected content type of $APPLICATION_JSON but got ${response.contentType()}")
                }
            }
            
            in 400..499 -> {
                val problem = response.problem()
                val message = problem?.detail ?: "no further details"
                throw when (status) {
                    404 -> NotFoundException(message)
                    409 -> WrongStateException(message)
                    else -> BadRequestException(message)
                }
            }
            
            else -> {
                fail("HTTP status $status: ${response.body()}")
            }
        }
    }
    
    fun HttpResponse<String>.problem() =
        contentType()
            ?.takeIf { APPLICATION_JSON.isCompatibleWith(it) }
            ?.let { objectMapper.readValue(body(), ProblemDetail::class.java) }
    
    fun HttpResponse<*>.contentType(): MediaType? =
        headers().firstValue("Content-Type")
            .map(MediaType::parseMediaType)
            .getOrNull()
    
}



