package com.cabral.lucrovarejo.ui.components

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadingButtonTest {
    @Test
    fun buttonIsEnabledOnlyWhenEnabledAndNotLoading() {
        assertTrue(isLoadingButtonEnabled(enabled = true, isLoading = false))
        assertFalse(isLoadingButtonEnabled(enabled = true, isLoading = true))
        assertFalse(isLoadingButtonEnabled(enabled = false, isLoading = false))
        assertFalse(isLoadingButtonEnabled(enabled = false, isLoading = true))
    }
}
