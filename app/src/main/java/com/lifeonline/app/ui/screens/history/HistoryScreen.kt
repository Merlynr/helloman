package com.lifeonline.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.data.model.Achievement
import com.lifeonline.app.data.model.AchievementRarity
import com.lifeonline.app.data.model.DailyLog
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeCard
import com.lifeonline.app.ui.theme.CartridgeCardWhite
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeGreenLight
import com.lifeonline.app.ui.theme.CartridgeText
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroCardBorderLight
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class HistoryTab(val title: String, val enTag: String) {
    BADGES("成就徽章", "BADGES"),
    LOGS("历程战报", "ARCHIVE")
}

@Composable
fun HistoryScreen(repository: LifeOnlineRepository) {
    val dailyLogs by repository.dailyLogsFlow.collectAsState(initial = emptyList())
    val achievements by repository.achievementsFlow.collectAsState(initial = emptyList())
    val userStats by repository.userStatsFlow.collectAsState(initial = null)
    val isCartridge = LocalIsCartridgeTheme.current

    var selectedTab by remember { mutableStateOf(HistoryTab.BADGES) }
    var selectedAchievement by remember { mutableStateOf<Achievement?>(null) }

    val unlockedCount = achievements.count { it.unlockedAt != null }
    val totalCount = if (achievements.isEmpty()) 40 else achievements.size
    val latestUnlocked = achievements.filter { it.unlockedAt != null }.maxByOrNull { it.unlockedAt ?: 0L }

    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val descColor = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isCartridge) CartridgeBg else RetroDarkBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // 顶部 Tab 切换 (卡带纸质风格)
        TabRow(
            selectedTabIndex = selectedTab.ordinal,
            containerColor = if (isCartridge) CartridgeCard else RetroDarkBg,
            contentColor = primaryGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = primaryGreen,
                    height = 2.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .border(1.2.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(4.dp))
        ) {
            HistoryTab.values().forEach { tab ->
                val isSelected = selectedTab == tab
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Text(
                            text = "${tab.title} (${tab.enTag})",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isSelected) primaryGreen else descColor
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        when (selectedTab) {
            HistoryTab.BADGES -> {
                // 成就强化板 (小MO 风格 4 列网格)
                RetroContainer(
                    title = "BADGES 成就徽章",
                    subtitle = "($unlockedCount/$totalCount)"
                ) {
                    if (achievements.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "正在同步人生里程碑与成就档案...",
                                color = descColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            contentPadding = PaddingValues(2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                        ) {
                            items(achievements) { ach ->
                                BadgeGridItem(
                                    achievement = ach,
                                    isCartridge = isCartridge,
                                    onClick = { selectedAchievement = ach }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 底部小MO风格当前称号与最近解锁
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCartridge) Color(0xFFFAF7F0) else RetroDarkBg)
                            .border(1.dp, if (isCartridge) CartridgeBorder.copy(alpha = 0.5f) else RetroCardBorder, RoundedCornerShape(4.dp))
                            .padding(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎖️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "当前称号: 「${userStats?.aiTitle?.ifBlank { userStats?.title ?: "稳健求索的学者" }}」",
                                    color = if (isCartridge) CartridgeGreen else RetroGreenPrimary,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                            if (latestUnlocked != null) {
                                Spacer(modifier = Modifier.height(3.dp))
                                val unlockTimeStr = remember(latestUnlocked.unlockedAt) {
                                    latestUnlocked.unlockedAt?.let {
                                        SimpleDateFormat("MM-dd", Locale.getDefault()).format(Date(it))
                                    } ?: ""
                                }
                                Text(
                                    text = "最近解锁: ${latestUnlocked.title} · ${latestUnlocked.description} ($unlockTimeStr)",
                                    color = if (isCartridge) CartridgeAccentRed else RetroAmber,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            HistoryTab.LOGS -> {
                // 历程战报卡片流
                if (dailyLogs.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无历史战报，前往流水页面完成首次结算吧！",
                            color = descColor,
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
                            HistoryLogCard(log, isCartridge)
                        }
                    }
                }
            }
        }
    }

    // 成就详情弹窗 (展示来自《高性价比人生指南》硬核知识与解锁状态)
    selectedAchievement?.let { ach ->
        val isUnlocked = ach.unlockedAt != null
        AlertDialog(
            onDismissRequest = { selectedAchievement = null },
            confirmButton = {
                Button(
                    onClick = { selectedAchievement = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCartridge) CartridgeGreen else RetroGreenPrimary
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "明白 (CONFIRM)",
                        color = if (isCartridge) CartridgeCard else Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = if (isUnlocked || !ach.isHidden) ach.icon else "🔒", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isUnlocked || !ach.isHidden) ach.title else "??????",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        color = if (isCartridge) CartridgeText else RetroGreenText
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "稀有度: ${ach.rarity.label} | 分类: ${ach.category.label}",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = if (isCartridge) CartridgeAccentRed else RetroAmber
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isUnlocked || !ach.isHidden) ach.description else "此为隐藏成就，在生活中做出对应决策或解决该问题时自动解锁。",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = if (isCartridge) CartridgeText else RetroGreenText,
                        lineHeight = 16.sp
                    )

                    if (ach.guideTip.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isCartridge) Color(0xFFF4EFE6) else RetroDarkBg)
                                .border(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(4.dp))
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "📖《高性价比人生指南》${ach.guideChapter}:",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = if (isCartridge) CartridgeGreen else RetroGreenPrimary
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = ach.guideTip,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = if (isCartridge) CartridgeText else RetroGreenText,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    if (isUnlocked) {
                        Spacer(modifier = Modifier.height(6.dp))
                        val dateText = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(ach.unlockedAt ?: 0L))
                        Text(
                            text = "解锁时间: $dateText",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = descColor
                        )
                    }
                }
            },
            shape = RoundedCornerShape(4.dp),
            containerColor = if (isCartridge) CartridgeCard else RetroDarkBg
        )
    }
}

@Composable
private fun BadgeGridItem(
    achievement: Achievement,
    isCartridge: Boolean,
    onClick: () -> Unit
) {
    val isUnlocked = achievement.unlockedAt != null
    val cardBg = when {
        isUnlocked -> if (isCartridge) CartridgeCardWhite else Color(0xFF0F1E16)
        else -> if (isCartridge) Color(0xFFEDE9DF) else RetroDarkBg
    }
    val borderColor = when {
        isUnlocked -> when (achievement.rarity) {
            AchievementRarity.GOLD -> Color(0xFFD4AF37)
            AchievementRarity.SECRET -> Color(0xFFE67E22)
            AchievementRarity.SILVER -> Color(0xFFA8A9AD)
            AchievementRarity.BRONZE -> Color(0xFFCD7F32)
        }
        else -> if (isCartridge) CartridgeBorder.copy(alpha = 0.35f) else RetroCardBorder.copy(alpha = 0.3f)
    }

    Box(
        modifier = Modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(4.dp))
            .background(cardBg)
            .border(1.2.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // 图标
            Text(
                text = if (isUnlocked) achievement.icon else if (achievement.isHidden) "?" else "🔒",
                fontSize = 20.sp,
                textAlign = TextAlign.Center
            )

            // 标题
            Text(
                text = if (isUnlocked || !achievement.isHidden) achievement.title else "??????",
                color = if (isUnlocked) {
                    if (isCartridge) CartridgeText else RetroGreenText
                } else {
                    if (isCartridge) CartridgeTextMuted.copy(alpha = 0.6f) else RetroCardBorderLight
                },
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isUnlocked) FontWeight.Bold else FontWeight.Normal,
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            // 稀有度角标 (铜/银/金/隐·彩)
            Text(
                text = achievement.rarity.label,
                color = when (achievement.rarity) {
                    AchievementRarity.GOLD -> Color(0xFFD4AF37)
                    AchievementRarity.SECRET -> Color(0xFFE67E22)
                    AchievementRarity.SILVER -> Color(0xFFA8A9AD)
                    AchievementRarity.BRONZE -> Color(0xFFCD7F32)
                },
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 8.sp
            )
        }
    }
}

@Composable
private fun HistoryLogCard(log: DailyLog, isCartridge: Boolean) {
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val quoteBg = if (isCartridge) Color(0xFFF7F4EC) else RetroDarkBg
    val quoteBorder = if (isCartridge) CartridgeBorder.copy(alpha = 0.4f) else RetroCardBorder

    RetroContainer(
        title = log.dateString,
        subtitle = "步数: ${log.stepCount}步 | +${log.expGained} EXP"
    ) {
        // 用户流水摘要
        Text(
            text = "用户流水: ${log.rawInput}",
            color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        // AI 旁白 / 解锁战报
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
                .background(quoteBg)
                .border(1.dp, quoteBorder, RoundedCornerShape(4.dp))
                .padding(8.dp)
        ) {
            Text(
                text = log.dialogue,
                color = textColor,
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
            Text(text = "智力: ${formatDelta(log.tecDelta)}", color = primaryGreen, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "气血: ${formatDelta(log.vitDelta)}", color = primaryGreen, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "心力: ${formatDelta(log.sanDelta)}", color = primaryGreen, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "财力: ${formatDelta(log.goldDelta)}", color = accentRed, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(text = "人脉: ${formatDelta(log.relDelta)}", color = primaryGreen, fontFamily = FontFamily.Monospace, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        if (log.longTermEval.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "长期指南: ${log.longTermEval}",
                color = accentRed,
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
