package com.example.auction.model

import dev.forkhandles.result4k.Result4k
import dev.forkhandles.result4k.recover
import java.lang.Exception

sealed interface AuctionError {
    fun orThrow(): Nothing {
        throw (this as Exception)
    }
}

fun<T> Result4k<T, AuctionError>.orThrow(): T = recover { it.orThrow() }