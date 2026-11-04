package com.cabral.lucrovarejo.ui.screens.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.BuildConfig
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.screens.register.RegisterValidator
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode

@Composable
fun ConfigScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onNavigateToTerminals: () -> Unit = {},
    storeName: String? = null,
    onUpdateStoreName: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showStoreNameDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DarkModeItem(
                checked = when (currentThemeMode) {
                    ThemeMode.SYSTEM -> isSystemInDarkTheme()
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                },
                onCheckedChange = { enabled ->
                    onThemeModeChange(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
                }
            )

            SettingsItem(
                icon = Icons.Outlined.Storefront,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_store_name),
                subtitle = storeName ?: stringResource(R.string.settings_store_name_subtitle),
                onClick = { showStoreNameDialog = true }
            )

            SettingsItem(
                icon = Icons.Outlined.PointOfSale,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_terminals),
                subtitle = stringResource(R.string.settings_terminals_subtitle),
                onClick = onNavigateToTerminals
            )

            SettingsItem(
                icon = Icons.AutoMirrored.Outlined.Logout,
                iconTint = MaterialTheme.colorScheme.error,
                title = stringResource(R.string.settings_logout),
                subtitle = stringResource(R.string.settings_logout_subtitle),
                onClick = onLogout
            )

            AboutItem(
                version = BuildConfig.VERSION_NAME,
                onClick = { showAboutDialog = true }
            )
        }
    }

    if (showStoreNameDialog) {
        StoreNameDialog(
            initialName = storeName.orEmpty(),
            onSave = onUpdateStoreName,
            onDismiss = { showStoreNameDialog = false }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.settings_about)) },
            text = { Text(stringResource(R.string.settings_about_version, BuildConfig.VERSION_NAME)) },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.dialog_close))
                }
            }
        )
    }
}

@Composable
private fun StoreNameDialog(
    initialName: String,
    onSave: (String, (Boolean) -> Unit) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var isSaving by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    val validationError = RegisterValidator.validateDisplayName(name)

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text(stringResource(R.string.settings_store_name_dialog_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it.take(MAX_STORE_NAME_LENGTH)
                    hasError = false
                },
                label = { Text(stringResource(R.string.register_display_name_label)) },
                singleLine = true,
                enabled = !isSaving,
                isError = hasError || (name.isNotEmpty() && validationError != null),
                supportingText = {
                    when {
                        hasError -> Text(stringResource(R.string.settings_store_name_error))
                        name.isNotEmpty() && validationError != null ->
                            Text(stringResource(validationError))
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                enabled = validationError == null && !isSaving,
                onClick = {
                    isSaving = true
                    onSave(name) { success ->
                        isSaving = false
                        if (success) onDismiss() else hasError = true
                    }
                }
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

private const val MAX_STORE_NAME_LENGTH = 100

@Composable
private fun DarkModeItem(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsItem(
        icon = Icons.Outlined.ModeNight,
        iconTint = MaterialTheme.colorScheme.primary,
        title = stringResource(R.string.settings_dark_mode),
        subtitle = stringResource(R.string.settings_dark_mode_subtitle),
        checked = checked,
        onCheckedChange = onCheckedChange
    )
}

@Preview(name = "Configurações - claro")
@Composable
private fun ConfigScreenLightPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.LIGHT) {
        ConfigScreen(
            currentThemeMode = ThemeMode.LIGHT,
            onThemeModeChange = {},
            onLogout = {}
        )
    }
}

@Preview(name = "Configurações - escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ConfigScreenDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        ConfigScreen(
            currentThemeMode = ThemeMode.DARK,
            onThemeModeChange = {},
            onLogout = {}
        )
    }
}
