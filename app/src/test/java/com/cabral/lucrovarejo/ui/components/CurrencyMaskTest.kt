package com.cabral.lucrovarejo.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyMaskTest {
    private fun type(text: String, cursor: Int = text.length) = applyCurrencyInput(text, cursor)

    @Test
    fun typingFiftyShowsFiftyWithZeroCents() {
        val five = type("5")
        assertEquals("5,00", five.display)
        assertEquals("5.00", five.plain)
        assertEquals(1, five.cursor)

        // "0" digitado depois do 5, com o cursor antes da vírgula
        val fifty = type("50,00", cursor = 2)
        assertEquals("50,00", fifty.display)
        assertEquals("50.00", fifty.plain)
        assertEquals(2, fifty.cursor)
    }

    @Test
    fun separatorMovesCursorToCents() {
        val result = type("50,,00", cursor = 3)
        assertEquals("50,00", result.display)
        assertEquals(3, result.cursor)

        val onEmpty = type(",")
        assertEquals("0,00", onEmpty.display)
        assertEquals(2, onEmpty.cursor)
    }

    @Test
    fun centsAreOverwrittenUpToTwoDigits() {
        val first = type("50,500", cursor = 4)
        assertEquals("50,50", first.display)
        assertEquals(4, first.cursor)

        val second = type("50,550", cursor = 5)
        assertEquals("50,55", second.display)
        assertEquals("50.55", second.plain)
        assertEquals(5, second.cursor)
    }

    @Test
    fun leadingZerosAreRemoved() {
        val zero = type("0")
        assertEquals("0,00", zero.display)
        assertEquals(1, zero.cursor)

        val result = type("05,00", cursor = 2)
        assertEquals("5,00", result.display)
        assertEquals(1, result.cursor)
    }

    @Test
    fun deletingEverythingClearsField() {
        assertEquals("", type("").plain)
        assertEquals("", type(",00", cursor = 0).plain)
        assertEquals("", type("abc").plain)
    }

    @Test
    fun integerPartIsLimitedToTenDigits() {
        assertEquals("9999999999,99", type("9".repeat(15) + ",99").display)
    }

    @Test
    fun plainValueIsDisplayedWithTwoDecimals() {
        assertEquals("89,90", plainToCurrencyDisplay("89.9"))
        assertEquals("50,00", plainToCurrencyDisplay("50"))
        assertEquals("1234,56", plainToCurrencyDisplay("1234.56"))
        assertEquals("", plainToCurrencyDisplay(""))
        assertEquals("", plainToCurrencyDisplay("x"))
    }
}
