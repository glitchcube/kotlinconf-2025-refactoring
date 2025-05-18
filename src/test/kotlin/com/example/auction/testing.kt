package com.example.auction

import com.example.auction.model.MonetaryAmount
import com.example.auction.model.Money
import java.math.BigDecimal
import java.util.Currency

val EUR = Currency.getInstance("EUR")
val GBP = Currency.getInstance("GBP")
val JPY = Currency.getInstance("JPY")
val JOD = Currency.getInstance("JOD")

fun Money(amount: Int, currency: Currency) =
    Money(amount.toBigDecimal(), currency).toWholeMinorUnits()

fun Money(amount: String, currency: Currency) =
    Money(amount.toBigDecimal(), currency)

val Int.percent get() = MonetaryAmount(BigDecimal(this).divide(100.toBigDecimal()))
