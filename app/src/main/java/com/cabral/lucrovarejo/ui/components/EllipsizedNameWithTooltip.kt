package com.cabral.lucrovarejo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

/** Nome limitado a [maxLines] com reticencias. Ao segurar o dedo, mostra o nome completo em um tooltip. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EllipsizedNameWithTooltip(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    maxLines: Int = 2,
    fontWeight: FontWeight? = null,
    color: Color = Color.Unspecified,
) {
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
        tooltip = { PlainTooltip { Text(text) } },
        state = rememberTooltipState(),
        modifier = modifier
    ) {
        Text(
            text = text,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = style,
            fontWeight = fontWeight,
            color = color
        )
    }
}

@Preview(showBackground = true, widthDp = 220, name = "Nome com tooltip")
@Composable
private fun EllipsizedNameWithTooltipPreview() {
    LucroVarejoTheme {
        Column(modifier = Modifier.padding(dimensionResource(R.dimen.spacing_16dp))) {
            EllipsizedNameWithTooltip(
                text = "Guarda-chuva automático reforçado com cabo emborrachado e capa",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
