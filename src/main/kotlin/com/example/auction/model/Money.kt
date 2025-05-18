package com.example.auction.model

import java.math.BigDecimal
import java.math.RoundingMode
import java.math.RoundingMode.UNNECESSARY
import java.util.Currency

data class Money(val amount: MonetaryAmount, val currency: Currency) {
    constructor(amount: BigDecimal, currency: Currency) :
        this(MonetaryAmount(amount), currency)
    
    override fun toString() = "$amount ${currency.currencyCode}"
    
    fun toWholeMinorUnits(roundingMode: RoundingMode = UNNECESSARY) =
        copy(amount = amount.withScale(currency.defaultFractionDigits, roundingMode))
    
    companion object {
        private val format = Regex("""^(-?\d+(?:\.\d+)?)\s+([A-Z]{3})$""")
        
        fun parse(str: String): Money? =
            format.matchEntire(str)?.groupValues
                ?.let { groups ->
                    try {
                        Money(BigDecimal(groups[1]), Currency.getInstance(groups[2]))
                    } catch (e: IllegalArgumentException) {
                        null
                    }
                }
    }
}
