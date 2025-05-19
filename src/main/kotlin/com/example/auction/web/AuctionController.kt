package com.example.auction.web

import com.example.auction.model.AuctionId
import com.example.auction.service.AuctionResult
import com.example.auction.service.AuctionService
import com.example.auction.service.AuctionSummary
import com.example.auction.service.BidRequest
import com.example.auction.service.CreateAuctionRequest
import com.example.auction.service.CreateAuctionResponse
import dev.forkhandles.result4k.mapFailure
import dev.forkhandles.result4k.orThrow
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus


@Controller
@RequestMapping("/auctions")
class AuctionController(
    private val auctionService: AuctionService
) {
    companion object {
        val log = LoggerFactory.getLogger(AuctionController::class.java)
    }
    
    @GetMapping
    @ResponseBody
    fun listAuctions(
        @RequestParam count: Int = 25,
        @RequestParam after: AuctionId = AuctionId.NONE
    ): List<AuctionSummary> {
        return auctionService.listAuctions(count, after)
    }
    
    @PostMapping(
        consumes = ["application/json"],
        produces = ["application/json"]
    )
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    fun createAuction(@RequestBody rq: CreateAuctionRequest): CreateAuctionResponse {
        val newId = auctionService.createAuction(rq)
        return CreateAuctionResponse(newId)
    }
    
    @PostMapping(
        "{auctionId}/bids",
        consumes = ["application/json"],
        produces = []
    )
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun recordBid(@PathVariable auctionId: AuctionId, @RequestBody bid: BidRequest) {
        auctionService.placeBid(auctionId, bid).mapFailure { it.toException() }.orThrow()
    }
    
    @PostMapping(
        "{auctionId}/closed",
        consumes = [],
        produces = ["application/json"]
    )
    @ResponseBody
    fun closeAuction(@PathVariable auctionId: AuctionId): AuctionResult {
        return auctionService.closeAuction(auctionId)
    }
}

