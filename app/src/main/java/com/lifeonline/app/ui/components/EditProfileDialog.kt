package com.lifeonline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeText
import com.lifeonline.app.ui.theme.CartridgeTextMuted

@Composable
fun EditProfileDialog(
    userStats: UserStats,
    isCartridge: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (newName: String, newAvatar: String) -> Unit
) {
    var name by remember { mutableStateOf(userStats.name) }
    var selectedAvatar by remember { mutableStateOf(userStats.avatar) }

    val primaryColor = if (isCartridge) CartridgeGreen else Color(0xFF4ADE80)
    val bgColor = if (isCartridge) CartridgeBg else Color(0xFF0F130E)
    val textColor = if (isCartridge) CartridgeText else Color(0xFFDCFCE7)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bgColor,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .border(2.dp, primaryColor, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 顶部标题
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "角色形象与身份配置",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = textColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "ID: #${userStats.id}",
                        fontSize = 11.sp,
                        color = CartridgeTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 头像与姓名预览卡
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCartridge) Color(0xFFFAF8F5) else Color(0xFF161C15), RoundedCornerShape(8.dp))
                        .border(1.dp, CartridgeBorder.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(if (isCartridge) Color(0xFFEDE9DF) else Color(0xFF0F130E), RoundedCornerShape(8.dp))
                            .border(1.5.dp, primaryColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarDisplay(avatar = selectedAvatar, size = 52.dp, fontSize = 28.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = name.ifBlank { "未命名冒险者" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            text = "Lv.${userStats.level} · ${userStats.aiTitle.ifBlank { userStats.title }}",
                            fontSize = 11.sp,
                            color = CartridgeTextMuted,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 角色名称输入框
                Text(
                    text = "角色昵称 / 代号",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor,
                        focusedBorderColor = primaryColor,
                        unfocusedBorderColor = CartridgeBorder.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(6.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 卡头头像选择器
                AvatarPicker(
                    selectedAvatar = selectedAvatar,
                    onAvatarSelected = { selectedAvatar = it },
                    isCartridge = isCartridge
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 操作按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("取消", color = CartridgeTextMuted)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onConfirm(name.trim(), selectedAvatar)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("保存配置", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
