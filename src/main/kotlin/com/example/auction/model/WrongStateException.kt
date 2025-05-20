package com.example.auction.model


class WrongStateException(message: String, cause: Throwable? = null) :
    RuntimeException(message, cause), AuctionError