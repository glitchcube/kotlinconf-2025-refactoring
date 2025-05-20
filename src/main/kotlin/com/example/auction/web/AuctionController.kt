package com.example.auction.web

import com.example.auction.model.AuctionId
import com.example.auction.model.BadRequestException
import com.example.auction.model.NotFoundException
import com.example.auction.model.WrongStateException
import com.example.auction.service.*
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.recover
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus.*
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
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
    @ResponseStatus(CREATED)
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

    fun recordBid(@PathVariable auctionId: AuctionId, @RequestBody bid: BidRequest): ResponseEntity<*> {
        return auctionService.placeBid(auctionId, bid).map {
            ResponseEntity<String>(NO_CONTENT)
        }.recover { exception ->
            val problemDetail = when (exception) {
                is BadRequestException -> ProblemDetail.forStatusAndDetail(BAD_REQUEST, exception.message)
                is NotFoundException -> ProblemDetail.forStatusAndDetail(NOT_FOUND, exception.message)
                is WrongStateException -> ProblemDetail.forStatusAndDetail(CONFLICT, exception.message)
            }
            ResponseEntity.status(problemDetail.status).body(problemDetail)
        }
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

