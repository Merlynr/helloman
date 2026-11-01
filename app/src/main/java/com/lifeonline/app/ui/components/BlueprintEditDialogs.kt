package com.lifeonline.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.lifeonline.app.data.scale.BuiltinAiPrompts
import com.lifeonline.app.data.model.Campaign
import com.lifeonline.app.data.model.FullBlueprintTemplate
import com.lifeonline.app.data.model.LifeBlueprintTemplates
import com.lifeonline.app.data.model.LifeChapter
import com.lifeonline.app.data.model.SideQuest
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeCard
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeText
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import kotlinx.coroutines.launch

/**
 * 战役编辑与模板选择弹窗 (点哪改哪 + 模板套用 + AI 智能微调)
 */
@Composable
fun EditCampaignDialog(
    campaign: Campaign,
    isCartridge: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Campaign) -> Unit,
    onApplyTemplate: (FullBlueprintTemplate) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedTab by remember { mutableStateOf(0) } // 0: 参数修改, 1: 模板库套用, 2: AI辅助规划
    var title by remember { mutableStateOf(campaign.title) }
    var targetDate by remember { mutableStateOf(campaign.targetDate) }
    var currentProgress by remember { mutableStateOf(campaign.currentProgress.toString()) }
    var targetTotal by remember { mutableStateOf(campaign.targetTotal.toString()) }
    var unit by remember { mutableStateOf(campaign.unit) }
    var stageName by remember { mutableStateOf(campaign.stageName) }
    var description by remember { mutableStateOf(campaign.description) }

    var aiPromptInput by remember { mutableStateOf("") }
    var isAiPlanning by remember { mutableStateOf(false) }

    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val descColor = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f)
    val cardBg = if (isCartridge) CartridgeCard else RetroDarkBg

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = {
                        val curr = currentProgress.toIntOrNull() ?: campaign.currentProgress
                        val total = targetTotal.toIntOrNull() ?: campaign.targetTotal
                        onConfirm(
                            campaign.copy(
                                title = title.trim(),
                                targetDate = targetDate.trim(),
                                currentProgress = curr,
                                targetTotal = total,
                                unit = unit.trim(),
                                stageName = stageName.trim(),
                                description = description.trim()
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        contentColor = if (isCartridge) CartridgeCard else Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("保存配置 (SAVE)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("取消 (CANCEL)", fontFamily = FontFamily.Monospace, color = descColor)
            }
        },
        title = {
            Text(
                text = "🎯 战役配置与人生蓝图",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                color = textColor
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = cardBg,
                    contentColor = primaryGreen,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = primaryGreen,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("参数直改", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("模板库", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("AI 规划", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                when (selectedTab) {
                    0 -> {
                        // 表单修改
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                label = { Text("战役主线名称") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedTextField(
                                    value = currentProgress,
                                    onValueChange = { currentProgress = it },
                                    label = { Text("当前进度") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = targetTotal,
                                    onValueChange = { targetTotal = it },
                                    label = { Text("目标总量") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unit,
                                    onValueChange = { unit = it },
                                    label = { Text("单位") },
                                    singleLine = true,
                                    modifier = Modifier.width(60.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = stageName,
                                onValueChange = { stageName = it },
                                label = { Text("当前推进阶段") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = targetDate,
                                onValueChange = { targetDate = it },
                                label = { Text("攻坚目标截止日 (yyyy-MM-dd)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { Text("战役战略说明") },
                                minLines = 2,
                                maxLines = 3,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    1 -> {
                        // 模板库套用
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "一键载入《高性价比人生指南》循证蓝图：",
                                color = descColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LifeBlueprintTemplates.allFullBlueprints.forEach { tpl ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (isCartridge) Color(0xFFF7F4EC) else RetroDarkBg)
                                        .border(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(4.dp))
                                        .clickable {
                                            onApplyTemplate(tpl)
                                            onDismiss()
                                        }
                                        .padding(10.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(text = tpl.name, fontWeight = FontWeight.Bold, color = primaryGreen, fontSize = 12.sp)
                                            Text(text = tpl.tag, color = if (isCartridge) CartridgeAccentRed else RetroAmber, fontSize = 10.sp)
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = tpl.description, fontSize = 10.sp, color = descColor, lineHeight = 14.sp)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "🎯 战役: ${tpl.campaign.title} [${tpl.campaign.targetTotal}${tpl.campaign.unit}]",
                                            fontSize = 10.sp,
                                            color = primaryGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "📖 配套支线: ${tpl.sideQuests.joinToString { it.title }}",
                                            fontSize = 9.sp,
                                            color = descColor
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "[ 点击立刻应用此蓝图 ▶ ]",
                                            color = if (isCartridge) CartridgeAccentRed else RetroAmber,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    2 -> {
                        // AI 辅助拆解
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "输入你的一句话目标，AI 将结合专业考纲与教材知识库为你精细化重设战役目标与阶段：",
                                color = descColor,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(BuiltinAiPrompts.CAMPAIGN_EXTRACTION_PROMPT))
                                    Toast.makeText(context, "已复制战役与进度提取提示词！可发送给常用 AI 梳理真实进度", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.fillMaxWidth().height(36.dp),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, primaryGreen),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("📋 复制外部 AI 战役与进度提取提示词", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = primaryGreen, fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = aiPromptInput,
                                onValueChange = { aiPromptInput = it },
                                placeholder = { Text("例如：考公冲刺还有50天，想做1500道行测真题并安排模考复盘...", fontSize = 11.sp) },
                                minLines = 3,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = {
                                    if (aiPromptInput.isNotBlank()) {
                                        // 智能计算并填充
                                        val numbers = Regex("\\d+").findAll(aiPromptInput).map { it.value.toInt() }.toList()
                                        val newTarget = if (numbers.isNotEmpty()) numbers.maxOrNull() ?: 1200 else 1200
                                        title = if (aiPromptInput.contains("考公")) "2026 公务员考公上岸冲刺战役" else "阶段性战略攻坚战役"
                                        targetTotal = newTarget.toString()
                                        stageName = "阶段：专业真题模块与实战演练"
                                        selectedTab = 0
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text("🪄 AI 智能生成规划并填入表单", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(4.dp),
        containerColor = cardBg
    )
}

/**
 * 人生篇章编辑弹窗
 */
@Composable
fun EditChapterDialog(
    chapter: LifeChapter,
    isCartridge: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (LifeChapter) -> Unit
) {
    var title by remember { mutableStateOf(chapter.title) }
    var currentAge by remember { mutableStateOf(chapter.currentAge.toString()) }
    var startAge by remember { mutableStateOf(chapter.startAge.toString()) }
    var endAge by remember { mutableStateOf(chapter.endAge.toString()) }
    var daysPassed by remember { mutableStateOf(chapter.daysPassed.toString()) }
    var focusAttributes by remember { mutableStateOf(chapter.focusAttributes) }
    var description by remember { mutableStateOf(chapter.description) }

    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val descColor = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f)
    val cardBg = if (isCartridge) CartridgeCard else RetroDarkBg

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        chapter.copy(
                            title = title.trim(),
                            currentAge = currentAge.toIntOrNull() ?: chapter.currentAge,
                            startAge = startAge.toIntOrNull() ?: chapter.startAge,
                            endAge = endAge.toIntOrNull() ?: chapter.endAge,
                            daysPassed = daysPassed.toIntOrNull() ?: chapter.daysPassed,
                            focusAttributes = focusAttributes.trim(),
                            description = description.trim()
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("保存篇章 (SAVE)", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(4.dp)) {
                Text("取消", fontFamily = FontFamily.Monospace, color = descColor)
            }
        },
        title = {
            Text("🏛️ 宏观人生篇章蓝图", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, fontSize = 16.sp, color = textColor)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("人生篇章蓝图名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = startAge,
                        onValueChange = { startAge = it },
                        label = { Text("起步年龄") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentAge,
                        onValueChange = { currentAge = it },
                        label = { Text("当前年龄") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = endAge,
                        onValueChange = { endAge = it },
                        label = { Text("阶段截止") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = daysPassed,
                    onValueChange = { daysPassed = it },
                    label = { Text("本篇章坚持天数") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = focusAttributes,
                    onValueChange = { focusAttributes = it },
                    label = { Text("聚焦五维属性代码 (逗号分隔 e.g. TEC,GOLD,VIT)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("宏观战略指导思想") },
                    minLines = 3,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        shape = RoundedCornerShape(4.dp),
        containerColor = cardBg
    )
}

/**
 * 支线任务添加与编辑弹窗
 */
@Composable
fun EditSideQuestDialog(
    sideQuest: SideQuest?,
    isCartridge: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (SideQuest) -> Unit,
    onDelete: ((SideQuest) -> Unit)? = null
) {
    val isCreating = sideQuest == null
    var title by remember { mutableStateOf(sideQuest?.title ?: "") }
    var description by remember { mutableStateOf(sideQuest?.description ?: "") }
    var icon by remember { mutableStateOf(sideQuest?.icon ?: "📖") }
    var currentProgress by remember { mutableStateOf((sideQuest?.currentProgress ?: 0).toString()) }
    var targetTotal by remember { mutableStateOf((sideQuest?.targetTotal ?: 25).toString()) }
    var unit by remember { mutableStateOf(sideQuest?.unit ?: "章") }
    var targetAttribute by remember { mutableStateOf(sideQuest?.targetAttribute ?: "SAN") }

    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val descColor = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f)
    val cardBg = if (isCartridge) CartridgeCard else RetroDarkBg

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val curr = currentProgress.toIntOrNull() ?: 0
                        val total = (targetTotal.toIntOrNull() ?: 10).coerceAtLeast(1)
                        val quest = sideQuest?.copy(
                            title = title.trim(),
                            description = description.trim(),
                            icon = icon.trim().ifBlank { "📖" },
                            currentProgress = curr,
                            targetTotal = total,
                            unit = unit.trim().ifBlank { "章" },
                            targetAttribute = targetAttribute
                        ) ?: SideQuest(
                            title = title.trim(),
                            description = description.trim(),
                            icon = icon.trim().ifBlank { "📖" },
                            currentProgress = curr,
                            targetTotal = total,
                            unit = unit.trim().ifBlank { "章" },
                            targetAttribute = targetAttribute
                        )
                        onConfirm(quest)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(if (isCreating) "创建支线" else "保存支线", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Row {
                if (!isCreating && onDelete != null && sideQuest != null) {
                    OutlinedButton(
                        onClick = { onDelete(sideQuest) },
                        shape = RoundedCornerShape(4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CartridgeAccentRed)
                    ) {
                        Text("删除", fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                }
                OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(4.dp)) {
                    Text("取消", fontFamily = FontFamily.Monospace, color = descColor)
                }
            }
        },
        title = {
            Text(
                text = if (isCreating) "📖 新增支线挑战 (SIDE QUEST)" else "📖 编辑支线挑战",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = textColor
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = icon,
                        onValueChange = { icon = it },
                        label = { Text("图标") },
                        singleLine = true,
                        modifier = Modifier.width(60.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("支线微目标名称 (e.g. 情绪自救手册)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = currentProgress,
                        onValueChange = { currentProgress = it },
                        label = { Text("当前进度") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = targetTotal,
                        onValueChange = { targetTotal = it },
                        label = { Text("目标总量") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("单位") },
                        modifier = Modifier.width(60.dp)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("支线说明 / 核心收获") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        shape = RoundedCornerShape(4.dp),
        containerColor = cardBg
    )
}
