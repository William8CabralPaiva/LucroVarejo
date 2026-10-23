package com.cabral.lucrovarejo.ui.screens.machinelist

import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel

data class MachineListUiState(
    val items: List<PaymentTerminalModel> = emptyList(),
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
)
