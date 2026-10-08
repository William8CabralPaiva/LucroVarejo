package com.cabral.lucrovarejo.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import org.junit.Test

class PasswordTextFieldStateTest {
    @Test
    fun hiddenPasswordUsesPasswordTransformation() {
        assertTrue(passwordVisualTransformation(false) is PasswordVisualTransformation)
    }

    @Test
    fun visiblePasswordUsesNoTransformation() {
        assertFalse(passwordVisualTransformation(true) is PasswordVisualTransformation)
        assertTrue(passwordVisualTransformation(true) === VisualTransformation.None)
    }
}
