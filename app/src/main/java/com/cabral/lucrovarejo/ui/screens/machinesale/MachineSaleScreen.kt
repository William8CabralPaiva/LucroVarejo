package com.cabral.lucrovarejo.ui.screens.machinesale

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.domain.payment.TerminalPaymentType
import com.cabral.lucrovarejo.domain.payment.bpsToRate
import com.cabral.lucrovarejo.ui.screens.machinehistory.PaymentMachineHistoryArgs
import com.cabral.lucrovarejo.ui.components.AppSwitch
import com.cabral.lucrovarejo.ui.components.CoachMarkOverlay
import com.cabral.lucrovarejo.ui.components.CoachStep
import com.cabral.lucrovarejo.ui.components.CoachTarget
import com.cabral.lucrovarejo.ui.components.coachTarget
import com.cabral.lucrovarejo.ui.components.rememberCoachMarkState
import com.cabral.lucrovarejo.ui.components.DateTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconButton
import com.cabral.lucrovarejo.ui.components.IconTextFieldContent
import com.cabral.lucrovarejo.ui.components.IconTextFieldType
import com.cabral.lucrovarejo.ui.components.SpinnerContent
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Percent
import androidx.compose.material.icons.outlined.PointOfSale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachineSaleScreen(
    terminal: PaymentTerminalModel? = null,
    onFinish: () -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onOpenMachineHistory: (PaymentMachineHistoryArgs) -> Unit = {},
    viewModel: MachineSaleViewModel? = null,
    tourActive: Boolean = false,
    onTourFinish: () -> Unit = {},
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()
    val savedMessage = stringResource(R.string.terminal_form_saved)
    val currentOnShowMessage by rememberUpdatedState(onShowMessage)
    val currentOnFinish by rememberUpdatedState(onFinish)

    LaunchedEffect(resolvedViewModel, terminal) {
        resolvedViewModel.loadTerminal(terminal)
    }

    LaunchedEffect(resolvedViewModel, savedMessage) {
        resolvedViewModel.saved.collect { currentOnShowMessage(savedMessage) }
    }

    LaunchedEffect(resolvedViewModel) {
        resolvedViewModel.completions.collect { currentOnFinish() }
    }

    MachineSaleContent(
        state = state,
        onNameChange = resolvedViewModel::onNameChange,
        onToggleMethod = resolvedViewModel::onToggleMethod,
        onCashChargesTariffToggle = resolvedViewModel::onCashChargesTariffToggle,
        onAddBrand = resolvedViewModel::onAddBrand,
        onToggleDebitBrand = resolvedViewModel::onToggleDebitBrand,
        onToggleCreditBrand = resolvedViewModel::onToggleCreditBrand,
        onAddCreditInstallment = resolvedViewModel::onAddCreditInstallment,
        onTariffRateChange = resolvedViewModel::onTariffRateChange,
        onTariffDateChange = resolvedViewModel::onTariffDateChange,
        onOpenMachineHistory = { entry ->
            val terminalId = state.terminalId ?: return@MachineSaleContent
            onOpenMachineHistory(
                PaymentMachineHistoryArgs(
                    terminalId = terminalId,
                    method = entry.method,
                    brandId = entry.brandId,
                    installments = entry.installments
                )
            )
        },
        onSave = resolvedViewModel::onSave,
        tourActive = tourActive,
        onTourFinish = onTourFinish,
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MachineSaleContent(
    state: MachineSaleUiState,
    onNameChange: (String) -> Unit,
    onToggleMethod: (TerminalPaymentType) -> Unit,
    onCashChargesTariffToggle: (Boolean) -> Unit,
    onAddBrand: (String) -> Unit,
    onToggleDebitBrand: (String) -> Unit,
    onToggleCreditBrand: (String) -> Unit,
    onAddCreditInstallment: (String, Int) -> Unit,
    onTariffRateChange: (String, String) -> Unit,
    onTariffDateChange: (String, Long) -> Unit,
    onSave: (closeAfter: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    onOpenMachineHistory: (TariffEntryUi) -> Unit = {},
    tourActive: Boolean = false,
    onTourFinish: () -> Unit = {},
) {
    val methods = remember {
        listOf(
            TerminalPaymentType.CASH to R.string.terminal_form_method_cash,
            TerminalPaymentType.PIX to R.string.terminal_form_method_pix,
            TerminalPaymentType.DEBIT to R.string.terminal_form_method_debit,
            TerminalPaymentType.CREDIT to R.string.terminal_form_method_credit,
        )
    }
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val (currentMethod, currentLabelResId) = methods[selectedTabIndex]

    val coachState = rememberCoachMarkState()
    val coach = if (tourActive) coachState else null
    var tourSaveStage by remember(tourActive) { mutableStateOf(false) }
    var brandDraft by remember { mutableStateOf("") }
    var tourNameDone by remember(tourActive) { mutableStateOf(false) }
    val tourBrand = state.selectedCreditBrandIds.firstOrNull()
    val tourInstallment = tourBrand?.let { state.creditInstallmentsByBrand[it].orEmpty().firstOrNull() }
    val tourTarget = when {
        !tourActive || state.isLoading -> null
        !tourNameDone -> CoachTarget.NAME_FIELD
        selectedTabIndex != CREDIT_TAB_INDEX -> CoachTarget.CREDIT_TAB
        !state.selectedMethods.contains(TerminalPaymentType.CREDIT) -> CoachTarget.ENABLE_SWITCH
        state.availableBrands.isEmpty() -> CoachTarget.BRAND_FIELD
        tourBrand == null -> CoachTarget.BRAND_CHIP
        tourInstallment == null -> CoachTarget.ADD_INSTALLMENT
        !tourSaveStage -> CoachTarget.INSTALLMENT_RATE
        else -> CoachTarget.SAVE
    }

    Box(modifier = modifier) {
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
                            modifier = Modifier.coachTarget(coach, CoachTarget.SAVE),
                            enabled = state.canSubmit && !state.isSaving,
                            onClick = { onSave(false) },
                            isLoading = state.isSaving && !state.closeAfterSave,
                            icon = Icons.Default.Save,
                            text = stringResource(R.string.terminal_form_save),
                            loadingText = stringResource(R.string.terminal_form_saving)
                        )
                        IconButton(
                            enabled = state.canSubmit && !state.isSaving,
                            onClick = { onSave(true) },
                            modifier = Modifier.padding(top = 8.dp),
                            isLoading = state.isSaving && state.closeAfterSave,
                            isBorderButton = true,
                            icon = Icons.Default.Save,
                            text = stringResource(R.string.terminal_form_save_and_back),
                            loadingText = stringResource(R.string.terminal_form_saving),
                            testTag = SAVE_AND_BACK_BUTTON_TAG
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

            val cashEntry = state.tariffEntries.firstOrNull { it.method == TerminalPaymentType.CASH }
            val pixEntry = state.tariffEntries.firstOrNull { it.method == TerminalPaymentType.PIX }

            val methodEnabled = state.selectedMethods.contains(currentMethod)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(modifier = Modifier.coachTarget(coach, CoachTarget.NAME_FIELD)) {
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
                                    modifier = if (index == CREDIT_TAB_INDEX) {
                                        Modifier.coachTarget(coach, CoachTarget.CREDIT_TAB)
                                    } else Modifier,
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
                        Row(
                            modifier = if (currentMethod == TerminalPaymentType.CREDIT) {
                                Modifier.coachTarget(coach, CoachTarget.ENABLE_SWITCH)
                            } else Modifier,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.terminal_form_enable_method),
                                modifier = Modifier.weight(1f)
                            )
                            AppSwitch(checked = methodEnabled, onCheckedChange = { onToggleMethod(currentMethod) })
                        }

                        if (methodEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            when (currentMethod) {
                                TerminalPaymentType.CASH -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = stringResource(
                                                R.string.terminal_form_cash_charges_tariff
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                        AppSwitch(
                                            checked = state.cashChargesTariff,
                                            onCheckedChange = onCashChargesTariffToggle
                                        )
                                    }
                                    if (state.cashChargesTariff && cashEntry != null) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TariffEntryFields(
                                            entry = cashEntry,
                                            onRateChange = { onTariffRateChange(cashEntry.id, it) },
                                            onDateChange = { onTariffDateChange(cashEntry.id, it) },
                                            onOpenHistory = { onOpenMachineHistory(cashEntry) }
                                        )
                                    }
                                }

                                TerminalPaymentType.PIX -> {
                                    pixEntry?.let { entry ->
                                        TariffEntryFields(
                                            entry = entry,
                                            onRateChange = { onTariffRateChange(entry.id, it) },
                                            onDateChange = { onTariffDateChange(entry.id, it) },
                                            onOpenHistory = { onOpenMachineHistory(entry) }
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
                                    BrandAddRow(
                                        name = brandDraft,
                                        onNameChange = { brandDraft = it },
                                        onAddBrand = onAddBrand
                                    )
                                    if (state.availableBrands.isEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.terminal_form_brands_empty),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        // Ao selecionar uma bandeira, rola a tela atÃ© o card dela.
                                        val debitBringIntoView = remember(state.availableBrands) {
                                            state.availableBrands.associateWith { BringIntoViewRequester() }
                                        }
                                        var debitPendingScroll by remember { mutableStateOf<String?>(null) }
                                        LaunchedEffect(state.selectedDebitBrandIds) {
                                            val target = debitPendingScroll
                                            if (target != null && target in state.selectedDebitBrandIds) {
                                                debitBringIntoView[target]?.bringIntoView()
                                                debitPendingScroll = null
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            state.availableBrands.forEach { brand ->
                                                BrandChip(
                                                    name = brand,
                                                    selected = brand in state.selectedDebitBrandIds,
                                                    onToggle = {
                                                        val willSelect = brand !in state.selectedDebitBrandIds
                                                        if (willSelect) debitPendingScroll = brand
                                                        onToggleDebitBrand(brand)
                                                    }
                                                )
                                            }
                                        }
                                        state.selectedDebitBrandIds.forEach { brandId ->
                                            val entry = state.tariffEntries.firstOrNull {
                                                it.method == TerminalPaymentType.DEBIT && it.brandId == brandId
                                            }
                                            if (entry != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                NestedBrandBlock(
                                                    title = brandId,
                                                    modifier = debitBringIntoView[brandId]?.let {
                                                        Modifier.bringIntoViewRequester(it)
                                                    } ?: Modifier
                                                ) {
                                                    TariffEntryFields(
                                                        entry = entry,
                                                        onRateChange = { onTariffRateChange(entry.id, it) },
                                                        onDateChange = { onTariffDateChange(entry.id, it) },
                                                        onOpenHistory = { onOpenMachineHistory(entry) }
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
                                    BrandAddRow(
                                        name = brandDraft,
                                        onNameChange = { brandDraft = it },
                                        onAddBrand = onAddBrand,
                                        modifier = Modifier.coachTarget(coach, CoachTarget.BRAND_FIELD)
                                    )
                                    if (state.availableBrands.isEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = stringResource(R.string.terminal_form_brands_empty),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        // Ao selecionar uma bandeira, rola a tela atÃ© o card dela.
                                        val creditBringIntoView = remember(state.availableBrands) {
                                            state.availableBrands.associateWith { BringIntoViewRequester() }
                                        }
                                        var creditPendingScroll by remember { mutableStateOf<String?>(null) }
                                        LaunchedEffect(state.selectedCreditBrandIds) {
                                            val target = creditPendingScroll
                                            if (target != null && target in state.selectedCreditBrandIds) {
                                                creditBringIntoView[target]?.bringIntoView()
                                                creditPendingScroll = null
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            state.availableBrands.forEachIndexed { index, brand ->
                                                BrandChip(
                                                    modifier = if (index == 0) {
                                                        Modifier.coachTarget(coach, CoachTarget.BRAND_CHIP)
                                                    } else Modifier,
                                                    name = brand,
                                                    selected = brand in state.selectedCreditBrandIds,
                                                    onToggle = {
                                                        val willSelect = brand !in state.selectedCreditBrandIds
                                                        if (willSelect) creditPendingScroll = brand
                                                        onToggleCreditBrand(brand)
                                                    }
                                                )
                                            }
                                        }
                                        state.selectedCreditBrandIds.forEach { brandId ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            NestedBrandBlock(
                                                title = brandId,
                                                modifier = creditBringIntoView[brandId]?.let {
                                                    Modifier.bringIntoViewRequester(it)
                                                } ?: Modifier
                                            ) {
                                                CreditInstallmentPicker(
                                                    modifier = if (brandId == tourBrand) {
                                                        Modifier.coachTarget(coach, CoachTarget.ADD_INSTALLMENT)
                                                    } else Modifier,
                                                    brandId = brandId,
                                                    configured = state.creditInstallmentsByBrand[brandId]
                                            .orEmpty()
                                            .toImmutableList(),
                                                    onAdd = onAddCreditInstallment
                                                )
                                                state.creditInstallmentsByBrand[brandId].orEmpty().forEach { installment ->
                                                    val entry = state.tariffEntries.firstOrNull {
                                                        it.method == TerminalPaymentType.CREDIT &&
                                                            it.brandId == brandId &&
                                                            it.installments == installment
                                                    }
                                                    if (entry != null) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        Text(
                                                            text = stringResource(
                                                                R.string.terminal_form_installment_title,
                                                                installment,
                                                                brandId
                                                            ),
                                                            style = MaterialTheme.typography.titleMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        TariffEntryFields(
                                                            modifier = if (brandId == tourBrand && installment == tourInstallment) {
                                                                Modifier.coachTarget(coach, CoachTarget.INSTALLMENT_RATE)
                                                            } else Modifier,
                                                            entry = entry,
                                                            onRateChange = { onTariffRateChange(entry.id, it) },
                                                            onDateChange = { onTariffDateChange(entry.id, it) },
                                                            onOpenHistory = { onOpenMachineHistory(entry) }
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
        if (tourTarget != null) {
            CoachMarkOverlay(
                state = coachState,
                step = coachStepFor(tourTarget, brandDraft.isNotBlank()),
                onNext = {
                    when (tourTarget) {
                        CoachTarget.NAME_FIELD -> tourNameDone = true
                        CoachTarget.BRAND_FIELD -> {
                            onAddBrand(brandDraft)
                            brandDraft = ""
                        }
                        CoachTarget.CREDIT_TAB -> selectedTabIndex = CREDIT_TAB_INDEX
                        CoachTarget.ENABLE_SWITCH -> onToggleMethod(TerminalPaymentType.CREDIT)
                        CoachTarget.BRAND_CHIP -> state.availableBrands.firstOrNull()?.let(onToggleCreditBrand)
                        CoachTarget.ADD_INSTALLMENT -> tourBrand?.let { onAddCreditInstallment(it, 1) }
                        CoachTarget.SAVE -> onTourFinish()
                        else -> tourSaveStage = true
                    }
                },
                onSkip = onTourFinish
            )
        }
    }
}

private fun coachStepFor(target: CoachTarget, brandDraftFilled: Boolean): CoachStep {
    val (title, text) = when (target) {
        CoachTarget.NAME_FIELD -> R.string.coach_tour_name_title to R.string.coach_tour_name_text
        CoachTarget.CREDIT_TAB -> R.string.coach_tour_credit_tab_title to R.string.coach_tour_credit_tab_text
        CoachTarget.ENABLE_SWITCH -> R.string.coach_tour_enable_title to R.string.coach_tour_enable_text
        CoachTarget.BRAND_FIELD -> R.string.coach_tour_brand_field_title to R.string.coach_tour_brand_field_text
        CoachTarget.BRAND_CHIP -> R.string.coach_tour_brand_chip_title to R.string.coach_tour_brand_chip_text
        CoachTarget.ADD_INSTALLMENT -> R.string.coach_tour_installment_title to R.string.coach_tour_installment_text
        CoachTarget.INSTALLMENT_RATE -> R.string.coach_tour_rate_title to R.string.coach_tour_rate_text
        CoachTarget.SAVE -> R.string.coach_tour_save_title to R.string.coach_tour_save_text
    }
    val nextLabel = when (target) {
        CoachTarget.NAME_FIELD, CoachTarget.BRAND_FIELD, CoachTarget.CREDIT_TAB, CoachTarget.ENABLE_SWITCH,
        CoachTarget.BRAND_CHIP, CoachTarget.ADD_INSTALLMENT,
        CoachTarget.INSTALLMENT_RATE -> R.string.coach_mark_next
        CoachTarget.SAVE -> R.string.coach_mark_done
        else -> null
    }
    return CoachStep(target, title, text, target.ordinal + 1, CoachTarget.entries.size, nextLabel,
        nextEnabled = target != CoachTarget.BRAND_FIELD || brandDraftFilled)
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
private fun NestedBrandBlock(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
private fun BrandAddRow(
    name: String,
    onNameChange: (String) -> Unit,
    onAddBrand: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        IconTextFieldContent(
            value = name,
            onValueChange = onNameChange,
            labelResId = R.string.terminal_form_brand_name_label,
            placeholderResId = R.string.terminal_form_brand_name_placeholder,
            icon = Icons.Outlined.CreditCard,
            type = IconTextFieldType.TEXT,
            required = false
        )
        TextButton(
            onClick = {
                onAddBrand(name)
                onNameChange("")
            },
            enabled = name.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.terminal_form_add_brand))
        }
    }
}

@Composable
private fun BrandChip(
    modifier: Modifier = Modifier,
    name: String,
    selected: Boolean,
    onToggle: () -> Unit,
) {
    FilterChip(
        modifier = modifier,
        selected = selected,
        onClick = onToggle,
        label = { Text(name) },
        leadingIcon = if (selected) {
            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.height(18.dp)) }
        } else null
    )
}

@Composable
private fun CreditInstallmentPicker(
    modifier: Modifier = Modifier,
    brandId: String,
    configured: ImmutableList<Int>,
    onAdd: (String, Int) -> Unit,
) {
    var showPicker by remember { mutableStateOf(false) }
    val availableInstallments = (1..MAX_INSTALLMENTS)
        .filter { it !in configured }
        .map { "${it}x" }
        .toImmutableList()

    Column(modifier = modifier) {
        if (configured.isNotEmpty()) {
            Text(
                text = stringResource(R.string.terminal_form_installments_label) + ": " +
                    configured.joinToString(", ") { "${it}x" },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (showPicker && availableInstallments.isNotEmpty()) {
            SpinnerContent(
                selectedItem = null,
                items = availableInstallments,
                onItemSelect = { selected ->
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

/** Campos de tarifa (%) + vigÃªncia de um slot, sem Card prÃ³prio (usado aninhado dentro das seÃ§Ãµes). */
@Composable
private fun TariffEntryFields(
    modifier: Modifier = Modifier,
    entry: TariffEntryUi,
    onRateChange: (String) -> Unit,
    onDateChange: (Long) -> Unit,
    onOpenHistory: () -> Unit,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(
                if (entry.currentRateBps != null) R.string.terminal_form_current_tariff
                else R.string.terminal_form_no_tariff,
                entry.currentRateBps?.let { "${it.bpsToRate()}%" } ?: ""
            ),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        IconTextFieldContent(
            value = entry.rateText,
            onValueChange = onRateChange,
            labelResId = R.string.terminal_form_tariff_rate_label,
            placeholderResId = R.string.terminal_form_tariff_rate_placeholder,
            icon = Icons.Outlined.Percent,
            type = IconTextFieldType.MONEY,
            required = false,
            isError = entry.error != null,
            supportingText = entry.error?.let { { Text(stringResource(it)) } }
        )
        DateTextFieldContent(
            dateMillis = entry.effectiveFromMillis,
            onDateChange = onDateChange,
            labelResId = R.string.terminal_form_tariff_effective_from_label,
            required = false
        )
        if (entry.currentRateBps != null) {
            TextButton(onClick = onOpenHistory) {
                Text(stringResource(R.string.terminal_form_history))
            }
        }
    }
}

private const val SAVE_AND_BACK_BUTTON_TAG = "save-and-back-machine-button"
private const val MAX_INSTALLMENTS = 12
private const val CREDIT_TAB_INDEX = 3

@Preview(showBackground = true, name = "Form maquininha")
@Composable
private fun MachineSaleContentPreview() {
    LucroVarejoTheme {
        MachineSaleContent(
            state = MachineSaleUiState(
                name = "Maquininha Mercado Pago",
                selectedMethods = setOf(TerminalPaymentType.PIX, TerminalPaymentType.CREDIT),
                availableBrands = listOf("Visa"),
                selectedCreditBrandIds = setOf("Visa"),
                creditInstallmentsByBrand = mapOf("Visa" to listOf(1, 2)),
                tariffEntries = listOf(
                    TariffEntryUi(method = TerminalPaymentType.PIX),
                    TariffEntryUi(
                        method = TerminalPaymentType.CREDIT,
                        brandId = "Visa",
                        brandName = "Visa",
                        installments = 1
                    )
                )
            ),
            onNameChange = {},
            onToggleMethod = {},
            onCashChargesTariffToggle = {},
            onAddBrand = {},
            onToggleDebitBrand = {},
            onToggleCreditBrand = {},
            onAddCreditInstallment = { _, _ -> },
            onTariffRateChange = { _, _ -> },
            onTariffDateChange = { _, _ -> },
            onSave = {}
        )
    }
}
