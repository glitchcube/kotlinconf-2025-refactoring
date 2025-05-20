package com.example.auction.model

class NotFoundException(message: String, cause: Throwable? = null) : RuntimeException(message, cause), AuctionError