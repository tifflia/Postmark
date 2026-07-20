package com.ait.postmark.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PostmarkColors = lightColorScheme(
    primary = InkBlack,
    onPrimary = Parchment,
    secondary = StampRed,
    onSecondary = SealGold,
    background = Parchment,
    onBackground = InkBlack,
    surface = PaperWhite,
    onSurface = InkBlack,
    surfaceVariant = ParchmentDark,
    onSurfaceVariant = InkSoft,
    outline = InkBlack
)

// Using Serif as the display family for that "stamped on paper" feel.
// Swap to a custom font (e.g. Playfair Display) by adding it to res/font and
// referencing it as FontFamily(Font(R.font.playfair_display)).
private val Serif = FontFamily.Serif

private val PostmarkTypography = Typography(
    displayLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 48.sp, letterSpacing = (-0.5).sp),
    headlineLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 32.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 24.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontFamily = Serif, fontSize = 18.sp),
    bodyMedium = TextStyle(fontFamily = Serif, fontSize = 16.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 2.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 11.sp, letterSpacing = 1.5.sp)
)

private val PostmarkShapes = Shapes(
    extraSmall = RoundedCornerShape(2.dp),
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(6.dp),
    large = RoundedCornerShape(8.dp)
)

@Composable
fun PostmarkTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PostmarkColors,
        typography = PostmarkTypography,
        shapes = PostmarkShapes,
        content = content
    )
}
