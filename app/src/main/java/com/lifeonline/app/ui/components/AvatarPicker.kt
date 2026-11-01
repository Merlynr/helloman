package com.lifeonline.app.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeCardWhite
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeTextMuted

data class AvatarOption(
    val id: String,
    val icon: String,
    val name: String,
    val traitTag: String
)

val PRESET_AVATARS = listOf(
    // 动物图腾
    AvatarOption("bear", "🐻", "忠诚棕熊", "稳健·坚韧"),
    AvatarOption("fox", "🦊", "灵睿赤狐", "灵动·破局"),
    AvatarOption("owl", "🦉", "洞察夜枭", "智思·洞悉"),
    AvatarOption("dog", "🐶", "坚毅柴犬", "忠直·长跑"),
    AvatarOption("cat", "🐱", "敏捷灵猫", "专注·心流"),
    AvatarOption("panda", "🐼", "沉稳墨竹", "中正·从容"),

    // 职业与特质图腾
    AvatarOption("robot", "🤖", "极客代码", "算力·极客"),
    AvatarOption("wizard", "🧙‍♂️", "智者学者", "深研·博雅"),
    AvatarOption("warrior", "⚔️", "坚毅行者", "破难·出征"),
    AvatarOption("scale", "⚖️", "律政秩序", "公允·护城"),
    AvatarOption("merchant", "💼", "宏图商贾", "财商·复利"),
    AvatarOption("leaf", "🌿", "归元自律", "气血·长青")
)

/**
 * 通用头像渲染组件（支持 Emoji 图腾与 本地相册图片 URI）
 */
@Composable
fun AvatarDisplay(
    avatar: String,
    modifier: Modifier = Modifier,
    size: Dp = 46.dp,
    fontSize: TextUnit = 24.sp
) {
    val context = LocalContext.current
    val isImageUri = avatar.startsWith("content://") || avatar.startsWith("file://")

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (isImageUri) {
            val bitmap = remember(avatar) {
                try {
                    val uri = Uri.parse(avatar)
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)
                    }
                } catch (_: Exception) {
                    null
                }
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(text = "🐻", fontSize = fontSize)
            }
        } else {
            Text(text = avatar.ifBlank { "🐻" }, fontSize = fontSize)
        }
    }
}

/**
 * 卡头头像选择器网格组件
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AvatarPicker(
    selectedAvatar: String,
    onAvatarSelected: (String) -> Unit,
    isCartridge: Boolean = true
) {
    val context = LocalContext.current
    val activeBorderColor = if (isCartridge) CartridgeGreen else Color(0xFF4ADE80)
    val inactiveBorderColor = if (isCartridge) CartridgeBorder.copy(alpha = 0.25f) else Color(0xFF2C3B2B)

    // 相册图片选择器
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onAvatarSelected(uri.toString())
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "选择角色专属卡头图腾",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCartridge) CartridgeGreen else Color.White,
                fontFamily = FontFamily.Monospace
            )

            // 本地相册上传按钮
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCartridge) Color(0xFFEDE9DF) else Color(0xFF161C15))
                    .border(1.dp, activeBorderColor.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .clickable { galleryLauncher.launch("image/*") }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "📷 相册选图",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeBorderColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 12 款复古预设网格 (FlowRow 自动排版)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            maxItemsInEachRow = 4
        ) {
            PRESET_AVATARS.forEach { option ->
                val isSelected = selectedAvatar == option.icon
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) (if (isCartridge) Color(0xFFE2DDD3) else Color(0xFF1B2E20)) else (if (isCartridge) CartridgeCardWhite else Color(0xFF121811)))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) activeBorderColor else inactiveBorderColor,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable { onAvatarSelected(option.icon) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = option.icon, fontSize = 26.sp)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = option.name,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) (if (isCartridge) CartridgeGreen else Color(0xFF4ADE80)) else CartridgeTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = option.traitTag,
                            fontSize = 8.sp,
                            color = if (isCartridge) CartridgeTextMuted.copy(alpha = 0.8f) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}
