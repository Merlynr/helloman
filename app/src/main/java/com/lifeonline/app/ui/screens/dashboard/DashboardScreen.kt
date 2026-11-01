package com.lifeonline.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.AvatarDisplay
import com.lifeonline.app.ui.components.BuffChip
import com.lifeonline.app.ui.components.EditProfileDialog
import com.lifeonline.app.ui.components.RadarChart
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.components.StatBar
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeCard
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeText
import com.lifeonline.app.ui.theme.CartridgeTextMuted
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
    val latestSleepRecord by repository.latestSleepRecordFlow.collectAsState()
    val sedentaryStatus by repository.sedentaryStatusFlow.collectAsState()
    val scope = rememberCoroutineScope()
    val isCartridge = LocalIsCartridgeTheme.current
    var isEditingProfile by remember { mutableStateOf(false) }

    val stats = userStats ?: UserStats()
    val latestLog = dailyLogs.firstOrNull()

    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val subtitleColor = if (isCartridge) CartridgeAccentRed else RetroAmber
    val mutedColor = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isCartridge) CartridgeBg else RetroDarkBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 顶部角色基础状态栏 (Player Status Card - 可点击更换头像与修改姓名)
        RetroContainer(title = "STATUS 角色属性面板", subtitle = "ONLINE") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { isEditingProfile = true }
                    .padding(vertical = 2.dp)
            ) {
                // 卡头图腾头像
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCartridge) Color(0xFFEDE9DF) else RetroDarkBg)
                        .border(1.5.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    AvatarDisplay(avatar = stats.avatar, size = 46.dp, fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    // 第一行：玩家名称 + 等级徽章 + 更换提示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stats.name,
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // 等级徽章 (紧凑胶囊)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCartridge) CartridgeGreen else RetroDarkBg)
                                .border(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(3.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Lv. ${stats.level}",
                                color = if (isCartridge) CartridgeCard else RetroGreenPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = "⚙️换头像",
                            color = if (isCartridge) CartridgeTextMuted else RetroCardBorder,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // 第二行：称号全宽排版
                    Text(
                        text = "称号: ${stats.title}",
                        color = subtitleColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            if (stats.aiTitle.isNotBlank() && stats.aiTitle != stats.title) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "✦ 动态: ${stats.aiTitle}",
                    color = if (isCartridge) CartridgeGreen else RetroAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 经验值进度条
            StatBar(
                label = "EXP 经验值",
                currentValue = stats.exp,
                maxValue = stats.maxExp,
                barColor = if (isCartridge) CartridgeAccentRed else RetroAmber
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

            // 实时身体节律状态标签
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val isLate = latestSleepRecord?.isLateNight == true
                val sleepText = if (latestSleepRecord != null) {
                    if (isLate) "🌙 昨夜熬夜 (${latestSleepRecord?.statusTitle})"
                    else "🌙 昨夜作息良好 (${latestSleepRecord?.statusTitle})"
                } else {
                    "🌙 传感器监测中 · 22:30标尺"
                }
                Text(
                    text = sleepText,
                    fontSize = 11.sp,
                    color = if (isLate) CartridgeAccentRed else (if (isCartridge) CartridgeGreen else RetroGreenPrimary),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                if (sedentaryStatus.isSedentary) {
                    Text(
                        text = "⚠️ 伏案${sedentaryStatus.inactiveMinutes}m (待走100步)",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CartridgeAccentRed,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    Text(
                        text = "● 经络通畅",
                        fontSize = 10.sp,
                        color = if (isCartridge) CartridgeGreen else RetroGreenPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 五维属性雷达图 (5D Attribute Radar)
        RetroContainer(title = "五维成长雷达图 (5D STATS)") {
            RadarChart(
                tec = stats.tec,
                vit = stats.vit,
                san = stats.san,
                gold = stats.gold,
                rel = stats.bnd
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 属性数值横排
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatValueItem("算力 TEC", stats.tec, isCartridge, stats.tecStreak)
                StatValueItem("气血 VIT", stats.vit, isCartridge, stats.vitStreak)
                StatValueItem("心力 SAN", stats.san, isCartridge, stats.sanStreak)
                StatValueItem("财商 GOLD", stats.gold, isCartridge, stats.goldStreak)
                StatValueItem("人脉 BND", stats.bnd, isCartridge, stats.bndStreak)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 状态增益与负面状态 (BUFF / DEBUFF)
        RetroContainer(title = "当前生效的 BUFF / DEBUFF", subtitle = "${buffs.size} 个状态") {
            if (buffs.isEmpty()) {
                Text(
                    text = "暂无特殊状态，保持高性价比规律自律可解锁强力 BUFF！",
                    color = mutedColor,
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
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text(
                        text = "掉落经验: +${latestLog.expGained} | 传感器步数: ${latestLog.stepCount}步",
                        color = subtitleColor,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // 角色换肤与改名弹窗
    if (isEditingProfile) {
        EditProfileDialog(
            userStats = stats,
            isCartridge = isCartridge,
            onDismiss = { isEditingProfile = false },
            onConfirm = { newName, newAvatar ->
                scope.launch {
                    repository.updateProfile(newName, newAvatar)
                }
                isEditingProfile = false
            }
        )
    }
}

@Composable
private fun StatValueItem(label: String, value: Int, isCartridge: Boolean, streak: Int = 0) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value.toString(),
            color = if (isCartridge) CartridgeGreen else RetroGreenPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Black,
            fontSize = 15.sp
        )
        Text(
            text = label,
            color = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        if (streak >= 3) {
            Text(
                text = "🔥${streak}d",
                color = CartridgeAccentRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
