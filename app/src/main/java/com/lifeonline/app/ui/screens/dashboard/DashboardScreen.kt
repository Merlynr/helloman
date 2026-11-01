package com.lifeonline.app.ui.screens.dashboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.BuffChip
import com.lifeonline.app.ui.components.RadarChart
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.components.StatBar
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroBlue
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import com.lifeonline.app.ui.theme.RetroRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardScreen(repository: LifeOnlineRepository) {
    val userStats by repository.userStatsFlow.collectAsState(initial = UserStats())
    val buffs by repository.buffsFlow.collectAsState(initial = emptyList())
    val steps by repository.stepsFlow.collectAsState()
    val dailyLogs by repository.dailyLogsFlow.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    val stats = userStats ?: UserStats()
    val latestLog = dailyLogs.firstOrNull()

    val isCartridge = LocalIsCartridgeTheme.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isCartridge) CartridgeBg else RetroDarkBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部角色基础状态栏 (Player Status Card)
        RetroContainer(title = "STATUS 角色属性面板", subtitle = "ONLINE") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stats.name,
                        color = RetroGreenText,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "称号: ${stats.title}",
                        color = RetroAmber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp
                    )
                }
                // 等级徽章
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(RetroDarkBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lv. ${stats.level}",
                        color = RetroGreenPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 经验值进度条
            StatBar(
                label = "EXP 经验值",
                currentValue = stats.exp,
                maxValue = stats.maxExp,
                barColor = RetroAmber
            )

            // 生命值 (HP) 与 精力值 (MP)
            StatBar(
                label = "HP 体魄活力",
                currentValue = stats.hp,
                maxValue = stats.maxHp,
                barColor = RetroRed
            )
            StatBar(
                label = "MP 心智专注",
                currentValue = stats.mp,
                maxValue = stats.maxMp,
                barColor = RetroBlue
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 五维属性雷达图 (5D Attribute Radar)
        RetroContainer(title = "五维成长雷达图 (5D STATS)") {
            RadarChart(
                tec = stats.tec,
                vit = stats.vit,
                san = stats.san,
                gold = stats.gold,
                rel = stats.rel
            )

            Spacer(modifier = Modifier.height(6.dp))

            // 属性数值横排
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatValueItem("智力 TEC", stats.tec)
                StatValueItem("气血 VIT", stats.vit)
                StatValueItem("心力 SAN", stats.san)
                StatValueItem("财力 GOLD", stats.gold)
                StatValueItem("人脉 REL", stats.rel)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 状态增益与负面状态 (BUFF / DEBUFF)
        RetroContainer(title = "当前生效的 BUFF / DEBUFF", subtitle = "${buffs.size} 个状态") {
            if (buffs.isEmpty()) {
                Text(
                    text = "暂无特殊状态，保持高性价比规律自律可解锁强力 BUFF！",
                    color = RetroCardBorder,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    buffs.forEach { buff ->
                        BuffChip(
                            buff = buff,
                            onRemove = {
                                scope.launch { repository.deleteBuff(buff) }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. 最新战报结算摘要 (Latest Dialogue)
        if (latestLog != null) {
            RetroContainer(title = "最新结算战报", subtitle = latestLog.dateString) {
                Text(
                    text = latestLog.dialogue,
                    color = RetroGreenText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text(
                        text = "掉落经验: +${latestLog.expGained} | 传感器步数: ${latestLog.stepCount}步",
                        color = RetroAmber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun StatValueItem(label: String, value: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            color = RetroGreenPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        Text(
            text = label,
            color = RetroGreenText.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
    }
}
