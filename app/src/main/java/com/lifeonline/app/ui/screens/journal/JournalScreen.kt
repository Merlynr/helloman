package com.lifeonline.app.ui.screens.journal

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.lifeonline.app.data.model.Campaign
import com.lifeonline.app.data.model.SideQuest
import com.lifeonline.app.data.model.DecomposedTask
import com.lifeonline.app.data.model.LifeChapter
import com.lifeonline.app.data.model.ChineseLifeCourseFramework
import com.lifeonline.app.data.model.EvidenceBasedChapter
import com.lifeonline.app.data.scale.LifeVelocityCalculator
import com.lifeonline.app.data.scale.LifeVelocityResult
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.TaskItem
import com.lifeonline.app.data.model.TaskType
import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.EditCampaignDialog
import com.lifeonline.app.ui.components.EditChapterDialog
import com.lifeonline.app.ui.components.EditSideQuestDialog
import com.lifeonline.app.ui.components.PixelSegmentedBar
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.components.SettlementDialog
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
import com.lifeonline.app.ui.theme.RetroCardBg
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroCardBorderLight
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import com.lifeonline.app.ui.theme.RetroMuted
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 流水模块三种并列嵌套模式：
 * 1. DAILY (日课): 每日任务打卡、传感器采集、语音AI拆解、流水账战报结算
 * 2. CAMPAIGN (战役): 阶段性攻坚目标跟踪（如：2026 公务员考试冲刺）
 * 3. CHAPTER (篇章): 宏观人生阶段蓝图（如：25~35 岁 · 立业与职业护城河黄金十年）
 */
enum class JournalMode(val title: String, val icon: String, val enTag: String) {
    DAILY("日课", "📅", "DAILY"),
    CAMPAIGN("战役", "🎯", "CAMPAIGN"),
    CHAPTER("篇章", "🏛️", "CHAPTER")
}

@Composable
fun JournalScreen(repository: LifeOnlineRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isCartridge = LocalIsCartridgeTheme.current

    // 观察数据流
    val tasks by repository.tasksFlow.collectAsState(initial = emptyList())
    val steps by repository.stepsFlow.collectAsState()
    val userStats by repository.userStatsFlow.collectAsState(initial = null)
    val activeCampaign by repository.activeCampaignFlow.collectAsState(initial = null)
    val activeChapter by repository.activeChapterFlow.collectAsState(initial = null)
    val sideQuests by repository.sideQuestsFlow.collectAsState(initial = emptyList())

    // 弹窗状态 (点哪改哪)
    var editingCampaign by remember { mutableStateOf<Campaign?>(null) }
    var editingChapter by remember { mutableStateOf<LifeChapter?>(null) }
    var editingSideQuest by remember { mutableStateOf<SideQuest?>(null) }
    var isCreatingSideQuest by remember { mutableStateOf(false) }

    // 模式切换状态 (默认以日课为主)
    var currentMode by remember { mutableStateOf(JournalMode.DAILY) }

    // 日常记账与单条任务输入
    var journalInput by remember {
        mutableStateOf("上午行测专项刷题50道，公安核心法条背诵40分钟，午餐自制花费18元，傍晚快走4000步。")
    }
    var newTaskTitle by remember { mutableStateOf("") }
    var isSettling by remember { mutableStateOf(false) }
    var activeSettlement by remember { mutableStateOf<SettlementResult?>(null) }

    // 语音输入与 AI 任务拆解状态
    var voiceInputText by remember { mutableStateOf("") }
    var isRecording by remember { mutableStateOf(false) }
    var isTranscribing by remember { mutableStateOf(false) }
    var isDecomposing by remember { mutableStateOf(false) }
    var candidateTasks by remember { mutableStateOf<List<DecomposedTask>>(emptyList()) }

    // 录音权限动态申请
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val started = repository.voiceRecorder.startRecording()
            if (started) {
                isRecording = true
                Toast.makeText(context, "正在录音，请清晰说出今日安排...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "启动录音失败，请重试", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "需要麦克风权限才能进行语音输入", Toast.LENGTH_LONG).show()
        }
    }

    fun handleMicClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        } else {
            val started = repository.voiceRecorder.startRecording()
            if (started) {
                isRecording = true
                Toast.makeText(context, "正在录音，请清晰说出今日安排...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "启动录音失败，请重试", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val screenBg = if (isCartridge) CartridgeBg else RetroDarkBg
    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ================= 顶部玩家存档头图 (Retro Save Card - 点击可编辑篇章) =================
        TopArchiveCard(
            userStats = userStats,
            activeChapter = activeChapter,
            dateStr = todayDateStr,
            isCartridge = isCartridge,
            onClick = { editingChapter = activeChapter }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ================= 三种并列模式切换标签栏 (MODE SELECTOR) =================
        ModeSelectorBar(
            currentMode = currentMode,
            onSelectMode = { currentMode = it },
            isCartridge = isCartridge
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ================= 模式内容分支渲染 =================
        when (currentMode) {
            JournalMode.DAILY -> {
                DailyModeContent(
                    tasks = tasks,
                    steps = steps,
                    activeCampaign = activeCampaign,
                    journalInput = journalInput,
                    onJournalInputChange = { journalInput = it },
                    newTaskTitle = newTaskTitle,
                    onNewTaskTitleChange = { newTaskTitle = it },
                    voiceInputText = voiceInputText,
                    onVoiceInputChange = { voiceInputText = it },
                    isRecording = isRecording,
                    isTranscribing = isTranscribing,
                    isDecomposing = isDecomposing,
                    isSettling = isSettling,
                    candidateTasks = candidateTasks,
                    onCandidateTasksChange = { candidateTasks = it },
                    onMicClick = { handleMicClick() },
                    onStopRecording = {
                        val audioFile = repository.voiceRecorder.stopRecording()
                        isRecording = false
                        if (audioFile != null && audioFile.exists() && audioFile.length() > 0) {
                            scope.launch {
                                isTranscribing = true
                                try {
                                    val text = repository.transcribeAudio(audioFile)
                                    if (text.isNotBlank()) {
                                        voiceInputText = text
                                        isDecomposing = true
                                        val parsed = repository.decomposeInputToTasks(text)
                                        candidateTasks = parsed
                                        Toast.makeText(context, "转写成功并已智能拆解！", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "未识别到有效语音，请重试", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "语音识别异常: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isTranscribing = false
                                    isDecomposing = false
                                }
                            }
                        } else {
                            Toast.makeText(context, "录音时间过短或为空", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCancelRecording = {
                        repository.voiceRecorder.cancelRecording()
                        isRecording = false
                    },
                    onTriggerDecompose = {
                        scope.launch {
                            isDecomposing = true
                            try {
                                val parsed = repository.decomposeInputToTasks(voiceInputText)
                                candidateTasks = parsed
                                if (parsed.isEmpty()) {
                                    Toast.makeText(context, "未识别出明确任务，请再补充描述", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "拆解异常: ${e.message}", Toast.LENGTH_SHORT).show()
                            } finally {
                                isDecomposing = false
                            }
                        }
                    },
                    onConfirmCandidateTasks = {
                        val selectedCount = candidateTasks.count { it.isSelected }
                        scope.launch {
                            repository.addBatchTasks(candidateTasks)
                            candidateTasks = emptyList()
                            voiceInputText = ""
                            Toast.makeText(context, "已成功添加 $selectedCount 项任务至今日清单！", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onToggleTask = { task ->
                        scope.launch {
                            repository.toggleTask(task)
                            if (task.campaignId != null) {
                                val addNum = if (task.campaignProgressAdd > 0) task.campaignProgressAdd else 1
                                val action = if (!task.isCompleted) "推进 +$addNum" else "撤销 -$addNum"
                                Toast.makeText(context, "联动打卡成功！2026考公战役 $action 题", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onDeleteTask = { task -> scope.launch { repository.deleteTask(task) } },
                    onAddSingleTask = {
                        if (newTaskTitle.isNotBlank()) {
                            scope.launch {
                                val isExam = newTaskTitle.contains("题") || newTaskTitle.contains("行测") ||
                                        newTaskTitle.contains("申论") || newTaskTitle.contains("考公")
                                val cId = if (isExam && activeCampaign != null) activeCampaign?.id else null
                                val cTag = if (isExam) "🎯 2026考公" else ""
                                val cAdd = if (isExam) 50 else 0

                                repository.addTask(
                                    title = newTaskTitle.trim(),
                                    type = TaskType.DAILY,
                                    attribute = if (isExam) "TEC" else "VIT",
                                    exp = 20,
                                    campaignId = cId,
                                    campaignTag = cTag,
                                    progressAdd = cAdd
                                )
                                newTaskTitle = ""
                            }
                        }
                    },
                    onExecuteSettlement = {
                        scope.launch {
                            isSettling = true
                            try {
                                val res = repository.executeSettlement(journalInput)
                                activeSettlement = res
                            } finally {
                                isSettling = false
                            }
                        }
                    },
                    onNavigateToCampaign = { currentMode = JournalMode.CAMPAIGN },
                    sideQuests = sideQuests,
                    onEditCampaign = { editingCampaign = activeCampaign },
                    onEditSideQuest = { editingSideQuest = it },
                    onBoostSideQuest = { id ->
                        scope.launch {
                            repository.boostSideQuest(id, 1)
                            Toast.makeText(context, "支线微目标已推进 +1 节点！获得 EXP", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCreateSideQuest = { isCreatingSideQuest = true },
                    isCartridge = isCartridge
                )
            }

            JournalMode.CAMPAIGN -> {
                CampaignModeContent(
                    campaign = activeCampaign,
                    chapter = activeChapter,
                    onBoost = { amount ->
                        activeCampaign?.let { c ->
                            scope.launch {
                                repository.boostCampaign(c.id, amount)
                                Toast.makeText(context, "刷题进度已增加 +$amount 题！", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onEditCampaign = { editingCampaign = activeCampaign },
                    onNavigateToChapter = { currentMode = JournalMode.CHAPTER },
                    onNavigateToDaily = { currentMode = JournalMode.DAILY },
                    isCartridge = isCartridge
                )
            }

            JournalMode.CHAPTER -> {
                ChapterModeContent(
                    chapter = activeChapter,
                    userStats = userStats,
                    steps = steps,
                    activeCampaign = activeCampaign,
                    onEditChapter = { editingChapter = activeChapter },
                    onNavigateToCampaign = { currentMode = JournalMode.CAMPAIGN },
                    isCartridge = isCartridge
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // 战役编辑与模板套用弹窗
    editingCampaign?.let { camp ->
        EditCampaignDialog(
            campaign = camp,
            isCartridge = isCartridge,
            onDismiss = { editingCampaign = null },
            onConfirm = { updated ->
                scope.launch { repository.updateCampaign(updated) }
                editingCampaign = null
                Toast.makeText(context, "战役配置已更新", Toast.LENGTH_SHORT).show()
            },
            onApplyTemplate = { template ->
                scope.launch {
                    repository.applyFullBlueprint(template)
                    Toast.makeText(context, "已成功载入「${template.name}」人生蓝图！", Toast.LENGTH_SHORT).show()
                }
                editingCampaign = null
            }
        )
    }

    // 篇章编辑弹窗
    editingChapter?.let { ch ->
        EditChapterDialog(
            chapter = ch,
            isCartridge = isCartridge,
            onDismiss = { editingChapter = null },
            onConfirm = { updated ->
                scope.launch { repository.updateChapter(updated) }
                editingChapter = null
                Toast.makeText(context, "人生篇章已更新", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 支线微目标新增/编辑弹窗
    if (editingSideQuest != null || isCreatingSideQuest) {
        EditSideQuestDialog(
            sideQuest = editingSideQuest,
            isCartridge = isCartridge,
            onDismiss = {
                editingSideQuest = null
                isCreatingSideQuest = false
            },
            onConfirm = { quest ->
                scope.launch {
                    if (quest.id == 0L) {
                        repository.insertSideQuest(quest)
                        Toast.makeText(context, "新支线已创建", Toast.LENGTH_SHORT).show()
                    } else {
                        repository.updateSideQuest(quest)
                        Toast.makeText(context, "支线配置已保存", Toast.LENGTH_SHORT).show()
                    }
                }
                editingSideQuest = null
                isCreatingSideQuest = false
            },
            onDelete = { quest ->
                scope.launch {
                    repository.deleteSideQuest(quest)
                    Toast.makeText(context, "支线已移除", Toast.LENGTH_SHORT).show()
                }
                editingSideQuest = null
                isCreatingSideQuest = false
            }
        )
    }

    // 战报结算弹窗
    activeSettlement?.let { settlement ->
        SettlementDialog(
            result = settlement,
            onDismiss = { activeSettlement = null }
        )
    }
}

// =========================================================================
// 1. 顶部复古存档卡片 (Top Archive Header)
// =========================================================================
@Composable
private fun TopArchiveCard(
    userStats: UserStats?,
    activeChapter: LifeChapter?,
    dateStr: String,
    isCartridge: Boolean,
    onClick: () -> Unit = {}
) {
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val cardBg = if (isCartridge) CartridgeCardWhite else RetroCardBg
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(cardBg)
            .border(2.dp, borderColor, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 顶栏：SAVE/存档反白徽章 + 日期 + 状态
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isCartridge) CartridgeGreen else RetroGreenPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "SAVE / 存档",
                        color = if (isCartridge) CartridgeCard else RetroDarkBg,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "DAILY LOG · $dateStr",
                    color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 角色与阶段数据行
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 头像方框
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCartridge) Color(0xFFE8E3D8) else RetroDarkBg)
                        .border(1.5.dp, borderColor, RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🐻", fontSize = 24.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    val age = activeChapter?.currentAge ?: 25
                    val days = activeChapter?.daysPassed ?: 319
                    val level = userStats?.level ?: 2
                    val exp = userStats?.exp ?: 25
                    val maxExp = userStats?.maxExp ?: 100
                    val remainExp = (maxExp - exp).coerceAtLeast(0)

                    // 第一行：ID + 等级徽章并排紧凑展示
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "ID: ${userStats?.name ?: "玩家"}",
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▶",
                            color = accentRed,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCartridge) CartridgeGreen else RetroDarkBg)
                                .border(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(3.dp))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "Lv.$level",
                                color = if (isCartridge) CartridgeCard else RetroGreenPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // 第二行：紧凑进度，永不折行
                    Text(
                        text = "距下级 ${remainExp}EXP · ${age}岁 · 第${days}天",
                        color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(1.dp))

                    // 第三行：称号，单行防护
                    Text(
                        text = userStats?.title ?: "体制攻坚 × 职业护城河黄金期",
                        color = accentRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =========================================================================
// 2. 三种模式并列切换标签栏 (Mode Selector Bar)
// =========================================================================
@Composable
private fun ModeSelectorBar(
    currentMode: JournalMode,
    onSelectMode: (JournalMode) -> Unit,
    isCartridge: Boolean
) {
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(6.dp))
            .background(if (isCartridge) Color(0xFFECE6DC) else RetroDarkBg)
            .padding(2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        JournalMode.values().forEach { mode ->
            val isSelected = currentMode == mode
            val activeBg = if (isCartridge) CartridgeGreen else RetroGreenPrimary
            val activeText = if (isCartridge) CartridgeCard else RetroDarkBg
            val inactiveText = if (isCartridge) CartridgeText else RetroGreenText

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isSelected) activeBg else Color.Transparent)
                    .clickable { onSelectMode(mode) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = mode.icon,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = mode.title,
                            color = if (isSelected) activeText else inactiveText,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = mode.enTag,
                        color = if (isSelected) activeText.copy(alpha = 0.85f) else (if (isCartridge) CartridgeTextMuted else RetroCardBorderLight),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// =========================================================================
// 3. 模式一：日课 (DAILY) 详细内容
// =========================================================================
@Composable
private fun DailyModeContent(
    tasks: List<TaskItem>,
    steps: Int,
    activeCampaign: Campaign?,
    sideQuests: List<SideQuest>,
    journalInput: String,
    onJournalInputChange: (String) -> Unit,
    newTaskTitle: String,
    onNewTaskTitleChange: (String) -> Unit,
    voiceInputText: String,
    onVoiceInputChange: (String) -> Unit,
    isRecording: Boolean,
    isTranscribing: Boolean,
    isDecomposing: Boolean,
    isSettling: Boolean,
    candidateTasks: List<DecomposedTask>,
    onCandidateTasksChange: (List<DecomposedTask>) -> Unit,
    onMicClick: () -> Unit,
    onStopRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onTriggerDecompose: () -> Unit,
    onConfirmCandidateTasks: () -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onDeleteTask: (TaskItem) -> Unit,
    onAddSingleTask: () -> Unit,
    onExecuteSettlement: () -> Unit,
    onNavigateToCampaign: () -> Unit,
    onEditCampaign: () -> Unit,
    onEditSideQuest: (SideQuest) -> Unit,
    onBoostSideQuest: (Long) -> Unit,
    onCreateSideQuest: () -> Unit,
    isCartridge: Boolean
) {
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText

    // 0. QUEST 战役主线剧情与支线微目标 (小MO 风格沉浸式联动卡片)
    RetroContainer(
        title = "QUEST 主线剧情与支线挑战",
        subtitle = "点卡片可直接编辑修改"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // (1) 主线战役卡片
            if (activeCampaign != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCartridge) Color(0xFFFAF7F0) else Color(0xFF14241B))
                        .border(1.2.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onEditCampaign() }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(text = "🎯", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "主线 · ${activeCampaign.title}",
                                    color = primaryGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${(activeCampaign.progressPercent * 100).toInt()}%",
                                color = accentRed,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // 主线进度条 (复古卡带色)
                        PixelSegmentedBar(
                            progress = activeCampaign.progressPercent,
                            activeColor = accentRed
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "阶段: ${activeCampaign.stageName} [${activeCampaign.currentProgress}/${activeCampaign.targetTotal}${activeCampaign.unit}]",
                                color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "⚙️ 配置/换模板",
                                color = primaryGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // (2) 支线任务列表
            sideQuests.forEach { quest ->
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCartridge) Color(0xFFF6F3EC) else RetroDarkBg)
                        .border(1.dp, if (isCartridge) CartridgeBorder.copy(alpha = 0.5f) else RetroCardBorder, RoundedCornerShape(6.dp))
                        .clickable { onEditSideQuest(quest) }
                        .padding(10.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Text(text = quest.icon, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "支线 · ${quest.title}",
                                    color = textColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${(quest.progressPercent * 100).toInt()}%",
                                    color = primaryGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(primaryGreen)
                                        .clickable { onBoostSideQuest(quest.id) }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "+1 推进",
                                        color = if (isCartridge) CartridgeCard else Color.Black,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 9.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        PixelSegmentedBar(
                            progress = quest.progressPercent,
                            activeColor = primaryGreen
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${quest.description} · [${quest.currentProgress}/${quest.targetTotal}${quest.unit}]",
                            color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // (3) 添加支线按钮
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCartridge) Color(0xFFEFEBE2) else RetroDarkBg)
                    .border(1.dp, if (isCartridge) CartridgeBorder.copy(alpha = 0.4f) else RetroCardBorder, RoundedCornerShape(4.dp))
                    .clickable { onCreateSideQuest() }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ 新增支线挑战 (SIDE QUEST)",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 1. 安卓传感器采集卡片
    RetroContainer(title = "安卓端自动采集", subtitle = "SENSOR ACTIVE") {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "今日计步传感器 (硬件实时同步)",
                    color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
                Text(
                    text = "$steps 步",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCartridge) Color(0xFFEDE9DF) else RetroDarkBg)
                    .border(1.dp, if (isCartridge) CartridgeBorder.copy(alpha = 0.5f) else RetroCardBorder, RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "● 自动记录中",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 2. 任务清单与习惯打卡卡片 (支持语音与AI智能拆解)
    RetroContainer(
        title = "任务清单与习惯打卡",
        subtitle = "${tasks.count { it.isCompleted }}/${tasks.size} 已完成"
    ) {
        // 语音 / 一句话 AI 智能拆解区域
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCartridge) Color(0xFFF3EFE7) else Color(0xFF0F1A13))
                .border(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(6.dp))
                .padding(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = accentRed,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "/// 语音 / 一句话 AI 智能拆解任务",
                    color = accentRed,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 录音状态栏
            if (isRecording) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF331111))
                        .border(1.dp, Color(0xFFFF5252), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF5252))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "正在录音中... 请清晰说出安排",
                        color = Color(0xFFFFCDD2),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )

                    Button(
                        onClick = onStopRecording,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5252),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("完成", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = onCancelRecording,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("取消", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            } else if (isTranscribing) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCartridge) Color(0xFFE8F4FD) else Color(0xFF0F2027))
                        .border(1.dp, Color(0xFF4FC3F7), RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF4FC3F7),
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SenseVoice AI 极速高精度语音转写中...",
                        color = Color(0xFF0288D1),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = voiceInputText,
                        onValueChange = onVoiceInputChange,
                        placeholder = {
                            Text(
                                "点击右侧麦克风录音，或在此打字 (如：做50道行测题，背30分钟法条)",
                                color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor,
                            focusedBorderColor = primaryGreen,
                            unfocusedBorderColor = if (isCartridge) CartridgeBorder else RetroCardBorder,
                            focusedContainerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg,
                            unfocusedContainerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg
                        ),
                        shape = RoundedCornerShape(4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onMicClick,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(primaryGreen)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "开始语音录入",
                            tint = if (isCartridge) CartridgeCard else RetroDarkBg,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onTriggerDecompose,
                enabled = !isDecomposing && !isRecording && !isTranscribing && voiceInputText.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCartridge) CartridgeGreen else RetroDarkBg,
                    contentColor = if (isCartridge) CartridgeCard else RetroGreenPrimary
                ),
                shape = RoundedCornerShape(4.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryGreen)
            ) {
                if (isDecomposing) {
                    CircularProgressIndicator(
                        color = if (isCartridge) CartridgeCard else RetroGreenPrimary,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 语义分析与战役关联标注中...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                } else {
                    Text("🪄 AI 智能拆解为任务清单 (PARSE)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 候选任务预览勾选区
        if (candidateTasks.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCartridge) Color(0xFFF1F6F2) else Color(0xFF0C2417))
                    .border(1.dp, primaryGreen, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Text(
                    text = "✓ AI 识别出 ${candidateTasks.size} 个原子任务（勾选确认后加入今日清单）：",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                candidateTasks.forEachIndexed { index, task ->
                    val isExamTask = task.title.contains("题") || task.title.contains("行测") ||
                            task.title.contains("申论") || task.title.contains("考公")

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCartridge) CartridgeCardWhite else RetroDarkBg)
                            .padding(horizontal = 6.dp, vertical = 6.dp)
                            .clickable {
                                onCandidateTasksChange(candidateTasks.toMutableList().also {
                                    it[index] = it[index].copy(isSelected = !it[index].isSelected)
                                })
                            }
                    ) {
                        Checkbox(
                            checked = task.isSelected,
                            onCheckedChange = { checked ->
                                onCandidateTasksChange(candidateTasks.toMutableList().also {
                                    it[index] = it[index].copy(isSelected = checked)
                                })
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = primaryGreen,
                                uncheckedColor = if (isCartridge) CartridgeBorder else RetroCardBorder,
                                checkmarkColor = if (isCartridge) CartridgeCard else RetroDarkBg
                            ),
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                color = textColor,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isExamTask) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(accentRed)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "🎯 2026考公",
                                            color = Color.White,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(4.dp))
                                }

                                Text(
                                    text = "[${task.targetAttribute}]",
                                    color = primaryGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+${task.expReward} EXP",
                                    color = accentRed,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { onCandidateTasksChange(emptyList()) },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder)
                    ) {
                        Text("取消", color = if (isCartridge) CartridgeTextMuted else RetroMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val selectedCount = candidateTasks.count { it.isSelected }
                    Button(
                        onClick = onConfirmCandidateTasks,
                        enabled = selectedCount > 0,
                        modifier = Modifier.height(34.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryGreen,
                            contentColor = if (isCartridge) CartridgeCard else RetroDarkBg
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("确认添加 ($selectedCount)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 正式任务列表
        tasks.forEach { task ->
            TaskRowItem(
                task = task,
                onToggle = { onToggleTask(task) },
                onDelete = { onDeleteTask(task) },
                isCartridge = isCartridge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 快速手动单条添加任务输入框
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = newTaskTitle,
                onValueChange = onNewTaskTitleChange,
                placeholder = {
                    Text(
                        "手动单条添加 (如: 晚间复盘错题本20分钟)",
                        color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textColor,
                    unfocusedTextColor = textColor,
                    focusedBorderColor = primaryGreen,
                    unfocusedBorderColor = if (isCartridge) CartridgeBorder else RetroCardBorder,
                    focusedContainerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg,
                    unfocusedContainerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg
                ),
                shape = RoundedCornerShape(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            IconButton(
                onClick = onAddSingleTask,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(primaryGreen)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加", tint = if (isCartridge) CartridgeCard else RetroDarkBg)
            }
        }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // 3. 极简流水账随手记与战报结算卡片
    RetroContainer(title = "极简流水账随手记", subtitle = "DAILY LOG") {
        Text(
            text = "写下今日真实行动，AI 将根据《高性价比人生指南》防御标尺折算经验、属性变化与战报。",
            color = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = journalInput,
            onValueChange = onJournalInputChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = textColor,
                unfocusedTextColor = textColor,
                focusedBorderColor = primaryGreen,
                unfocusedBorderColor = if (isCartridge) CartridgeBorder else RetroCardBorder,
                focusedContainerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg,
                unfocusedContainerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg
            ),
            shape = RoundedCornerShape(4.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onExecuteSettlement,
            enabled = !isSettling && journalInput.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = primaryGreen,
                contentColor = if (isCartridge) CartridgeCard else RetroDarkBg
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            if (isSettling) {
                CircularProgressIndicator(
                    color = if (isCartridge) CartridgeCard else RetroDarkBg,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI 裁决经验中...", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            } else {
                Text(
                    text = "⚡ 今日战报结算 (EXECUTE SETTLEMENT)",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// =========================================================================
// 4. 模式二：战役 (CAMPAIGN) 详细内容
// =========================================================================
@Composable
private fun CampaignModeContent(
    campaign: Campaign?,
    chapter: LifeChapter?,
    onBoost: (Int) -> Unit,
    onEditCampaign: () -> Unit,
    onNavigateToChapter: () -> Unit,
    onNavigateToDaily: () -> Unit,
    isCartridge: Boolean
) {
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val cardWhite = if (isCartridge) CartridgeCardWhite else RetroCardBg

    val currentProg = campaign?.currentProgress ?: 504
    val targetProg = campaign?.targetTotal ?: 1200
    val progressRatio = if (targetProg > 0) (currentProg.toFloat() / targetProg).coerceIn(0f, 1f) else 0f
    val percentStr = String.format(Locale.getDefault(), "%.1f", progressRatio * 100f)

    // 战役主面板 (点哪改哪)
    RetroContainer(
        title = "阶段战役跟踪",
        subtitle = "ACTIVE CAMPAIGN"
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 战役标题与编辑按钮
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = campaign?.title ?: "2026 公务员考公上岸冲刺战役",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEditCampaign() }
                )

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isCartridge) CartridgeGreen else RetroDarkBg)
                        .border(1.dp, if (isCartridge) CartridgeBorder else RetroCardBorder, RoundedCornerShape(3.dp))
                        .clickable { onEditCampaign() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "⚙️ 配置/模板",
                        color = if (isCartridge) CartridgeCard else RetroGreenPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 归属篇章标签（点击跳篇章）
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onNavigateToChapter() }
                    .padding(vertical = 2.dp)
            ) {
                Text(
                    text = "🏛️ 归属篇章: ${chapter?.title ?: "25~35岁 · 立业与职业护城河黄金十年"}",
                    color = if (isCartridge) CartridgeGreenLight else RetroGreenPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "▶", color = accentRed, fontSize = 10.sp)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "目标达成日期: ${campaign?.targetDate ?: "2026-12-01"} · ${campaign?.stageName ?: "阶段二：真题与模块题海冲刺"}",
                color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 像素垂直刻度进度条
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "冲刺进度",
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$currentProg / $targetProg ${campaign?.unit ?: "题"} ($percentStr%)",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            PixelSegmentedBar(
                progress = progressRatio,
                totalTicks = 40,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 联动机制说明提示盒
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCartridge) Color(0xFFF7F4EC) else Color(0xFF14241B))
                    .border(1.dp, if (isCartridge) Color(0xFFDED8CB) else RetroCardBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(text = "💡", fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "双向联动机制：",
                            color = primaryGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "在【日课】中打卡带有 [🎯 2026考公] 标签的任务，会自动按题数累加到本战役！亦可点击下方快速推进：",
                            color = if (isCartridge) CartridgeText else RetroGreenText.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 快捷推进按钮组
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onBoost(10) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryGreen,
                        contentColor = if (isCartridge) CartridgeCard else RetroDarkBg
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("+10 题 (随手练)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onBoost(50) },
                    modifier = Modifier.weight(1f).height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCartridge) CartridgeCardWhite else RetroDarkBg,
                        contentColor = primaryGreen
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, primaryGreen)
                ) {
                    Text("+50 题 (模考卷)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNavigateToDaily,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isCartridge) Color(0xFFE8E2D5) else Color(0xFF1E281D),
                        contentColor = textColor
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("去打卡 ▶", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 战役里程碑分期路线
    RetroContainer(title = "战役攻坚四阶段里程碑", subtitle = "MILESTONES") {
        MilestoneItem(
            status = "✓ 已达成",
            title = "阶段一：基础考点全覆盖与高频词汇背诵",
            desc = "言语理解、判断推理基本逻辑框架通读，错题集框架搭建完成",
            isDone = true,
            isCurrent = false,
            isCartridge = isCartridge
        )
        MilestoneItem(
            status = "▶ 进行中",
            title = "阶段二：行测五大模块强化与真题刷题",
            desc = "当前进度: $currentProg / $targetProg 题，重点攻克资料分析计算速度与言语正确率",
            isDone = false,
            isCurrent = true,
            isCartridge = isCartridge
        )
        MilestoneItem(
            status = "○ 待解锁",
            title = "阶段三：申论热点积累与全真模考冲刺",
            desc = "公文写作格式固化，大作文金句积累，限时全真模考（目标20套）",
            isDone = false,
            isCurrent = false,
            isCartridge = isCartridge
        )
        MilestoneItem(
            status = "○ 待解锁",
            title = "阶段四：考前两周全真模拟与心态调适",
            desc = "生物钟调整至考试时段，严防失眠与体能透支，轻量刷错题本",
            isDone = false,
            isCurrent = false,
            isCartridge = isCartridge
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // 《HowToLiveBetter》指导建议
    RetroContainer(title = "HowToLiveBetter 防御指南", subtitle = "STRATEGY") {
        Text(
            text = "【体制壁垒在防御优先体系中的定位】\n根据《高性价比人生指南》，体制内考试是个人对抗宏观周期波动、防止断崖式失业的最强护城河之一。\n\n" +
                    "• 小步高频：避免单日过度疲劳导致弃坑，保持每天 30~50 题的神经突触活跃。\n" +
                    "• 循证健康：睡足 7.5 小时，睡眠期是大脑将短期刷题记忆固化为长期技能的关键期。",
            color = if (isCartridge) CartridgeText else RetroGreenText.copy(alpha = 0.85f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

// =========================================================================
// 5. 模式三：篇章 (CHAPTER) 详细内容 (整合张侃5个20年 × 毕生发展心理学 × CFPS/CHARLS实证调查)
// =========================================================================
@Composable
private fun ChapterModeContent(
    chapter: LifeChapter?,
    userStats: UserStats?,
    steps: Int,
    activeCampaign: Campaign?,
    onEditChapter: () -> Unit,
    onNavigateToCampaign: () -> Unit,
    isCartridge: Boolean
) {
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val descColor = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f)

    val currentAge = chapter?.currentAge ?: 25
    val daysPassed = chapter?.daysPassed ?: 319

    // 默认定位到用户当前年龄对应的阶段索引 (0: 18-25, 1: 25-35, 2: 35-45, 3: 45-60, 4: 60-80)
    val defaultStageIndex = when {
        currentAge < 25 -> 0
        currentAge < 35 -> 1
        currentAge < 45 -> 2
        currentAge < 60 -> 3
        else -> 4
    }
    var selectedStageIndex by remember { mutableStateOf(defaultStageIndex) }

    // 自测核验状态字典
    val selfCheckMap = remember {
        mutableStateOf(
            mutableMapOf(
                "t1_1" to true, "t1_2" to true, "t1_3" to false, "t1_4" to true, "t1_5" to false,
                "t2_1" to false, "t2_2" to true, "t2_3" to false, "t2_4" to false, "t2_5" to true
            )
        )
    }

    val selectedChapter = ChineseLifeCourseFramework.allChapters[selectedStageIndex]
    val isPast = selectedStageIndex < defaultStageIndex
    val isCurrent = selectedStageIndex == defaultStageIndex
    val isFuture = selectedStageIndex > defaultStageIndex

    // 生命流速计算
    val velocityResult = remember(userStats, steps) {
        LifeVelocityCalculator.calculate(userStats ?: UserStats(), steps)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 1. 顶部 5 阶段终身胶囊进度轴 (Life Course Capsule Timeline)
        Text(
            text = "/// LIFE TIMELINE 中国人生命历程五大篇章 (张侃“5个20年” × 毕生发展心理学)",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            color = descColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCartridge) Color(0xFFECE6DC) else RetroDarkBg)
                .border(1.2.dp, borderColor, RoundedCornerShape(6.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ChineseLifeCourseFramework.allChapters.forEachIndexed { index, ch ->
                val isSelected = selectedStageIndex == index
                val isThisStageCurrent = defaultStageIndex == index
                val isThisStagePast = index < defaultStageIndex

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(
                            when {
                                isSelected -> if (isCartridge) CartridgeGreen else RetroGreenPrimary
                                isThisStageCurrent -> if (isCartridge) Color(0xFFDFD8CA) else Color(0xFF14241B)
                                else -> Color.Transparent
                            }
                        )
                        .clickable { selectedStageIndex = index }
                        .padding(vertical = 6.dp, horizontal = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = when (index) {
                                0 -> "18-25"
                                1 -> "25-35"
                                2 -> "35-45"
                                3 -> "45-60"
                                else -> "60-80+"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                            fontSize = 10.sp,
                            color = when {
                                isSelected -> if (isCartridge) CartridgeCard else Color.Black
                                isThisStageCurrent -> accentRed
                                else -> descColor
                            }
                        )
                        Text(
                            text = when {
                                isThisStageCurrent -> "进行中"
                                isThisStagePast -> "已通关"
                                else -> "预习"
                            },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 8.sp,
                            color = when {
                                isSelected -> if (isCartridge) CartridgeCard.copy(alpha = 0.85f) else Color.Black
                                isThisStageCurrent -> accentRed
                                else -> descColor.copy(alpha = 0.7f)
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 篇章主面板 (根据 是进行中 / 过去通关 / 未来预习 展现不同三态)
        RetroContainer(
            title = when {
                isCurrent -> "当前经历篇章 (ACTIVE)"
                isPast -> "历史篇章回顾与自测 (REVIEW)"
                else -> "未来篇章提前预习 (PREVIEW)"
            },
            subtitle = selectedChapter.ageRange
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 篇章名称行
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = selectedChapter.name,
                        color = primaryGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (isCurrent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isCartridge) CartridgeGreen else RetroDarkBg)
                                .border(1.dp, borderColor, RoundedCornerShape(3.dp))
                                .clickable { onEditChapter() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "⚙️ 微调",
                                color = if (isCartridge) CartridgeCard else RetroGreenPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "理论基石: ${selectedChapter.theorySource}",
                    color = descColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = selectedChapter.macroStrategy,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // (A) 如果是当前篇章：展示生命流速仪表盘与流速提醒因子
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCartridge) Color(0xFFF7F2E7) else RetroDarkBg)
                            .border(1.2.dp, if (velocityResult.isWarning) CartridgeAccentRed else primaryGreen, RoundedCornerShape(6.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "⏳ 年龄流速计: 当前 $currentAge 岁 · 第 $daysPassed 天",
                                    color = textColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = velocityResult.statusTag,
                                    color = if (velocityResult.isWarning) CartridgeAccentRed else primaryGreen,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 10.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // 十年进度条
                            val totalDays = (selectedChapter.endAge - selectedChapter.startAge) * 365
                            val prog = (daysPassed.toFloat() / totalDays).coerceIn(0f, 1f)
                            PixelSegmentedBar(
                                progress = prog,
                                activeColor = if (velocityResult.isWarning) CartridgeAccentRed else primaryGreen
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "生命流速因子: ${velocityResult.factorText} | ${velocityResult.diagnosticSummary}",
                                color = if (velocityResult.isWarning) CartridgeAccentRed else descColor,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // (B) 核心发展任务 (Developmental Tasks)
                Text(
                    text = when {
                        isCurrent -> "📋 本阶段 5 大核心发展任务 (进行中):"
                        isPast -> "📋 历史任务自测核验 (点击可切换“达成 vs 遗留待补”):"
                        else -> "📋 未来阶段必须面对的核心矛盾与发展任务:"
                    },
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = primaryGreen
                )

                Spacer(modifier = Modifier.height(6.dp))

                selectedChapter.developmentalTasks.forEach { task ->
                    val isChecked = selfCheckMap.value[task.id] ?: task.defaultAchieved
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCartridge) Color(0xFFFAF7F0) else Color(0xFF0F1A13))
                            .border(1.dp, if (isChecked) primaryGreen.copy(alpha = 0.6f) else borderColor.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                            .clickable {
                                // 点击切换自测状态
                                val updated = selfCheckMap.value.toMutableMap()
                                updated[task.id] = !isChecked
                                selfCheckMap.value = updated
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isChecked) "✓" else "○",
                                color = if (isChecked) primaryGreen else CartridgeAccentRed,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${task.category}] ${task.title}",
                                    color = if (isChecked) textColor else descColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = task.description,
                                    color = descColor,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 9.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isChecked) "已达成" else if (isPast) "待补课" else "未达成",
                                color = if (isChecked) primaryGreen else CartridgeAccentRed,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // (C) 权威实证数据引用卡片 (CFPS / CHARLS 硬核透视)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCartridge) Color(0xFFF3EFE7) else RetroDarkBg)
                        .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📊", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = selectedChapter.empiricalInsight.source,
                                color = primaryGreen,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "“${selectedChapter.empiricalInsight.dataQuote}”",
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "💡 启示: ${selectedChapter.empiricalInsight.keyTakeaway}",
                            color = accentRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                // (D) 建议主线战役锚定
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCartridge) Color(0xFFFAF7F0) else RetroDarkBg)
                        .border(1.dp, borderColor, RoundedCornerShape(4.dp))
                        .clickable { onNavigateToCampaign() }
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "🎯 本阶段核心战役: ${selectedChapter.recommendedCampaignTitle}",
                            color = primaryGreen,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "目标配额: ${selectedChapter.recommendedCampaignTarget}",
                            color = descColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 9.sp
                        )
                    }
                    Text(
                        text = "查看战役 ▶",
                        color = accentRed,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// =========================================================================
// 6. 辅助小组件 (Helper Item Components)
// =========================================================================
@Composable
private fun TaskRowItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    isCartridge: Boolean
) {
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val cardBg = if (isCartridge) CartridgeCardWhite else RetroDarkBg
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber
    val textColor = if (isCartridge) CartridgeText else RetroGreenText

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(cardBg)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .padding(bottom = 2.dp)
            .clickable { onToggle() }
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (task.isCompleted) primaryGreen else if (isCartridge) CartridgeTextMuted else RetroMuted,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 若关联了阶段战役标签
                if (task.campaignTag.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentRed)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = task.campaignTag,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                Text(
                    text = task.title,
                    color = if (task.isCompleted) if (isCartridge) CartridgeTextMuted else RetroMuted else textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
            }

            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 经验徽章 (复古砖红/琥珀)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(if (isCartridge) Color(0xFFF0EBE1) else RetroCardBorder)
                .border(1.dp, if (isCartridge) CartridgeBorder.copy(alpha = 0.4f) else RetroCardBorder, RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${task.targetAttribute} +${task.expReward}",
                color = accentRed,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // EXP 标签
        Text(
            text = if (task.isCompleted) "EXP +${task.expReward}" else "EXP -${task.expReward / 2}",
            color = if (task.isCompleted) {
                if (isCartridge) CartridgeGreen else RetroGreenPrimary
            } else {
                CartridgeAccentRed
            },
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = if (isCartridge) CartridgeTextMuted else RetroMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun MilestoneItem(
    status: String,
    title: String,
    desc: String,
    isDone: Boolean,
    isCurrent: Boolean,
    isCartridge: Boolean
) {
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber
    val cardWhite = if (isCartridge) CartridgeCardWhite else RetroDarkBg

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(cardWhite)
            .border(
                1.dp,
                if (isCurrent) primaryGreen else if (isCartridge) CartridgeBorder.copy(alpha = 0.3f) else RetroCardBorder,
                RoundedCornerShape(4.dp)
            )
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDone) primaryGreen else if (isCurrent) accentRed else Color.Gray)
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = status,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isCartridge) CartridgeText else RetroGreenText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = desc,
            color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun DefensePillarItem(
    attrTag: String,
    title: String,
    desc: String,
    status: String,
    statusColor: Color,
    onClick: (() -> Unit)?,
    isCartridge: Boolean
) {
    val cardWhite = if (isCartridge) CartridgeCardWhite else RetroDarkBg

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(cardWhite)
            .border(1.dp, if (isCartridge) CartridgeBorder.copy(alpha = 0.4f) else RetroCardBorder, RoundedCornerShape(4.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isCartridge) CartridgeGreen else RetroGreenPrimary)
                    .padding(horizontal = 5.dp, vertical = 1.dp)
            ) {
                Text(
                    text = attrTag,
                    color = if (isCartridge) CartridgeCard else RetroDarkBg,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = if (isCartridge) CartridgeText else RetroGreenText,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = status,
                color = statusColor,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = desc,
            color = if (isCartridge) CartridgeTextMuted else RetroCardBorderLight,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            lineHeight = 14.sp
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
private fun RoadmapStageItem(
    stage: String,
    title: String,
    tag: String,
    isPassed: Boolean,
    isCurrent: Boolean,
    isCartridge: Boolean
) {
    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = stage,
            color = if (isCurrent) accentRed else if (isPassed) primaryGreen else Color.Gray,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.width(85.dp)
        )
        Text(
            text = title,
            color = if (isCartridge) CartridgeText else RetroGreenText,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = tag,
            color = if (isCurrent) accentRed else if (isPassed) primaryGreen else Color.Gray,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
    }
}
