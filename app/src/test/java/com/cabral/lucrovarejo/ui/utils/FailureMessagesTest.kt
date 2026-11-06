package com.cabral.lucrovarejo.ui.utils

import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.common.DataFailure
import org.junit.Assert.assertEquals
import org.junit.Test

class FailureMessagesTest {
    private val fallback = R.string.purchase_create_error_generic

    @Test
    fun mapsEachReasonToFriendlyMessage() {
        assertEquals(R.string.error_network, DataFailure(DataFailure.Reason.NETWORK).userMessageRes(fallback))
        assertEquals(
            R.string.error_permission,
            DataFailure(DataFailure.Reason.PERMISSION_DENIED).userMessageRes(fallback)
        )
        assertEquals(
            R.string.error_unauthenticated,
            DataFailure(DataFailure.Reason.UNAUTHENTICATED).userMessageRes(fallback)
        )
        assertEquals(
            R.string.terminal_form_tariff_duplicate_date,
            DataFailure(DataFailure.Reason.DUPLICATE_EFFECTIVE_DATE).userMessageRes(fallback)
        )
    }

    @Test
    fun usesFallbackForUnknownNullAndForeignErrors() {
        assertEquals(fallback, DataFailure(DataFailure.Reason.UNKNOWN).userMessageRes(fallback))
        assertEquals(fallback, IllegalStateException("x").userMessageRes(fallback))
        assertEquals(fallback, (null as Throwable?).userMessageRes(fallback))
    }
}
