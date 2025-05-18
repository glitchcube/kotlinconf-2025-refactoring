package com.example.auction.model

import java.math.BigDecimal
import java.math.RoundingMode

class MonetaryAmount(val repr: BigDecimal) : Comparable<MonetaryAmount> {
    constructor(repr: String) : this(BigDecimal(repr))
    constructor(value: Int) : this(BigDecimal(value))
    
    override fun toString() = repr.toString()
    
    override fun equals(other: Any?) =
        other is MonetaryAmount
            && repr.compareTo(other.repr) == 0
    
    override fun hashCode() = repr.stripTrailingZeros().hashCode()
    
    override fun compareTo(other: MonetaryAmount) = repr.compareTo(other.repr)
    
    fun withScale(defaultFractionDigits: Int, roundingMode: RoundingMode) =
        MonetaryAmount(repr = repr.setScale(defaultFractionDigits, roundingMode))
    
    operator fun plus(that: MonetaryAmount) =
        MonetaryAmount(this.repr + that.repr)
    
    operator fun minus(that: MonetaryAmount) =
        MonetaryAmount(this.repr - that.repr)
    
    operator fun times(that: MonetaryAmount) =
        MonetaryAmount(this.repr * that.repr)
    
    operator fun times(n: Int) =
        MonetaryAmount(this.repr * n.toBigDecimal())
    
    companion object {
        val ZERO = MonetaryAmount(BigDecimal.ZERO)
    }
}

operator fun Int.times(that: MonetaryAmount) = that * this