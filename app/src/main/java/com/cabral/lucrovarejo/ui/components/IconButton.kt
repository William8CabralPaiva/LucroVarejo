package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.DarkSurfaceVariant
import com.cabral.lucrovarejo.ui.theme.ForestGreenHeader
import com.cabral.lucrovarejo.ui.theme.LocalIsDarkTheme
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme
import com.cabral.lucrovarejo.ui.theme.TextSecondaryDark
import com.cabral.lucrovarejo.ui.theme.TextSecondaryLight

internal data class SaveProductButtonPalette(
    val container: Color,
    val content: Color,
    val disabledContainer: Color,
    val disabledContent: Color
)

internal fun saveProductButtonPalette(isDarkTheme: Boolean) = SaveProductButtonPalette(
    container = ForestGreenHeader,
    content = Color.White,
    disabledContainer = if (isDarkTheme) DarkSurfaceVariant else Color(0xFFDCE8E2),
    disabledContent = if (isDarkTheme) TextSecondaryDark else TextSecondaryLight
)

@Composable
fun IconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: ImageVector = Icons.Default.Save,
    iconContentDescription: String? = null,
    text: String? = null,
    loadingText: String? = null
) {
    SaveProductButtonContent(
        label = text ?: stringResource(R.string.product_create_submit),
        loadingLabel = loadingText ?: stringResource(R.string.product_create_loading),
        enabled = enabled,
        isLoading = isLoading,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        iconContentDescription = iconContentDescription
    )
}

@Composable
fun SaveProductButtonContent(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    loadingLabel: String = label,
    icon: ImageVector = Icons.Default.Save,
    iconContentDescription: String? = null
) {
    val palette = saveProductButtonPalette(LocalIsDarkTheme.current)
    Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.product_submit_button_height))
            .testTag(SAVE_PRODUCT_BUTTON_TAG),
        shape = RoundedCornerShape(dimensionResource(R.dimen.product_submit_button_radius)),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(R.dimen.product_submit_button_horizontal_padding)
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = palette.container,
            contentColor = palette.content,
            disabledContainerColor = if (isLoading) palette.container else palette.disabledContainer,
            disabledContentColor = if (isLoading) palette.content else palette.disabledContent
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.product_submit_button_content_spacing)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(dimensionResource(R.dimen.product_submit_progress_size))
                        .testTag(SAVE_PRODUCT_PROGRESS_TAG),
                    color = LocalContentColor.current,
                    strokeWidth = dimensionResource(R.dimen.product_submit_progress_stroke)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = iconContentDescription,
                    tint = LocalContentColor.current
                )
            }
            Text(
                text = if (isLoading) loadingLabel else label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

internal const val SAVE_PRODUCT_BUTTON_TAG = "save-product-button"
internal const val SAVE_PRODUCT_PROGRESS_TAG = "save-product-progress"

@Preview(showBackground = true, name = "Salvar produto habilitado")
@Composable
private fun IconButtonEnabledPreview() {
    LucroVarejoTheme {
        IconButton(enabled = true, onClick = {})
    }
}

@Preview(showBackground = true, name = "Salvar produto desabilitado")
@Composable
private fun IconButtonDisabledPreview() {
    LucroVarejoTheme {
        IconButton(enabled = false, onClick = {})
    }
}
