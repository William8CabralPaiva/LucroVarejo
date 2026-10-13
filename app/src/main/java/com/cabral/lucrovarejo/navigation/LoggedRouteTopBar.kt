package com.cabral.lucrovarejo.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.cabral.lucrovarejo.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoggedRouteTopBar(
    @StringRes textId: Int,
    route: String,
    onBackPress: () -> Unit,
) {
    if (route == LoggedRoutes.PRODUCT) {
        TopAppBar(
            title = { Text(stringResource(textId)) },
            navigationIcon = {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            }
        )
    }
}
