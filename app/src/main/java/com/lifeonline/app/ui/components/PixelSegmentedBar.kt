package com.lifeonline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeTickEmpty
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary

/**
 * 像素复古刻度进度条 (对应参考图中的垂直条纹 ||||||||||||||||||||)
 */
@Composable
fun PixelSegmentedBar(
    progress: Float,
    modifier: Modifier = Modifier,
    totalTicks: Int = 36,
    activeColor: Color? = null,
    inactiveColor: Color? = null
) {
    val isCartridge = LocalIsCartridgeTheme.current
    val filledCount = (progress.coerceIn(0f, 1f) * totalTicks).toInt()
    val emptyCount = totalTicks - filledCount

    val filledColor = activeColor ?: if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val emptyColor = inactiveColor ?: if (isCartridge) CartridgeTickEmpty else RetroCardBorder
    val containerBg = if (isCartridge) Color(0xFFF0EBE1) else RetroDarkBg
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(3.dp))
            .border(1.dp, borderColor, RoundedCornerShape(3.dp))
            .background(containerBg)
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Row {
            Text(
                text = "|".repeat(filledCount),
                color = filledColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = (-1.5).sp,
                lineHeight = 11.sp
            )
            Text(
                text = "|".repeat(emptyCount),
                color = emptyColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = (-1.5).sp,
                lineHeight = 11.sp
            )
        }
    }
}
