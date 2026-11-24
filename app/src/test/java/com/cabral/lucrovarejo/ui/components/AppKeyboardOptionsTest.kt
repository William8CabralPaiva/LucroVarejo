package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import org.junit.Assert.assertEquals
import org.junit.Test

class AppKeyboardOptionsTest {

    @Test
    fun commonDefaultsUseSentencesCapitalizationAndTextType() {
        val options = AppKeyboardDefaults.common
        assertEquals(KeyboardCapitalization.Sentences, options.capitalization)
        assertEquals(KeyboardType.Text, options.keyboardType)
    }

    @Test
    fun emailDefaultsDisableCapitalizationAndUseEmailType() {
        val options = AppKeyboardDefaults.email
        assertEquals(KeyboardCapitalization.None, options.capitalization)
        assertEquals(KeyboardType.Email, options.keyboardType)
    }

    @Test
    fun passwordDefaultsDisableCapitalizationAndUsePasswordType() {
        val options = AppKeyboardDefaults.password
        assertEquals(KeyboardCapitalization.None, options.capitalization)
        assertEquals(KeyboardType.Password, options.keyboardType)
    }

    @Test
    fun numberDefaultsDisableCapitalizationAndUseNumberType() {
        val options = AppKeyboardDefaults.number
        assertEquals(KeyboardCapitalization.None, options.capitalization)
        assertEquals(KeyboardType.Number, options.keyboardType)
    }

    @Test
    fun decimalDefaultsDisableCapitalizationAndUseDecimalType() {
        val options = AppKeyboardDefaults.decimal
        assertEquals(KeyboardCapitalization.None, options.capitalization)
        assertEquals(KeyboardType.Decimal, options.keyboardType)
    }

    @Test
    fun defaultCapitalizationForMapsExpectedTypes() {
        assertEquals(KeyboardCapitalization.None, defaultCapitalizationFor(KeyboardType.Email))
        assertEquals(KeyboardCapitalization.None, defaultCapitalizationFor(KeyboardType.Password))
        assertEquals(KeyboardCapitalization.None, defaultCapitalizationFor(KeyboardType.Number))
        assertEquals(KeyboardCapitalization.None, defaultCapitalizationFor(KeyboardType.Decimal))
        assertEquals(KeyboardCapitalization.None, defaultCapitalizationFor(KeyboardType.Phone))
        assertEquals(KeyboardCapitalization.None, defaultCapitalizationFor(KeyboardType.NumberPassword))

        assertEquals(KeyboardCapitalization.Sentences, defaultCapitalizationFor(KeyboardType.Text))
        assertEquals(KeyboardCapitalization.Sentences, defaultCapitalizationFor(KeyboardType.Ascii))
        assertEquals(KeyboardCapitalization.Sentences, defaultCapitalizationFor(KeyboardType.Uri))
    }

    @Test
    fun appKeyboardOptionsAppliesDefaultCapitalizationBasedOnType() {
        val textOptions = appKeyboardOptions(keyboardType = KeyboardType.Text)
        assertEquals(KeyboardCapitalization.Sentences, textOptions.capitalization)
        assertEquals(KeyboardType.Text, textOptions.keyboardType)

        val emailOptions = appKeyboardOptions(keyboardType = KeyboardType.Email)
        assertEquals(KeyboardCapitalization.None, emailOptions.capitalization)
        assertEquals(KeyboardType.Email, emailOptions.keyboardType)

        val customOptions = appKeyboardOptions(
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Search
        )
        assertEquals(KeyboardCapitalization.Words, customOptions.capitalization)
        assertEquals(KeyboardType.Text, customOptions.keyboardType)
        assertEquals(ImeAction.Search, customOptions.imeAction)
    }
}
