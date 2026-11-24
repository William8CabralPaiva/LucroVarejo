package com.cabral.lucrovarejo.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.ThemeMode
/**
 * Switch padrão do app: mantém a bolinha (thumb) com bom contraste tanto ligado quanto
 * desligado. Use este componente em vez de `Switch` do Material3 para que todas as telas
 * sigam o mesmo visual sem precisar repetir as cores em cada ponto de uso.
 */
@Composable
fun appSwitchColors(): SwitchColors = SwitchDefaults.colors(
    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
    checkedTrackColor = MaterialTheme.colorScheme.primary,
    uncheckedThumbColor = MaterialTheme.colorScheme.primary,
    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
    uncheckedBorderColor = MaterialTheme.colorScheme.outline
)

@Composable
fun AppSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = appSwitchColors()
    )
}

@Preview(showBackground = true, name = "AppSwitch - claro")
@Composable
private fun AppSwitchLightPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.LIGHT) {
        AppSwitchPreviewContent()
    }
}

@Preview(
    showBackground = true,
    name = "AppSwitch - escuro",
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AppSwitchDarkPreview() {
    LucroVarejoTheme(themeMode = ThemeMode.DARK) {
        AppSwitchPreviewContent()
    }
}

@Composable
private fun AppSwitchPreviewContent() {
    Row(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) {
        AppSwitch(checked = true, onCheckedChange = {})
        AppSwitch(checked = false, onCheckedChange = {}, modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_16dp)))
        AppSwitch(
            checked = true,
            onCheckedChange = {},
            enabled = false,
            modifier = Modifier.padding(start = dimensionResource(R.dimen.spacing_16dp))
        )
    }
}
