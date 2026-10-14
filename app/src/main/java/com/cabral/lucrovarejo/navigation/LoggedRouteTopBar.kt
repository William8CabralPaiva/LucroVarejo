package com.cabral.lucrovarejo.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.DarkPrimaryContainer
import com.cabral.lucrovarejo.ui.theme.LightMintContainer

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
        LoggedRoutes.ADD_PURCHASE -> R.string.add_purchase_title
        else -> return
    }
    val showBackButton = showBackButton(route)

    TopAppBar(
        title = {
            Text(
                text = stringResource(titleId),
                color = LightMintContainer,
                fontWeight = FontWeight.Bold
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkPrimaryContainer
        ),
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onBackPress) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.navigate_back),
                        tint = LightMintContainer
                    )
                }
            }
        }
    )
}

private fun showBackButton(route: String): Boolean {
    return route == LoggedRoutes.PRODUCT ||
            route == LoggedRoutes.CONFIG ||
            route == LoggedRoutes.ADD_PURCHASE
}
