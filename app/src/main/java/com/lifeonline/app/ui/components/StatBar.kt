package com.lifeonline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeText
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.CartridgeTickEmpty
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText

@Composable
fun StatBar(
    label: String,
    currentValue: Int,
    maxValue: Int,
    barColor: Color = RetroGreenPrimary,
    modifier: Modifier = Modifier
) {
    val isCartridge = LocalIsCartridgeTheme.current
    val progress = (currentValue.toFloat() / maxValue.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)

    val labelColor = if (isCartridge) CartridgeText else RetroGreenText
    val valueColor = if (isCartridge) CartridgeTextMuted else barColor
    val trackBg = if (isCartridge) CartridgeTickEmpty else RetroDarkBg
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val activeFillColor = if (isCartridge) {
        if (barColor == Color(0xFFEF4444)) Color(0xFFC85A54) // HP 砖红
        else if (barColor == Color(0xFF38BDF8)) Color(0xFF426E57) // MP 墨绿
        else CartridgeGreen
    } else barColor

    Column(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
        ) {
            Text(
                text = label,
                color = labelColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$currentValue / $maxValue",
                color = valueColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (isCartridge) {
            // 卡带纸质风格的刻度进度条
            PixelSegmentedBar(
                progress = progress,
                totalTicks = 36,
                activeColor = activeFillColor,
                modifier = Modifier.fillMaxWidth().height(14.dp)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(trackBg)
                    .border(1.dp, borderColor, RoundedCornerShape(3.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(2.dp))
                        .background(activeFillColor)
                )
            }
        }
    }
}
