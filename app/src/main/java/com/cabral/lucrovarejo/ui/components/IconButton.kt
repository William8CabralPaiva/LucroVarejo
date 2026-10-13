package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun IconButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SaveProductButtonContent(
        label = stringResource(R.string.product_create_submit),
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
    )
}

@Composable
fun SaveProductButtonContent(
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(dimensionResource(R.dimen.product_submit_button_height))
            .testTag(SAVE_PRODUCT_BUTTON_TAG),
        shape = RoundedCornerShape(dimensionResource(R.dimen.product_submit_button_radius)),
        contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.product_submit_button_horizontal_padding))
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                dimensionResource(R.dimen.product_submit_button_content_spacing)
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = null
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

internal const val SAVE_PRODUCT_BUTTON_TAG = "save-product-button"

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
