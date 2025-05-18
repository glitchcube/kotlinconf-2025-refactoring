package com.example.auction.web

import com.example.auction.AuctionApplication
import com.example.auction.acceptance.EUR
import com.example.auction.model.AuctionId
import com.example.auction.model.MonetaryAmount
import com.example.auction.repository.InMemoryAuctionRepository
import com.example.auction.service.AuctionService
import com.example.auction.service.AuctionServiceImpl
import com.example.auction.service.BidRequest
import com.example.auction.service.CreateBlindAuctionRequest
import com.example.auction.web.ControllerTests.Config
import com.example.pii.UserId
import com.example.pii.UserIdValidator
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.Test
import kotlin.test.assertNotEquals


@WebMvcTest(controllers = [AuctionController::class])
@AutoConfigureMockMvc
@Import(AuctionApplication::class, Config::class)
class ControllerTests {
    @Autowired
    lateinit var mockMvc: MockMvc
    
    @Autowired
    lateinit var objectMapper: ObjectMapper
    
    @Autowired
    lateinit var service: AuctionService
    
    @Configuration
    class Config {
        @Bean
        fun service() = AuctionServiceImpl(
            repository = InMemoryAuctionRepository(),
            piiVault = object : UserIdValidator {
                override fun isValid(userId: UserId) = true
            }
        )
    }
    
    @Test
    fun `returns 404 when bidding on non-existing auction`() {
        val existingAuctionId = createAuction()
        val invalidAuctionId = AuctionId(99)
        
        assertNotEquals(existingAuctionId, invalidAuctionId)
        
        this.mockMvc.perform(
            post(bidUrl(invalidAuctionId))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BidRequest(UserId("bidder"), 100.EUR))))
            .andExpect(status().isNotFound)
    }
    
    @Test
    fun `returns 404 when closing non-existing auction`() {
        val existingAuctionId = createAuction()
        val invalidAuctionId = AuctionId(99)
        
        this.mockMvc.perform(post("/auctions/$existingAuctionId/closed"))
            .andExpect(status().is2xxSuccessful)
        
        this.mockMvc.perform(post("/auctions/$invalidAuctionId/closed"))
            .andExpect(status().isNotFound)
            .andReturn()
    }
    
    private fun createAuction(): AuctionId {
        val existingAuctionId = service.createAuction(
            CreateBlindAuctionRequest(
                seller = UserId("seller"),
                description = "example",
                reserve = 10.EUR,
                commission = MonetaryAmount("0.1")
            )
        )
        
        // Check that it's possible to bid in the auction
        this.mockMvc.perform(
            post(bidUrl(existingAuctionId))
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(BidRequest(UserId("bidder"), 100.EUR)))
        )
            .andExpect(status().is2xxSuccessful)
        
        return existingAuctionId
    }
    
    private fun bidUrl(existingAuctionId: AuctionId) = "/auctions/${existingAuctionId}/bids"
}

