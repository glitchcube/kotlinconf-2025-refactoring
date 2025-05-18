package com.example.auction

import com.example.auction.model.Money
import java.math.BigDecimal
import java.math.RoundingMode.DOWN
import java.util.Currency
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull


class MoneyTest {
    @Test
    fun `can parse positive money value`() {
        val money = Money(BigDecimal("123.45"), Currency.getInstance("EUR"))
        
        assertEquals("123.45 EUR", money.toString())
        assertEquals(money, Money.parse("123.45 EUR"))
    }
    
    @Test
    fun `can parse negative money value`() {
        val money = Money(BigDecimal("-123.45"), Currency.getInstance("EUR"))
        
        assertEquals("-123.45 EUR", money.toString())
        assertEquals(money, Money.parse("-123.45 EUR"))
    }
    
    @Test
    fun `can parse integral money value`() {
        val money = Money(BigDecimal("123"), Currency.getInstance("EUR"))
        
        assertEquals("123 EUR", money.toString())
        assertEquals(money, Money.parse("123 EUR"))
    }
    
    @Test
    fun `numerical equality`() {
        assertNotEquals(BigDecimal("1"), BigDecimal("1.00"))
        assertEquals(Money("1", EUR), Money("1.00", EUR))
    }
    
    @Test
    fun `returns null on malformed money value`() {
        assertNull(Money.parse("123.45EUR"))
        assertNull(Money.parse("xyz EUR"))
        assertNull(Money.parse("123.45"))
        assertNull(Money.parse("123.45 KJASHDHKA"))
        assertNull(Money.parse("123.45 E"))
        assertNull(Money.parse("123.45 123"))
        assertNull(Money.parse("123.45 123"))
        assertNull(Money.parse("123.45 XYZ"))
    }
    
    @Test
    fun `round to nearest whole minor currency unit`() {
        assertEquals(
            Money(BigDecimal("123.00"), EUR),
            Money(BigDecimal("123"), EUR).toWholeMinorUnits(DOWN)
        )
        assertEquals(
            Money(BigDecimal("123.40"), EUR),
            Money(BigDecimal("123.4"), EUR).toWholeMinorUnits(DOWN)
        )
        assertEquals(
            Money(BigDecimal("123.45"), EUR),
            Money(BigDecimal("123.4567"), EUR).toWholeMinorUnits(DOWN)
        )
        
        assertEquals(
            Money(BigDecimal("123"), JPY),
            Money(BigDecimal("123.4567"), JPY).toWholeMinorUnits(DOWN)
        )
        
        assertEquals(
            Money(BigDecimal("123.450"), JOD),
            Money(BigDecimal("123.45"), JOD).toWholeMinorUnits(DOWN)
        )
        assertEquals(
            Money(BigDecimal("123.456"), JOD),
            Money(BigDecimal("123.4567"), JOD).toWholeMinorUnits(DOWN)
        )
    }
}