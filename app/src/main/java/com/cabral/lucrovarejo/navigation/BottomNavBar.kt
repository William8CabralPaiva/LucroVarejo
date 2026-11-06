package com.cabral.lucrovarejo.navigation

import androidx.compose.ui.draw.drawBehind
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cabral.lucrovarejo.R
import com.cabral.lucrovarejo.ui.theme.LucroVarejoTheme

private data class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
)

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        BottomNavItem(LoggedRoutes.HOME, R.string.bottom_nav_home, Icons.Default.Home),
        BottomNavItem(
            LoggedRoutes.SALES,
            R.string.bottom_nav_sales,
            Icons.AutoMirrored.Filled.ReceiptLong
        ),
        BottomNavItem(LoggedRoutes.PRODUCTS, R.string.bottom_nav_products, Icons.Outlined.Inventory2),
        //Não Descomentar
        //BottomNavItem(LoggedRoutes.SUMMARY, R.string.bottom_nav_summary, Icons.Default.BarChart),
        BottomNavItem(LoggedRoutes.CONFIG, R.string.bottom_nav_config, Icons.Default.Settings)
    )

    // A barra mantem a altura nas telas de formulario (so esconde os itens e a linha); sumir
    // de vez mudava o layout e fazia a tela descer.
    val isTabRoute = items.any { it.route == currentRoute }
    run {
        val dividerColor = if (isTabRoute) {
            MaterialTheme.colorScheme.outline
        } else {
            MaterialTheme.colorScheme.surface
        }
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = modifier.drawBehind {
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 1.dp.toPx()
                )
            }
        ) {
            if (isTabRoute) items.forEach { item ->
                val label = stringResource(item.labelRes)
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = { onNavigate(item.route) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label
                        )
                    },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        indicatorColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Barra inferior")
@Composable
private fun BottomNavBarPreview() {
    LucroVarejoTheme {
        BottomNavBar(currentRoute = LoggedRoutes.HOME, onNavigate = {})
    }
}
