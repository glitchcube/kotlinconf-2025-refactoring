package com.example.auction.web

import com.example.auction.model.BadRequestException
import com.example.auction.model.WrongStateException
import com.example.auction.model.NotFoundException
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.http.HttpStatus.CONFLICT
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class AuctionExceptionHandlers {
    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    fun handle(e: BadRequestException) =
        ProblemDetail.forStatusAndDetail(BAD_REQUEST, e.message)
    
    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    fun handle(e: NotFoundException) =
        ProblemDetail.forStatusAndDetail(NOT_FOUND, e.message)
    
    @ExceptionHandler(WrongStateException::class)
    @ResponseStatus(CONFLICT)
    @ResponseBody
    fun handle(e: WrongStateException) =
        ProblemDetail.forStatusAndDetail(CONFLICT, e.message)
}