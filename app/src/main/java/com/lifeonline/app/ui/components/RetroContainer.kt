package com.lifeonline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeCard
import com.lifeonline.app.ui.theme.CartridgeCardWhite
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroCardBg
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroCardBorderLight
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText

@Composable
fun RetroContainer(
    title: String? = null,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isCartridge = LocalIsCartridgeTheme.current

    val bg = if (isCartridge) CartridgeCardWhite else RetroCardBg
    val borderCol = if (isCartridge) CartridgeBorder else RetroCardBorder
    val borderWidth = if (isCartridge) 2.dp else 1.5.dp
    val radius = if (isCartridge) 8.dp else 6.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(radius))
            .background(bg)
            .border(borderWidth, borderCol, RoundedCornerShape(radius))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (title != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    if (isCartridge) {
                        // 暖白卡带风格的反白标签
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(CartridgeGreen)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "/// $title",
                                color = CartridgeCard,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        // 黑客暗黑绿风格
                        Text(
                            text = "///",
                            color = RetroGreenPrimary,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            color = RetroGreenText,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        )
                    }

                    if (subtitle != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = subtitle,
                            color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
            content()
        }
    }
}
