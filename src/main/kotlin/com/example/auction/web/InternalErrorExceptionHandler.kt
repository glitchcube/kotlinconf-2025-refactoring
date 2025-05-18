package com.example.auction.web

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class InternalErrorExceptionHandler {
    @ExceptionHandler(RuntimeException::class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    fun handleRuntimeException(e: RuntimeException) {
        // Because Spring does not log the exception type by default
        AuctionController.log.error("Internal error", e)
        throw e
    }
}