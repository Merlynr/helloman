package com.lifeonline.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsCartridgeTheme = compositionLocalOf { true }

// 暖白卡带亮色主题
private val CartridgeColorScheme = lightColorScheme(
    primary = CartridgeGreen,
    secondary = CartridgeAccentRed,
    tertiary = CartridgeGreenLight,
    background = CartridgeBg,
    surface = CartridgeCard,
    onPrimary = CartridgeBg,
    onSecondary = CartridgeBg,
    onTertiary = CartridgeBg,
    onBackground = CartridgeText,
    onSurface = CartridgeText
)

// 黑客终端暗黑主题
private val RetroDarkColorScheme = darkColorScheme(
    primary = RetroGreenPrimary,
    secondary = RetroAmber,
    tertiary = RetroBlue,
    background = RetroDarkBg,
    surface = RetroCardBg,
    onPrimary = RetroDarkBg,
    onSecondary = RetroDarkBg,
    onTertiary = RetroDarkBg,
    onBackground = RetroGreenText,
    onSurface = RetroGreenText
)

@Composable
fun LifeOnlineTheme(
    isCartridge: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (isCartridge) CartridgeColorScheme else RetroDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val bg = if (isCartridge) CartridgeBg else RetroDarkBg
            window.statusBarColor = bg.toArgb()
            window.navigationBarColor = bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = isCartridge
        }
    }

    CompositionLocalProvider(LocalIsCartridgeTheme provides isCartridge) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
