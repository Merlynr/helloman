package com.lifeonline.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.data.model.DailyLog
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroCardBorderLight
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText

@Composable
fun HistoryScreen(repository: LifeOnlineRepository) {
    val dailyLogs by repository.dailyLogsFlow.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroDarkBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        RetroContainer(title = "历程档案与成长复盘", subtitle = "共 ${dailyLogs.size} 条记录") {
            Text(
                text = "现实生活的每一步都有痕迹。回顾多周期成长复盘，检验长期复利。",
                color = RetroGreenText.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (dailyLogs.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "暂无历史战报，前往流水页面完成首次结算吧！",
                    color = RetroCardBorder,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(dailyLogs) { log ->
                    HistoryLogCard(log)
                }
            }
        }
    }
}

@Composable
private fun HistoryLogCard(log: DailyLog) {
    RetroContainer(
        title = log.dateString,
        subtitle = "步数: ${log.stepCount}步 | +${log.expGained} EXP"
    ) {
        // 用户流水摘要
        Text(
            text = "用户流水: ${log.rawInput}",
            color = RetroCardBorderLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // AI 旁白
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(RetroDarkBg)
                .padding(8.dp)
        ) {
            Text(
                text = log.dialogue,
                color = RetroGreenText,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 属性变动一览
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "智力: ${formatDelta(log.tecDelta)}", color = RetroGreenPrimary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Text(text = "气血: ${formatDelta(log.vitDelta)}", color = RetroGreenPrimary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Text(text = "心力: ${formatDelta(log.sanDelta)}", color = RetroGreenPrimary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Text(text = "财力: ${formatDelta(log.goldDelta)}", color = RetroAmber, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
            Text(text = "人脉: ${formatDelta(log.relDelta)}", color = RetroGreenPrimary, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        }

        if (log.longTermEval.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "长期指南: ${log.longTermEval}",
                color = RetroAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                lineHeight = 14.sp
            )
        }
    }
}

private fun formatDelta(delta: Int): String {
    return if (delta >= 0) "+$delta" else "$delta"
}
