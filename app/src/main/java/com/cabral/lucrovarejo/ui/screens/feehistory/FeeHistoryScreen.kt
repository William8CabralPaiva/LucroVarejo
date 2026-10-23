package com.cabral.lucrovarejo.ui.screens.feehistory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Percent
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.TerminalFeeModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.bpsToRate
import com.cabral.lucrovarejo.ui.components.DateTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldType
import com.cabral.lucrovarejo.ui.components.formatSaleDatePtBr
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import java.util.Date

@Composable
fun FeeHistoryScreen(
    slot: FeeSlotKey,
    viewModel: FeeHistoryViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()

    LaunchedEffect(resolvedViewModel, slot) {
        resolvedViewModel.loadSlot(slot)
    }

    FeeHistoryContent(
        state = state,
        onShowAddDialog = resolvedViewModel::onShowAddDialog,
        onDismissAddDialog = resolvedViewModel::onDismissAddDialog,
        onRateChange = resolvedViewModel::onRateChange,
        onDateChange = resolvedViewModel::onDateChange,
        onConfirmAdd = resolvedViewModel::onConfirmAdd,
    )
}

@Composable
fun FeeHistoryContent(
    state: FeeHistoryUiState,
    onShowAddDialog: () -> Unit,
    onDismissAddDialog: () -> Unit,
    onRateChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onConfirmAdd: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onShowAddDialog) {
                Icon(imageVector = Icons.Outlined.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.fee_history_add))
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { CircularProgressIndicator() }
            }

            state.loadError != null -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { Text(text = stringResource(state.loadError)) }
            }

            state.fees.isEmpty() -> {
                Column(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) { Text(text = stringResource(R.string.fee_history_empty)) }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.fees, key = { it.historyKey() }) { fee ->
                        FeeHistoryItem(fee = fee, isCurrent = fee.historyKey() == state.currentFeeKey)
                    }
                }
            }
        }
    }

    if (state.showAddDialog) {
        AddFeeDialog(
            state = state,
            onDismiss = onDismissAddDialog,
            onRateChange = onRateChange,
            onDateChange = onDateChange,
            onConfirm = onConfirmAdd,
        )
    }
}

@Composable
private fun FeeHistoryItem(fee: TerminalFeeModel, isCurrent: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${fee.rateBps.bpsToRate()}%", style = MaterialTheme.typography.titleMedium)
                if (isCurrent) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.fee_history_current_badge)) })
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(
                    R.string.fee_history_effective_from,
                    formatSaleDatePtBr(fee.effectiveFrom.time)
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AddFeeDialog(
    state: FeeHistoryUiState,
    onDismiss: () -> Unit,
    onRateChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fee_history_add)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                IconTextFieldContent(
                    value = state.rateText,
                    onValueChange = onRateChange,
                    labelResId = R.string.terminal_form_fee_rate_label,
                    placeholderResId = R.string.terminal_form_fee_rate_placeholder,
                    icon = Icons.Outlined.Percent,
                    type = IconTextFieldType.MONEY,
                    required = true,
                    isError = state.rateError != null,
                    supportingText = state.rateError?.let { { Text(stringResource(it)) } }
                )
                DateTextFieldContent(
                    dateMillis = state.effectiveFromMillis,
                    onDateChange = onDateChange,
                    labelResId = R.string.terminal_form_fee_effective_from_label,
                    required = true
                )
                state.dateError?.let {
                    Text(text = stringResource(it), color = MaterialTheme.colorScheme.error)
                }
                state.saveError?.let {
                    Text(text = stringResource(it), color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !state.isSaving) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.dialog_cancel)) }
        }
    )
}

@Preview(showBackground = true, name = "Histórico de tarifas")
@Composable
private fun FeeHistoryContentPreview() {
    LucroVarejoTheme {
        FeeHistoryContent(
            state = FeeHistoryUiState(
                method = TerminalPaymentType.CREDIT,
                brandName = "Visa",
                installments = 1,
                fees = listOf(
                    TerminalFeeModel(
                        terminalId = "1",
                        method = TerminalPaymentType.CREDIT,
                        brandId = "1",
                        installments = 1,
                        rateBps = 349,
                        effectiveFrom = Date()
                    )
                ),
                currentFeeKey = "k"
            ),
            onShowAddDialog = {},
            onDismissAddDialog = {},
            onRateChange = {},
            onDateChange = {},
            onConfirmAdd = {},
        )
    }
}
