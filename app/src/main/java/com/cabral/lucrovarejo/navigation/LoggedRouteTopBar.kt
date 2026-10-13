package com.cabral.lucrovarejo.navigation

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
    route: String,
    onBackPress: () -> Unit,
) {
    val titleId = when (route) {
        LoggedRoutes.HOME -> R.string.home_title
        LoggedRoutes.PRODUCT -> R.string.new_product
        LoggedRoutes.SALES -> R.string.sales_title
        LoggedRoutes.SUMMARY -> R.string.summary_title
        LoggedRoutes.CONFIG -> R.string.config_title
        else -> return
    }
    val showBackButton = route == LoggedRoutes.PRODUCT || route == LoggedRoutes.CONFIG

    TopAppBar(
        title = { Text(stringResource(titleId)) },
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back)
                    )
                }
            }
        }
    )
}
