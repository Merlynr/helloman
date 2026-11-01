package com.lifeonline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.data.model.BuffItem
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeCardWhite
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroMuted
import com.lifeonline.app.ui.theme.RetroRed

@Composable
fun BuffChip(
    buff: BuffItem,
    onRemove: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isCartridge = LocalIsCartridgeTheme.current
    val borderColor = if (isCartridge) {
        if (buff.isDebuff) CartridgeAccentRed else CartridgeGreen
    } else {
        if (buff.isDebuff) RetroRed else RetroGreenPrimary
    }
    val textColor = borderColor
    val bg = if (isCartridge) CartridgeCardWhite else RetroDarkBg

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.2.dp, borderColor.copy(alpha = 0.8f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (buff.isDebuff) "▼" else "▲",
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = buff.name,
            color = textColor,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
        if (onRemove != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "移除",
                tint = if (isCartridge) CartridgeTextMuted else RetroMuted,
                modifier = Modifier
                    .size(12.dp)
                    .clickable { onRemove() }
            )
        }
    }
}
