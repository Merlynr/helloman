package com.lifeonline.app.ui.screens.journal

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.data.model.DecomposedTask
import com.lifeonline.app.data.model.SettlementResult
import com.lifeonline.app.data.model.TaskItem
import com.lifeonline.app.data.model.TaskType
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.components.SettlementDialog
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroCardBorderLight
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import com.lifeonline.app.ui.theme.RetroMuted
import kotlinx.coroutines.launch

@Composable
fun JournalScreen(repository: LifeOnlineRepository) {
    val context = LocalContext.current
    val tasks by repository.tasksFlow.collectAsState(initial = emptyList())
    val steps by repository.stepsFlow.collectAsState()
    val scope = rememberCoroutineScope()

    var journalInput by remember {
        mutableStateOf("上午心流写代码2小时，下午在公园快走4公里，午餐轻食花费28元，晚上阅读专业书半小时。")
    }
    var newTaskTitle by remember { mutableStateOf("") }
    var isSettling by remember { mutableStateOf(false) }
    var activeSettlement by remember { mutableStateOf<SettlementResult?>(null) }

    // 语音输入与 AI 任务拆解状态
    var voiceInputText by remember { mutableStateOf("") }
    var isDecomposing by remember { mutableStateOf(false) }
    var candidateTasks by remember { mutableStateOf<List<DecomposedTask>>(emptyList()) }

    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val spokenText = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                voiceInputText = spokenText
            }
        }
    }

    fun startVoiceRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说出您今天打算做的事或任务清单...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "未找到系统语音识别服务，可直接在此处打字输入", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroDarkBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 1. 安卓端自动采集卡片 (Auto Sensor Card)
        RetroContainer(title = "安卓端自动采集", subtitle = "SENSOR ACTIVE") {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "今日计步传感器",
                        color = RetroCardBorderLight,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "$steps 步",
                        color = RetroGreenPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }

                // 便捷模拟测试按钮
                Button(
                    onClick = { repository.sensorManager.addManualSteps(1000) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroDarkBg,
                        contentColor = RetroGreenPrimary
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("+1000步测试", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 2. 每日任务与习惯打卡 (Quests & Habits)
        RetroContainer(title = "任务清单与习惯打卡", subtitle = "${tasks.count { it.isCompleted }}/${tasks.size} 已完成") {

            // 语音 / 一句话 AI 智能拆解区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF0F1A13))
                    .border(1.dp, RetroCardBorder, RoundedCornerShape(6.dp))
                    .padding(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = RetroAmber,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "/// 语音 / 一句话 AI 智能拆解任务",
                        color = RetroAmber,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = voiceInputText,
                        onValueChange = { voiceInputText = it },
                        placeholder = {
                            Text(
                                "点击右侧麦克风说话，或在此打字输入 (如：上午写代码2小时，下午跑步5公里，晚上看书记账)",
                                color = RetroCardBorderLight,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(62.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = RetroGreenText,
                            unfocusedTextColor = RetroGreenText,
                            focusedBorderColor = RetroGreenPrimary,
                            unfocusedBorderColor = RetroCardBorder,
                            focusedContainerColor = RetroDarkBg,
                            unfocusedContainerColor = RetroDarkBg
                        ),
                        shape = RoundedCornerShape(4.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { startVoiceRecognition() },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(RetroGreenPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "语音输入",
                            tint = RetroDarkBg,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        if (voiceInputText.isNotBlank()) {
                            scope.launch {
                                isDecomposing = true
                                try {
                                    val parsed = repository.decomposeInputToTasks(voiceInputText)
                                    candidateTasks = parsed
                                    if (parsed.isEmpty()) {
                                        Toast.makeText(context, "未识别出明确任务，请再补充一点描述", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "拆解异常: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isDecomposing = false
                                }
                            }
                        }
                    },
                    enabled = !isDecomposing && voiceInputText.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(38.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroDarkBg,
                        contentColor = RetroGreenPrimary
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RetroGreenPrimary)
                ) {
                    if (isDecomposing) {
                        CircularProgressIndicator(
                            color = RetroGreenPrimary,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 语义分析与五维属性标注中...", fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    } else {
                        Text("🪄 AI 智能拆解为任务清单 (PARSE)", fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // 候选任务预览卡片
            if (candidateTasks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF0C2417))
                        .border(1.dp, RetroGreenPrimary, RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "✓ AI 识别出 ${candidateTasks.size} 个原子任务（勾选确认后加入今日清单）：",
                        color = RetroGreenPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    candidateTasks.forEachIndexed { index, task ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(RetroDarkBg)
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .clickable {
                                    candidateTasks = candidateTasks.toMutableList().also {
                                        it[index] = it[index].copy(isSelected = !it[index].isSelected)
                                    }
                                }
                        ) {
                            Checkbox(
                                checked = task.isSelected,
                                onCheckedChange = { checked ->
                                    candidateTasks = candidateTasks.toMutableList().also {
                                        it[index] = it[index].copy(isSelected = checked)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = RetroGreenPrimary,
                                    uncheckedColor = RetroCardBorder,
                                    checkmarkColor = RetroDarkBg
                                ),
                                modifier = Modifier.size(24.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = task.title,
                                    color = RetroGreenText,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val attrColor = when (task.targetAttribute) {
                                        "TEC" -> Color(0xFF4FC3F7)
                                        "VIT" -> RetroGreenPrimary
                                        "SAN" -> Color(0xFFBA68C8)
                                        "GOLD" -> RetroAmber
                                        "REL" -> Color(0xFFFF8A65)
                                        else -> RetroGreenPrimary
                                    }
                                    Text(
                                        text = "[${task.targetAttribute}]",
                                        color = attrColor,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (task.taskType == TaskType.DAILY) "日常" else "支线",
                                        color = RetroCardBorderLight,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 10.sp
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "+${task.expReward} EXP",
                                        color = RetroAmber,
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
                            onClick = { candidateTasks = emptyList() },
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RetroCardBorder)
                        ) {
                            Text("取消", color = RetroMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        val selectedCount = candidateTasks.count { it.isSelected }
                        Button(
                            onClick = {
                                scope.launch {
                                    repository.addBatchTasks(candidateTasks)
                                    candidateTasks = emptyList()
                                    voiceInputText = ""
                                    Toast.makeText(context, "成功添加 $selectedCount 项任务至今日清单！", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = selectedCount > 0,
                            modifier = Modifier.height(34.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RetroGreenPrimary,
                                contentColor = RetroDarkBg
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

            // 任务列表
            tasks.forEach { task ->
                TaskRowItem(
                    task = task,
                    onToggle = { scope.launch { repository.toggleTask(task) } },
                    onDelete = { scope.launch { repository.deleteTask(task) } }
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
                    onValueChange = { newTaskTitle = it },
                    placeholder = {
                        Text(
                            "手动单条添加 (如: 晚间拉伸10分钟)",
                            color = RetroCardBorderLight,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = RetroGreenText,
                        unfocusedTextColor = RetroGreenText,
                        focusedBorderColor = RetroGreenPrimary,
                        unfocusedBorderColor = RetroCardBorder,
                        focusedContainerColor = RetroDarkBg,
                        unfocusedContainerColor = RetroDarkBg
                    ),
                    shape = RoundedCornerShape(4.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (newTaskTitle.isNotBlank()) {
                            scope.launch {
                                repository.addTask(newTaskTitle.trim(), TaskType.DAILY, "VIT", 15)
                                newTaskTitle = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(RetroGreenPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "添加", tint = RetroDarkBg)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. 极简流水账随手记输入框
        RetroContainer(title = "极简流水账随手记", subtitle = "DAILY LOG") {
            Text(
                text = "写下一两句今日真实行动，AI 将根据《高性价比人生指南》标尺为你折算经验、属性变化与战报。",
                color = RetroGreenText.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = journalInput,
                onValueChange = { journalInput = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = RetroGreenText,
                    unfocusedTextColor = RetroGreenText,
                    focusedBorderColor = RetroGreenPrimary,
                    unfocusedBorderColor = RetroCardBorder,
                    focusedContainerColor = RetroDarkBg,
                    unfocusedContainerColor = RetroDarkBg
                ),
                shape = RoundedCornerShape(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            // 触发战报结算大按钮
            Button(
                onClick = {
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
                enabled = !isSettling && journalInput.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroGreenPrimary,
                    contentColor = RetroDarkBg
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                if (isSettling) {
                    CircularProgressIndicator(
                        color = RetroDarkBg,
                        modifier = Modifier.width(18.dp).height(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AI 裁决经验中...", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                } else {
                    Text(
                        text = "⚡ 今日战报结算 (EXECUTE SETTLEMENT)",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

    // 结算弹窗
    activeSettlement?.let { settlement ->
        SettlementDialog(
            result = settlement,
            onDismiss = { activeSettlement = null }
        )
    }
}

@Composable
private fun TaskRowItem(
    task: TaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(RetroDarkBg)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .padding(bottom = 4.dp)
            .clickable { onToggle() }
    ) {
        Icon(
            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.Circle,
            contentDescription = null,
            tint = if (task.isCompleted) RetroGreenPrimary else RetroMuted,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = task.title,
                color = if (task.isCompleted) RetroMuted else RetroGreenText,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
            )
            if (task.description.isNotBlank()) {
                Text(
                    text = task.description,
                    color = RetroCardBorderLight,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // 属性标签与经验徽章
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(RetroCardBorder)
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${task.targetAttribute} +${task.expReward}",
                color = RetroAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = RetroMuted,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
