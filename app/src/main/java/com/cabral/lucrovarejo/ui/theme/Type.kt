package com.cabral.lucrovarejo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import com.cabral.lucrovarejo.R

// toSp() aplica a escala de fonte do usuário uma única vez (dimensionResource em sp a duplicaria).
@Composable
private fun sp(id: Int): TextUnit {
    val resources = LocalContext.current.resources
    return with(LocalDensity.current) { resources.getDimension(id).toSp() }
}

// Tamanhos vêm de dimens_responsive.xml (por largura de tela); peso e espaçamento seguem o padrão do Material 3.
@Composable
fun responsiveTypography(): Typography {
    val base = Typography()

    @Composable
    fun TextStyle.scaled(textRes: Int, lineRes: Int) =
        copy(fontSize = sp(textRes), lineHeight = sp(lineRes))

    return Typography(
        displayLarge = base.displayLarge.scaled(R.dimen.text_display_large, R.dimen.line_display_large),
        displayMedium = base.displayMedium.scaled(R.dimen.text_display_medium, R.dimen.line_display_medium),
        displaySmall = base.displaySmall.scaled(R.dimen.text_display_small, R.dimen.line_display_small),
        headlineLarge = base.headlineLarge.scaled(R.dimen.text_headline_large, R.dimen.line_headline_large),
        headlineMedium = base.headlineMedium.scaled(R.dimen.text_headline_medium, R.dimen.line_headline_medium),
        headlineSmall = base.headlineSmall.scaled(R.dimen.text_headline_small, R.dimen.line_headline_small),
        titleLarge = base.titleLarge.scaled(R.dimen.text_title_large, R.dimen.line_title_large),
        titleMedium = base.titleMedium.scaled(R.dimen.text_title_medium, R.dimen.line_title_medium),
        titleSmall = base.titleSmall.scaled(R.dimen.text_title_small, R.dimen.line_title_small),
        bodyLarge = base.bodyLarge.scaled(R.dimen.text_body_large, R.dimen.line_body_large),
        bodyMedium = base.bodyMedium.scaled(R.dimen.text_body_medium, R.dimen.line_body_medium),
        bodySmall = base.bodySmall.scaled(R.dimen.text_body_small, R.dimen.line_body_small),
        labelLarge = base.labelLarge.scaled(R.dimen.text_label_large, R.dimen.line_label_large),
        labelMedium = base.labelMedium.scaled(R.dimen.text_label_medium, R.dimen.line_label_medium),
        labelSmall = base.labelSmall.scaled(R.dimen.text_label_small, R.dimen.line_label_small),
    )
}
