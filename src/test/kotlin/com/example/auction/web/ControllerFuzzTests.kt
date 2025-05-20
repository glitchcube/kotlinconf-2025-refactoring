package com.example.auction.web

import com.example.auction.AuctionApplication
import com.example.auction.acceptance.EUR
import com.example.auction.model.*
import com.example.auction.repository.AuctionRepositoryContract
import com.example.auction.service.AuctionResult
import com.example.auction.service.AuctionService
import com.example.auction.service.AuctionSummary
import com.example.auction.service.BidRequest
import com.example.auction.service.CreateAuctionRequest
import com.example.auction.service.Sold
import com.example.auction.web.ControllerFuzzTests.Config
import com.example.pii.UserId
import com.natpryce.snodge.json.defaultJsonMutagens
import com.natpryce.snodge.json.forStrings
import com.natpryce.snodge.mutants
import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.asSuccess
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import kotlin.random.Random
import kotlin.test.assertTrue


@WebMvcTest(controllers = [AuctionController::class])
@AutoConfigureMockMvc
@Import(AuctionApplication::class, Config::class)
class ControllerFuzzTests {
    @Autowired
    lateinit var mockMvc: MockMvc
    
    companion object : ArgumentsProvider {
        val validRequests = listOf(
            "/auctions" to "CreateBlindAuctionRequest",
            "/auctions" to "CreateReverseAuctionRequest",
            "/auctions" to "CreateVickreyAuctionRequest",
            "/auctions/1/bids" to "BidRequest",
        )
        
        override fun provideArguments(context: ExtensionContext?) =
            validRequests
                .map { (path, basename) -> Arguments.of(path, basename) }
                .stream()
    }
    
    @Configuration
    class Config {
        @Bean
        fun service() = DummyAuctionService()
    }
    
    @ParameterizedTest
    @ArgumentsSource(Companion::class)
    fun fuzz(path: String, basename: String) {
        val resourceName = "json/$basename.approved.json"
        
        val validJson = javaClass.getResource(resourceName)?.readText()
            ?: error("could not load $resourceName")
        
        Random.mutants(defaultJsonMutagens().forStrings(), 1000, validJson)
            .forEach { mutantJson -> testOneRequest(path, mutantJson) }
    }
    
    private fun testOneRequest(path: String, mutantJson: String) {
        val exchange = this.mockMvc.perform(
            post(path)
                .contentType(APPLICATION_JSON)
                .content(mutantJson)
        ).andReturn()
        
        val status = exchange.response.status
        assertTrue(
            status in (200..299) || status == 400,
            "\n$mutantJson\nstatus was $status"
        )
    }
}


class DummyAuctionService : AuctionService {
    override fun listAuctions(count: Int, after: AuctionId): List<AuctionSummary> {
        return emptyList()
    }
    
    override fun createAuction(rq: CreateAuctionRequest): AuctionId {
        return AuctionId(1)
    }
    
    override fun placeBid(auctionId: AuctionId, bid: BidRequest): Result4k<Auction, AuctionError> {
        return AuctionRepositoryContract.newReverseAuction(1).asSuccess()
    }
    
    override fun closeAuction(auctionId: AuctionId): AuctionResult {
        return Sold(UserId.newId(), 100.EUR)
    }
}
