package com.example.auction.service

import com.example.auction.model.Money
import com.example.pii.UserId
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = NAME, include = PROPERTY, property = "result")
sealed class AuctionResult

@JsonTypeName("passed")
data object Passed : AuctionResult()

@JsonTypeName("sold")
data class Sold(val bidder: UserId, val amount: Money) : AuctionResult()