package com.lifeonline.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import com.lifeonline.app.ui.theme.RetroRed
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    repository: LifeOnlineRepository,
    onResetCharacter: () -> Unit
) {
    val aiClient = repository.aiClient
    val daemonClient = repository.daemonClient
    val scope = rememberCoroutineScope()

    var daemonUrl by remember { mutableStateOf(daemonClient.serverUrl) }
    var daemonStatus by remember { mutableStateOf("") }
    var apiKey by remember { mutableStateOf(aiClient.apiKey) }
    var baseUrl by remember { mutableStateOf(aiClient.baseUrl) }
    var modelName by remember { mutableStateOf(aiClient.modelName) }
    var saveStatus by remember { mutableStateOf("") }
    var isTestingApi by remember { mutableStateOf(false) }
    var apiTestResult by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroDarkBg)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 0. C/S 模式：电脑端 Agent 守护进程配置
        RetroContainer(title = "C/S 模式 · 电脑端 Agent 守护进程", subtitle = "DAEMON") {
            Text(
                text = "连接当前电脑常驻的 Python Agent 守护进程。支持 USB 调试直连 (127.0.0.1:8080) 或局域网 IP 直连。由电脑端 Agent 负责推演与持久化长程知识库。",
                color = RetroGreenText.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = daemonUrl,
                onValueChange = { daemonUrl = it },
                label = { Text("Agent 服务端地址", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    daemonClient.serverUrl = daemonUrl.trim()
                    scope.launch {
                        val isOnline = daemonClient.isServerOnline()
                        daemonStatus = if (isOnline) "● 连接成功！电脑端 Agent 守护服务在线" else "○ 无法连接，请确认电脑已启动 server/main.py 并在 8080 端口运行"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroGreenPrimary,
                    contentColor = RetroDarkBg
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("[ 测试 Agent 服务连通性 (TEST) ]", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            if (daemonStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = daemonStatus,
                    color = if (daemonStatus.startsWith("●")) RetroGreenPrimary else RetroAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. 大模型 API 配置 (LLM API Configuration)
        RetroContainer(title = "AI 模型服务配置 (国内直连·全天候)", subtitle = "CONFIG") {
            Text(
                text = "支持国内主流大模型。配置后手机将直接连接云端模型，无需开电脑，24小时随时推演结算！",
                color = RetroGreenText.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 一键预设切换按钮
            Text(
                text = "/// 一键快捷选择大模型提供商",
                color = RetroAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip(
                    title = "智谱 GLM-4-Flash\n(官方永久免费)",
                    color = RetroGreenPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    baseUrl = "https://open.bigmodel.cn/api/paas/v4"
                    modelName = "glm-4-flash"
                }
                PresetChip(
                    title = "DeepSeek\n(高性价比开源名模)",
                    color = RetroAmber,
                    modifier = Modifier.weight(1f)
                ) {
                    baseUrl = "https://api.deepseek.com/v1"
                    modelName = "deepseek-chat"
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                PresetChip(
                    title = "硅基流动\n(Qwen/DeepSeek)",
                    color = RetroGreenPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    baseUrl = "https://api.siliconflow.cn/v1"
                    modelName = "Qwen/Qwen2.5-7B-Instruct"
                }
                PresetChip(
                    title = "通义千问\n(阿里云百炼大模型)",
                    color = RetroAmber,
                    modifier = Modifier.weight(1f)
                ) {
                    baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1"
                    modelName = "qwen-plus"
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // API Key
            OutlinedTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                label = { Text("API Key", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                placeholder = { Text("sk-...", color = RetroCardBorder, fontFamily = FontFamily.Monospace) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Base URL
            OutlinedTextField(
                value = baseUrl,
                onValueChange = { baseUrl = it },
                label = { Text("Base URL", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Model Name
            OutlinedTextField(
                value = modelName,
                onValueChange = { modelName = it },
                label = { Text("模型名称 (Model)", fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors(),
                shape = RoundedCornerShape(4.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        aiClient.apiKey = apiKey.trim()
                        aiClient.baseUrl = baseUrl.trim()
                        aiClient.modelName = modelName.trim()
                        saveStatus = "配置已持久化保存！"
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroGreenPrimary,
                        contentColor = RetroDarkBg
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("[ 保存 (SAVE) ]", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        scope.launch {
                            isTestingApi = true
                            apiTestResult = "正在向大模型发送握手测试..."
                            try {
                                val (ok, msg) = aiClient.testApiConnection(apiKey.trim(), baseUrl.trim(), modelName.trim())
                                apiTestResult = if (ok) "● $msg" else "○ $msg"
                            } finally {
                                isTestingApi = false
                            }
                        }
                    },
                    enabled = !isTestingApi,
                    modifier = Modifier.weight(1.3f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroDarkBg,
                        contentColor = RetroAmber
                    ),
                    shape = RoundedCornerShape(4.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RetroAmber)
                ) {
                    Text(
                        if (isTestingApi) "测试中..." else "[ 测试 API 连通性 ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            if (saveStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = saveStatus,
                    color = RetroGreenPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            if (apiTestResult.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = apiTestResult,
                    color = if (apiTestResult.startsWith("●")) RetroGreenPrimary else RetroRed,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. 《高性价比人生指南》标尺介绍
        RetroContainer(title = "成长评价标尺体系", subtitle = "HowToLiveBetter") {
            Text(
                text = "本项目经验体系严格参考 GitHub 开源项目 eternity4719/HowToLiveBetter 构建：",
                color = RetroAmber,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            BulletItem("循证科学", "拒绝玄学与虚假自律，只有具备证据等级的健康、知识与财务行为才能带来确定性复利。")
            BulletItem("防御优先", "防重疾、防断崖式失业、防意外致贫，底线防守的权重高于短期的盲目进攻。")
            BulletItem("多周期标尺", "近期微习惯呵护心力 -> 阶段性习惯固化 -> 长期人生护城河构筑。")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 3. 角色转生/重新初始化
        RetroContainer(title = "角色管理", subtitle = "RESET") {
            Text(
                text = "若经历有了重大飞跃，或想重新输入履历生成全新五维属性与天生特质，可点击下方重新转生。",
                color = RetroGreenText.copy(alpha = 0.7f),
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onResetCharacter,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RetroDarkBg,
                    contentColor = RetroRed
                ),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("[ 重新录入经历·重新转生 ]", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BulletItem(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 3.dp)) {
        Text(
            text = "• $title",
            color = RetroGreenPrimary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = RetroGreenText.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            lineHeight = 15.sp,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}

@Composable
private fun PresetChip(
    title: String,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(RetroDarkBg)
            .border(1.dp, color.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 4.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(
            text = title,
            color = color,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            lineHeight = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun textFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = RetroGreenText,
    unfocusedTextColor = RetroGreenText,
    focusedBorderColor = RetroGreenPrimary,
    unfocusedBorderColor = RetroCardBorder,
    focusedContainerColor = RetroDarkBg,
    unfocusedContainerColor = RetroDarkBg,
    focusedLabelColor = RetroGreenPrimary,
    unfocusedLabelColor = RetroCardBorder
)
