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
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.ModeNight
import androidx.compose.material.icons.outlined.PointOfSale
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.BuildConfig
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.components.StoreNameDialog
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
    onShowTips: () -> Unit = {},
    onUpdateStoreName: (String, (Boolean) -> Unit) -> Unit = { _, _ -> },
    onOpenSwitchStore: () -> Unit = {},
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
                .padding(horizontal = dimensionResource(R.dimen.spacing_16dp), vertical = dimensionResource(R.dimen.spacing_24dp)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16dp))
        ) {
            SettingsItem(
                icon = Icons.Outlined.Storefront,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_store_name),
                subtitle = storeName ?: stringResource(R.string.settings_store_name_subtitle),
                onClick = { showStoreNameDialog = true }
            )

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
                icon = Icons.Outlined.PointOfSale,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_terminals),
                subtitle = stringResource(R.string.settings_terminals_subtitle),
                onClick = onNavigateToTerminals
            )

            SettingsItem(
                icon = Icons.Outlined.Lightbulb,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_tips),
                subtitle = stringResource(R.string.settings_tips_subtitle),
                onClick = onShowTips
            )

            SettingsItem(
                icon = Icons.Outlined.SwapHoriz,
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_switch_store),
                subtitle = stringResource(
                    R.string.settings_switch_store_subtitle,
                    storeName ?: stringResource(R.string.settings_store_unnamed)
                ),
                onClick = onOpenSwitchStore
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
            title = R.string.settings_store_name_dialog_title,
            errorMessage = R.string.settings_store_name_error,
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
