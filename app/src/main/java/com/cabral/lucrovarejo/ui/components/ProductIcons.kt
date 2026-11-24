package com.cabral.lucrovarejo.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// Ícones de contorno próprios, pois o Material Icons não tem tênis, meia nem chinelo.
private fun outlinedIcon(
    name: String,
    pathBuilder: androidx.compose.ui.graphics.vector.PathBuilder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).path(
    fill = null,
    stroke = SolidColor(Color.Black),
    strokeLineWidth = 1.8f,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round,
    pathBuilder = pathBuilder
).build()

val SneakerIcon: ImageVector by lazy {
    outlinedIcon("Sneaker") {
        moveTo(3f, 18f)
        lineTo(3f, 9f)
        lineTo(8f, 9f)
        lineTo(14f, 13f)
        lineTo(19f, 13f)
        curveTo(21f, 13.5f, 22f, 15.5f, 22f, 18f)
        close()
        moveTo(10.35f, 9.4f)
        lineTo(9f, 11.4f)
        moveTo(13.05f, 11.2f)
        lineTo(11.7f, 13.2f)
    }
}
val SockIcon: ImageVector by lazy {
    outlinedIcon("Sock") {
        moveTo(8f, 3f)
        lineTo(15f, 3f)
        lineTo(15f, 11f)
        lineTo(19.5f, 13f)
        curveTo(21.5f, 14f, 22f, 15.5f, 22f, 17f)
        curveTo(22f, 19f, 20.5f, 20.5f, 19f, 20.5f)
        lineTo(10.5f, 20.5f)
        curveTo(8.8f, 20.5f, 8f, 19.3f, 8f, 17.5f)
        close()
        moveTo(8f, 6.5f)
        lineTo(15f, 6.5f)
    }
}

val SlipperIcon: ImageVector by lazy {
    outlinedIcon("Slipper") {
        moveTo(12f, 2f)
        curveTo(15.5f, 2f, 17.5f, 4.8f, 17.5f, 8f)
        curveTo(17.5f, 11f, 16f, 12.5f, 16.5f, 16f)
        curveTo(17f, 19.5f, 15f, 22f, 12f, 22f)
        curveTo(9f, 22f, 7f, 19.5f, 7.5f, 16f)
        curveTo(8f, 12.5f, 6.5f, 11f, 6.5f, 8f)
        curveTo(6.5f, 4.8f, 8.5f, 2f, 12f, 2f)
        close()
        moveTo(12f, 5f)
        lineTo(12f, 7.5f)
        moveTo(7.3f, 10.5f)
        lineTo(12f, 7.5f)
        lineTo(16.7f, 10.5f)
    }
}




val UmbrellaIcon: ImageVector by lazy {
    outlinedIcon("Umbrella") {
        moveTo(2.5f, 12f)
        curveTo(2.5f, 6.8f, 6.8f, 3f, 12f, 3f)
        curveTo(17.2f, 3f, 21.5f, 6.8f, 21.5f, 12f)
        curveTo(20.2f, 10.8f, 18.4f, 10.8f, 17f, 12f)
        curveTo(15.6f, 10.8f, 13.4f, 10.8f, 12f, 12f)
        curveTo(10.6f, 10.8f, 8.4f, 10.8f, 7f, 12f)
        curveTo(5.6f, 10.8f, 3.8f, 10.8f, 2.5f, 12f)
        close()
        moveTo(12f, 3f)
        lineTo(12f, 2f)
        moveTo(12f, 12f)
        lineTo(12f, 19f)
        curveTo(12f, 20.8f, 10.8f, 21.5f, 9.8f, 21.5f)
        curveTo(8.8f, 21.5f, 8f, 20.8f, 8f, 19.8f)
    }
}

val BackpackIcon: ImageVector by lazy {
    outlinedIcon("Backpack") {
        moveTo(6f, 21f)
        lineTo(6f, 10f)
        curveTo(6f, 6.5f, 8.5f, 4.5f, 12f, 4.5f)
        curveTo(15.5f, 4.5f, 18f, 6.5f, 18f, 10f)
        lineTo(18f, 21f)
        close()
        moveTo(10f, 4.5f)
        lineTo(10f, 3f)
        lineTo(14f, 3f)
        lineTo(14f, 4.5f)
        moveTo(9f, 21f)
        lineTo(9f, 14f)
        lineTo(15f, 14f)
        lineTo(15f, 21f)
        moveTo(9f, 9.5f)
        lineTo(15f, 9.5f)
    }
}

val GymBagIcon: ImageVector by lazy {
    outlinedIcon("GymBag") {
        moveTo(2.5f, 12f)
        curveTo(2.5f, 9.8f, 4f, 9f, 6f, 9f)
        lineTo(18f, 9f)
        curveTo(20f, 9f, 21.5f, 9.8f, 21.5f, 12f)
        lineTo(21.5f, 17f)
        curveTo(21.5f, 18.5f, 20.5f, 19f, 19.5f, 19f)
        lineTo(4.5f, 19f)
        curveTo(3.5f, 19f, 2.5f, 18.5f, 2.5f, 17f)
        close()
        moveTo(8f, 9f)
        curveTo(8f, 4.5f, 16f, 4.5f, 16f, 9f)
        moveTo(2.5f, 13f)
        lineTo(21.5f, 13f)
        moveTo(7f, 13f)
        lineTo(7f, 19f)
        moveTo(17f, 13f)
        lineTo(17f, 19f)
    }
}
