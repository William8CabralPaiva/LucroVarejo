package com.cabral.lucrovarejo.ui.screens.terminalform

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.CardBrandModel
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.bpsToRate
import com.cabral.lucrovarejo.ui.screens.feehistory.FeeSlotKey
import com.cabral.lucrovarejo.ui.components.DateTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconButton
import com.cabral.lucrovarejo.ui.components.IconTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldType
import com.cabral.lucrovarejo.ui.components.SpinnerContent
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.PointOfSale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerminalFormScreen(
    terminal: PaymentTerminalModel? = null,
    onFinished: () -> Unit = {},
    onOpenFeeHistory: (FeeSlotKey) -> Unit = {},
    viewModel: TerminalFormViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()

    LaunchedEffect(resolvedViewModel, terminal) {
        resolvedViewModel.loadTerminal(terminal)
    }

    TerminalFormContent(
        state = state,
        onNameChange = resolvedViewModel::onNameChange,
        onToggleMethod = resolvedViewModel::onToggleMethod,
        onCashChargesFeeToggle = resolvedViewModel::onCashChargesFeeToggle,
        onToggleDebitBrand = resolvedViewModel::onToggleDebitBrand,
        onToggleCreditBrand = resolvedViewModel::onToggleCreditBrand,
        onAddCreditInstallment = resolvedViewModel::onAddCreditInstallment,
        onFeeRateChange = resolvedViewModel::onFeeRateChange,
        onFeeDateChange = resolvedViewModel::onFeeDateChange,
        onOpenFeeHistory = { entry ->
            val terminalId = state.terminalId ?: return@TerminalFormContent
            onOpenFeeHistory(
                FeeSlotKey(
                    terminalId = terminalId,
                    method = entry.method,
                    brandId = entry.brandId,
                    installments = entry.installments
                )
            )
        },
        onSave = resolvedViewModel::onSave,
    )

    if (state.saved) {
        onFinished()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerminalFormContent(
    state: TerminalFormUiState,
    onNameChange: (String) -> Unit,
    onToggleMethod: (TerminalPaymentType) -> Unit,
    onCashChargesFeeToggle: (Boolean) -> Unit,
    onToggleDebitBrand: (String) -> Unit,
    onToggleCreditBrand: (String) -> Unit,
    onAddCreditInstallment: (String, Int) -> Unit,
    onFeeRateChange: (String, String) -> Unit,
    onFeeDateChange: (String, Long) -> Unit,
    onOpenFeeHistory: (FeeEntryUi) -> Unit = {},
    onSave: () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (!state.isLoading) {
                Column(modifier = Modifier.padding(16.dp)) {
                    state.saveError?.let { error ->
                        Text(
                            text = stringResource(error),
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    IconButton(
                        enabled = state.canSubmit,
                        onClick = onSave,
                        isLoading = state.isSaving,
                        icon = Icons.Default.Save,
                        text = stringResource(R.string.terminal_form_save),
                        loadingText = stringResource(R.string.terminal_form_saving)
                    )
                }
            }
        }
    ) { innerPadding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        val cashEntry = state.feeEntries.firstOrNull { it.method == TerminalPaymentType.CASH }
        val pixEntry = state.feeEntries.firstOrNull { it.method == TerminalPaymentType.PIX }

        val methods = remember {
            listOf(
                TerminalPaymentType.CASH to R.string.terminal_form_method_cash,
                TerminalPaymentType.PIX to R.string.terminal_form_method_pix,
                TerminalPaymentType.DEBIT to R.string.terminal_form_method_debit,
                TerminalPaymentType.CREDIT to R.string.terminal_form_method_credit,
            )
        }
        // Aba apenas navega entre as seções; habilitar/desabilitar o método é feito pelo Switch
        // dentro da própria seção, evitando que mais de uma aba pareça "selecionada" ao mesmo tempo.
        var selectedTabIndex by remember { mutableStateOf(0) }
        val (currentMethod, currentLabelResId) = methods[selectedTabIndex]
        val methodEnabled = state.selectedMethods.contains(currentMethod)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IconTextFieldContent(
                    value = state.name,
                    onValueChange = onNameChange,
                    labelResId = R.string.terminal_form_name_label,
                    placeholderResId = R.string.terminal_form_name_placeholder,
                    icon = Icons.Outlined.PointOfSale,
                    type = IconTextFieldType.TEXT,
                    required = true,
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(stringResource(it)) } }
                )
            }

            item {
                Column {
                    Text(
                        text = stringResource(R.string.terminal_form_methods_label),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SecondaryTabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        methods.forEachIndexed { index, (method, labelResId) ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = { Text(stringResource(labelResId)) }
                            )
                        }
                    }
                    state.methodsError?.let {
                        Text(
                            text = stringResource(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            item {
                SectionCard(title = stringResource(currentLabelResId)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.terminal_form_enable_method),
                            modifier = Modifier.weight(1f)
                        )
                        Switch(checked = methodEnabled, onCheckedChange = { onToggleMethod(currentMethod) })
                    }

                    if (methodEnabled) {
                        Spacer(modifier = Modifier.height(8.dp))
                        when (currentMethod) {
                            TerminalPaymentType.CASH -> {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = stringResource(R.string.terminal_form_cash_charges_fee),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Switch(checked = state.cashChargesFee, onCheckedChange = onCashChargesFeeToggle)
                                }
                                if (state.cashChargesFee && cashEntry != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    FeeEntryFields(
                                        entry = cashEntry,
                                        onRateChange = { onFeeRateChange(cashEntry.id, it) },
                                        onDateChange = { onFeeDateChange(cashEntry.id, it) },
                                        onOpenHistory = { onOpenFeeHistory(cashEntry) }
                                    )
                                }
                            }

                            TerminalPaymentType.PIX -> {
                                pixEntry?.let { entry ->
                                    FeeEntryFields(
                                        entry = entry,
                                        onRateChange = { onFeeRateChange(entry.id, it) },
                                        onDateChange = { onFeeDateChange(entry.id, it) },
                                        onOpenHistory = { onOpenFeeHistory(entry) }
                                    )
                                }
                            }

                            TerminalPaymentType.DEBIT -> {
                                Text(
                                    text = stringResource(R.string.terminal_form_brands_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.availableBrands.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.terminal_form_brands_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        state.availableBrands.forEach { brand ->
                                            BrandChip(
                                                brand = brand,
                                                selected = brand.key in state.selectedDebitBrandIds,
                                                onToggle = { brand.key?.let(onToggleDebitBrand) }
                                            )
                                        }
                                    }
                                    state.selectedDebitBrandIds.forEach { brandId ->
                                        val brandName =
                                            state.availableBrands.firstOrNull { it.key == brandId }?.name.orEmpty()
                                        val entry = state.feeEntries.firstOrNull {
                                            it.method == TerminalPaymentType.DEBIT && it.brandId == brandId
                                        }
                                        if (entry != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            NestedBrandBlock(title = brandName) {
                                                FeeEntryFields(
                                                    entry = entry,
                                                    onRateChange = { onFeeRateChange(entry.id, it) },
                                                    onDateChange = { onFeeDateChange(entry.id, it) },
                                                    onOpenHistory = { onOpenFeeHistory(entry) }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            TerminalPaymentType.CREDIT -> {
                                Text(
                                    text = stringResource(R.string.terminal_form_brands_label),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                if (state.availableBrands.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.terminal_form_brands_empty),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        state.availableBrands.forEach { brand ->
                                            BrandChip(
                                                brand = brand,
                                                selected = brand.key in state.selectedCreditBrandIds,
                                                onToggle = { brand.key?.let(onToggleCreditBrand) }
                                            )
                                        }
                                    }
                                    state.selectedCreditBrandIds.forEach { brandId ->
                                        val brandName =
                                            state.availableBrands.firstOrNull { it.key == brandId }?.name.orEmpty()
                                        Spacer(modifier = Modifier.height(8.dp))
                                        NestedBrandBlock(title = brandName) {
                                            CreditInstallmentPicker(
                                                brandId = brandId,
                                                configured = state.creditInstallmentsByBrand[brandId].orEmpty(),
                                                onAdd = onAddCreditInstallment
                                            )
                                            state.creditInstallmentsByBrand[brandId].orEmpty().forEach { installment ->
                                                val entry = state.feeEntries.firstOrNull {
                                                    it.method == TerminalPaymentType.CREDIT &&
                                                        it.brandId == brandId &&
                                                        it.installments == installment
                                                }
                                                if (entry != null) {
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(
                                                        text = "${installment}x",
                                                        style = MaterialTheme.typography.labelLarge
                                                    )
                                                    FeeEntryFields(
                                                        entry = entry,
                                                        onRateChange = { onFeeRateChange(entry.id, it) },
                                                        onDateChange = { onFeeDateChange(entry.id, it) },
                                                        onOpenHistory = { onOpenFeeHistory(entry) }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun NestedBrandBlock(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            content()
        }
    }
}

@Composable
private fun BrandChip(
    brand: CardBrandModel,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick = onToggle,
        label = { Text(brand.name) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.height(18.dp)) }
        } else null
    )
}

@Composable
private fun CreditInstallmentPicker(
    brandId: String,
    configured: List<Int>,
    onAdd: (String, Int) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val availableInstallments = (1..12).filter { it !in configured }.map { "${it}x" }

    Column {
        if (configured.isNotEmpty()) {
            Text(
                text = stringResource(R.string.terminal_form_installments_label) + ": " +
                    configured.joinToString(", ") { "${it}x" },
                style = MaterialTheme.typography.bodySmall
            )
        }
        if (showPicker && availableInstallments.isNotEmpty()) {
            SpinnerContent(
                selectedItem = null,
                items = availableInstallments,
                onItemSelected = { selected ->
                    val installment = selected.removeSuffix("x").toIntOrNull()
                    if (installment != null) onAdd(brandId, installment)
                    showPicker = false
                },
                textId = R.string.terminal_form_installments_label,
                placeholderResId = R.string.terminal_form_installments_placeholder
            )
        } else if (availableInstallments.isNotEmpty()) {
            TextButton(onClick = { showPicker = true }) {
                Text(stringResource(R.string.terminal_form_add_installment))
            }
        }
    }
}

/** Campos de tarifa (%) + vigência de um slot, sem Card próprio (usado aninhado dentro das seções). */
@Composable
private fun FeeEntryFields(
    entry: FeeEntryUi,
    onRateChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onOpenHistory: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(
                if (entry.currentRateBps != null) R.string.terminal_form_current_fee
                else R.string.terminal_form_no_fee,
                entry.currentRateBps?.let { "${it.bpsToRate()}%" } ?: ""
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconTextFieldContent(
            value = entry.rateText,
            onValueChange = onRateChange,
            labelResId = R.string.terminal_form_fee_rate_label,
            placeholderResId = R.string.terminal_form_fee_rate_placeholder,
            icon = Icons.Outlined.Percent,
            type = IconTextFieldType.MONEY,
            required = false,
            isError = entry.error != null,
            supportingText = entry.error?.let { { Text(stringResource(it)) } }
        )
        DateTextFieldContent(
            dateMillis = entry.effectiveFromMillis,
            onDateChange = onDateChange,
            labelResId = R.string.terminal_form_fee_effective_from_label,
            required = false
        )
        if (entry.currentRateBps != null) {
            TextButton(onClick = onOpenHistory) {
                Text(stringResource(R.string.terminal_form_history))
            }
        }
    }
}

@Preview(showBackground = true, name = "Form maquininha")
@Composable
private fun TerminalFormContentPreview() {
    LucroVarejoTheme {
        TerminalFormContent(
            state = TerminalFormUiState(
                name = "Maquininha Mercado Pago",
                selectedMethods = setOf(TerminalPaymentType.PIX, TerminalPaymentType.CREDIT),
                availableBrands = listOf(CardBrandModel(key = "1", name = "Visa")),
                selectedCreditBrandIds = setOf("1"),
                creditInstallmentsByBrand = mapOf("1" to listOf(1, 2)),
                feeEntries = listOf(
                    FeeEntryUi(method = TerminalPaymentType.PIX),
                    FeeEntryUi(method = TerminalPaymentType.CREDIT, brandId = "1", brandName = "Visa", installments = 1)
                )
            ),
            onNameChange = {},
            onToggleMethod = {},
            onCashChargesFeeToggle = {},
            onToggleDebitBrand = {},
            onToggleCreditBrand = {},
            onAddCreditInstallment = { _, _ -> },
            onFeeRateChange = { _, _ -> },
            onFeeDateChange = { _, _ -> },
            onSave = {}
        )
    }
}
