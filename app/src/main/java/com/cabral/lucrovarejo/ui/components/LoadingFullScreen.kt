package com.cabral.lucrovarejo.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

@Composable
fun LoadingFullScreen(
    @StringRes loadingTextId: Int,
    modifier: Modifier = Modifier,
) {
    LoadingFullScreenContent(
        modifier = modifier,
        loadingTextId = loadingTextId
    )
}

@Composable
fun LoadingFullScreenContent(
    @StringRes loadingTextId: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.spacing_16dp))
        ) {
            CircularProgressIndicator()
            Text(text = stringResource(loadingTextId))
        }
    }
}

@Preview
@Composable
private fun LoadingFullScreenPreview() {
    LucroVarejoTheme {
        LoadingFullScreen(loadingTextId = R.string.purchase_load_products_loading)
    }
}
