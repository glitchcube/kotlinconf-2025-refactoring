package com.example.auction.service

import com.example.auction.model.MonetaryAmount
import com.example.auction.model.Money
import com.example.pii.UserId
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import com.fasterxml.jackson.annotation.JsonTypeName

@JsonTypeInfo(use = NAME, include = PROPERTY, property = "rules")
sealed interface CreateAuctionRequest {
    val seller: UserId
    val description: String
}

@JsonTypeName("Blind")
data class CreateBlindAuctionRequest(
    override val seller: UserId,
    override val description: String,
    val reserve: Money,
    val commission: MonetaryAmount,
) : CreateAuctionRequest

@JsonTypeName("Vickrey")
data class CreateVickreyAuctionRequest(
    override val seller: UserId,
    override val description: String,
    val reserve: Money,
    val commission: MonetaryAmount
) : CreateAuctionRequest

@JsonTypeName("Reverse")
data class CreateReverseAuctionRequest(
    override val seller: UserId,
    override val description: String,
    val reserve: Money,
    val chargePerBid: Money
) : CreateAuctionRequest
