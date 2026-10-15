package com.cabral.lucrovarejo.ui.screens.config

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ModeNight
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.BuildConfig
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode

@Composable
fun ConfigScreen(
    currentThemeMode: ThemeMode,
    onThemeModeChanged: (ThemeMode) -> Unit,
    onLogout: () -> Unit = {}
) {
    var showAboutDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
                    onThemeModeChanged(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
                }
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
fun ConfigScreenLightPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.LIGHT) {
        ConfigScreen(
            currentThemeMode = ThemeMode.LIGHT,
            onThemeModeChanged = {},
            onLogout = {}
        )
    }
}

@Preview(name = "Configurações - escuro", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ConfigScreenDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        ConfigScreen(
            currentThemeMode = ThemeMode.DARK,
            onThemeModeChanged = {},
            onLogout = {}
        )
    }
}
