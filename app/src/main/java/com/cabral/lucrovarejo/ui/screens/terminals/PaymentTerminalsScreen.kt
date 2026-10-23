package com.cabral.lucrovarejo.ui.screens.terminals

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.domain.payment.PaymentTerminalModel
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun PaymentTerminalsScreen(
    onAddTerminal: () -> Unit,
    onEditTerminal: (PaymentTerminalModel) -> Unit = {},
    viewModel: PaymentTerminalsViewModel? = null,
) {
    val resolvedViewModel = viewModel ?: hiltViewModel()
    val state by resolvedViewModel.uiState.collectAsState()
    PaymentTerminalsContent(
        state = state,
        onAddTerminal = onAddTerminal,
        onEditTerminal = onEditTerminal,
        onArchive = resolvedViewModel::onArchive,
    )
}

@Composable
fun PaymentTerminalsContent(
    state: PaymentTerminalsUiState,
    onAddTerminal: () -> Unit,
    onEditTerminal: (PaymentTerminalModel) -> Unit = {},
    onArchive: (String) -> Unit,
) {
    var terminalToArchive by remember { mutableStateOf<PaymentTerminalModel?>(null) }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTerminal,
                containerColor = MaterialTheme.colorScheme.primary,
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(R.string.terminals_add)
                )
            }
        }
    ) { innerPadding ->
        when {
            state.isLoading -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            state.hasError -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.terminals_load_error),
                    color = MaterialTheme.colorScheme.error
                )
            }

            state.items.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(R.string.terminals_empty))
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.items, key = { it.key ?: it.name }) { terminal ->
                    PaymentTerminalItem(
                        terminal = terminal,
                        onClick = { onEditTerminal(terminal) },
                        onArchive = { terminalToArchive = terminal }
                    )
                }
            }
        }
    }

    terminalToArchive?.let { terminal ->
        AlertDialog(
            onDismissRequest = { terminalToArchive = null },
            title = { Text(stringResource(R.string.terminals_archive_dialog_title)) },
            text = { Text(stringResource(R.string.terminals_archive_dialog_message, terminal.name)) },
            confirmButton = {
                TextButton(onClick = {
                    terminal.key?.let(onArchive)
                    terminalToArchive = null
                }) {
                    Text(stringResource(R.string.dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { terminalToArchive = null }) {
                    Text(stringResource(R.string.dialog_cancel))
                }
            }
        )
    }
}

@Composable
private fun PaymentTerminalItem(
    terminal: PaymentTerminalModel,
    onClick: () -> Unit,
    onArchive: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = terminal.name, style = MaterialTheme.typography.titleMedium)
                if (!terminal.active) {
                    AssistChip(onClick = {}, label = { Text(stringResource(R.string.terminals_archived_badge)) })
                }
            }
            if (terminal.active) {
                TextButton(onClick = onArchive) {
                    Text(stringResource(R.string.terminals_archive))
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Maquininhas")
@Composable
private fun PaymentTerminalsContentPreview() {
    LucroVarejoTheme {
        PaymentTerminalsContent(
            state = PaymentTerminalsUiState(
                isLoading = false,
                items = listOf(
                    PaymentTerminalModel(key = "1", name = "Maquininha Mercado Pago"),
                    PaymentTerminalModel(key = "2", name = "Stone", active = false)
                )
            ),
            onAddTerminal = {},
            onEditTerminal = {},
            onArchive = {}
        )
    }
}

@Preview(showBackground = true, name = "Maquininhas vazio")
@Composable
private fun PaymentTerminalsContentEmptyPreview() {
    LucroVarejoTheme {
        PaymentTerminalsContent(
            state = PaymentTerminalsUiState(isLoading = false),
            onAddTerminal = {},
            onArchive = {}
        )
    }
}
