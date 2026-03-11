package com.example.treasurehunt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = TreasureGold,
    secondary = StoryAccent,
    tertiary = AdventureAccent,
    background = SandBackground,
    surface = CardCream,
    onPrimary = CardCream,
    onSecondary = CardCream,
    onTertiary = CardCream,
    onBackground = TreasureBrown,
    onSurface = TreasureBrown
)

@Composable
fun TreasurehuntTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
