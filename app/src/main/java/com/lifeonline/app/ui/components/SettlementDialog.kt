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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.window.Dialog
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBg
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroCardBorderLight
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import com.lifeonline.app.ui.theme.RetroRed

@Composable
fun SettlementDialog(
    result: SettlementResult,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(RetroCardBg)
                .border(2.dp, RetroGreenPrimary, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 顶部标题
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "★ 今日战报结算 ★",
                        color = RetroAmber,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 经验掉落大卡片
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(RetroDarkBg)
                        .border(1.dp, RetroAmber, RoundedCornerShape(4.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EXP GAINED",
                            color = RetroCardBorderLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "+${result.expGained}",
                            color = RetroAmber,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 战报旁白
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(RetroDarkBg)
                        .padding(10.dp)
                ) {
                    Text(
                        text = result.dialogue,
                        color = RetroGreenText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 五维属性增减展示
                Text(
                    text = "/// 属性变化",
                    color = RetroGreenPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val d = result.attributeDeltas
                    StatDeltaChip("智力", d.tec)
                    StatDeltaChip("气血", d.vit)
                    StatDeltaChip("心力", d.san)
                    StatDeltaChip("财力", d.gold)
                    StatDeltaChip("人脉", d.rel)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = RetroCardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // 《高性价比人生指南》三层标尺点评
                Text(
                    text = "/// 《高性价比人生指南》标尺分析",
                    color = RetroAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                ScaleRow("【近期·微习惯】", result.scaleEvaluation.shortTerm)
                ScaleRow("【阶段·防御垫】", result.scaleEvaluation.midTerm)
                ScaleRow("【长期·复利底线】", result.scaleEvaluation.longTerm)

                Spacer(modifier = Modifier.height(16.dp))

                // 确认按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroGreenPrimary,
                        contentColor = RetroDarkBg
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "[ 收入囊中 (CONFIRM) ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatDeltaChip(label: String, delta: Int) {
    val deltaColor = when {
        delta > 0 -> RetroGreenPrimary
        delta < 0 -> RetroRed
        else -> RetroCardBorderLight
    }
    val sign = if (delta > 0) "+" else ""

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            color = RetroCardBorderLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Text(
            text = "$sign$delta",
            color = deltaColor,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ScaleRow(tag: String, content: String) {
    if (content.isNotBlank()) {
        Column(modifier = Modifier.padding(vertical = 3.dp)) {
            Text(
                text = tag,
                color = RetroGreenPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = content,
                color = RetroGreenText.copy(alpha = 0.85f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(start = 4.dp, top = 1.dp)
            )
        }
    }
}
