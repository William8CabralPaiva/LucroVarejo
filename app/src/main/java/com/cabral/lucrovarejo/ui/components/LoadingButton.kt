package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

internal fun isLoadingButtonEnabled(enabled: Boolean, isLoading: Boolean): Boolean =
    enabled && !isLoading

@Composable
fun LoadingButton(
    modifier: Modifier = Modifier,
    @StringRes textId: Int = 0,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    onClick: () -> Unit = {},
) {
    Button(
        onClick = onClick,
        enabled = isLoadingButtonEnabled(enabled, isLoading),
        modifier = modifier
            .fillMaxWidth()
            .testTag(LOADING_BUTTON_TAG)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.testTag(LOADING_INDICATOR_TAG)
            )
        } else {
            Text(stringResource(textId))
        }
    }
}

internal const val LOADING_BUTTON_TAG = "loading-button"
internal const val LOADING_INDICATOR_TAG = "loading-indicator"


@Preview(showBackground = true)
@Composable
private fun LoadingButtonPreview() {
    LucroVarejoTheme {
        LoadingButton(
            textId = R.string.register_submit,
            onClick = {}
        )
    }
}
