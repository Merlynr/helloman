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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.lifeonline.app.data.model.SleepRecord
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeCardBg
import com.lifeonline.app.ui.theme.CartridgeForestGreen
import com.lifeonline.app.ui.theme.CartridgeGold
import com.lifeonline.app.ui.theme.CartridgeTextDark
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MorningSettlementDialog(
    record: SleepRecord,
    onDismiss: () -> Unit
) {
    val isLate = record.isLateNight
    val mainAccentColor = if (isLate) CartridgeAccentRed else CartridgeForestGreen
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val sleepTimeStr = if (record.sleepTimeMillis > 0) timeFmt.format(Date(record.sleepTimeMillis)) else "22:15"
    val wakeTimeStr = if (record.wakeTimeMillis > 0) timeFmt.format(Date(record.wakeTimeMillis)) else "07:00"
    val durationHrs = String.format(Locale.getDefault(), "%.1f", record.durationMinutes / 60.0f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CartridgeBg,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(2.5.dp, CartridgeTextDark, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // 顶部标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .background(mainAccentColor, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (isLate) "⚠️ 熬夜警报" else "✨ 晨间唤醒结算",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "SLEEP REPORT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CartridgeTextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 核心状态称号
                Text(
                    text = record.statusTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = CartridgeTextDark
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isLate) "入睡晚于 22:30 标尺，生理机能透支！" else "遵循 22:30 安歇天时，气血全面回充！",
                    fontSize = 12.sp,
                    color = mainAccentColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 作息数据卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CartridgeCardBg, RoundedCornerShape(8.dp))
                        .border(1.5.dp, CartridgeTextDark.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "入睡时刻", fontSize = 11.sp, color = CartridgeTextMuted)
                            Text(
                                text = sleepTimeStr,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isLate) CartridgeAccentRed else CartridgeForestGreen,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isLate) "⚠️晚于22:30" else "●天时入眠",
                                fontSize = 9.sp,
                                color = if (isLate) CartridgeAccentRed else CartridgeForestGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(CartridgeTextDark.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "晨醒时刻", fontSize = 11.sp, color = CartridgeTextMuted)
                            Text(
                                text = wakeTimeStr,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CartridgeTextDark,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (record.isEarlyBird) "✨优质晨光" else "●正常作息",
                                fontSize = 9.sp,
                                color = CartridgeForestGreen
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(40.dp)
                                .background(CartridgeTextDark.copy(alpha = 0.2f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "总计睡眠", fontSize = 11.sp, color = CartridgeTextMuted)
                            Text(
                                text = "${durationHrs}h",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = CartridgeTextDark,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "得分 ${record.sleepScore}",
                                fontSize = 9.sp,
                                color = CartridgeGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 属性结算条目
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (record.vitDelta >= 0) CartridgeForestGreen.copy(alpha = 0.12f)
                                else CartridgeAccentRed.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (record.vitDelta >= 0) CartridgeForestGreen else CartridgeAccentRed,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VIT 气血 ${if (record.vitDelta >= 0) "+${record.vitDelta}" else "${record.vitDelta}"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (record.vitDelta >= 0) CartridgeForestGreen else CartridgeAccentRed
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (record.hpDelta >= 0) CartridgeForestGreen.copy(alpha = 0.12f)
                                else CartridgeAccentRed.copy(alpha = 0.12f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                1.dp,
                                if (record.hpDelta >= 0) CartridgeForestGreen else CartridgeAccentRed,
                                RoundedCornerShape(6.dp)
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HP 体力 ${if (record.hpDelta >= 0) "+${record.hpDelta}" else "${record.hpDelta}"}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (record.hpDelta >= 0) CartridgeForestGreen else CartridgeAccentRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 研判详情
                Text(
                    text = record.description.ifBlank {
                        if (isLate) "熬夜导致体内阳气耗竭，今日请注意补充水分并多做舒展拉伸！"
                        else "身心充足回血，带着充盈气血开启今天的现实战役吧！"
                    },
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = CartridgeTextDark.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // 确认按钮
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = mainAccentColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .border(1.5.dp, CartridgeTextDark, RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = if (isLate) "⚡ 收下Debuff · 今日引以为戒" else "✨ 吸收气血 · 出征现实世界",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
