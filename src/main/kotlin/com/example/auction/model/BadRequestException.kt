package com.example.auction.model

class BadRequestException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)