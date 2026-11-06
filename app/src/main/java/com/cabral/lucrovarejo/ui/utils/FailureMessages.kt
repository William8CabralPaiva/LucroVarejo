package com.cabral.lucrovarejo.ui.utils

import androidx.annotation.StringRes
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.common.DataFailure

/**
 * Mensagem amigável pronta para qualquer falha de dados. Rede, permissão, sessão e data
 * duplicada têm texto único; só o caso desconhecido usa [fallback], específico da ação.
 */
@StringRes
fun Throwable?.userMessageRes(@StringRes fallback: Int): Int =
    when ((this as? DataFailure)?.reason) {
        DataFailure.Reason.NETWORK -> R.string.error_network
        DataFailure.Reason.PERMISSION_DENIED -> R.string.error_permission
        DataFailure.Reason.UNAUTHENTICATED -> R.string.error_unauthenticated
        DataFailure.Reason.DUPLICATE_EFFECTIVE_DATE -> R.string.terminal_form_tariff_duplicate_date
        DataFailure.Reason.UNKNOWN, null -> fallback
    }
