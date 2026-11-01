package com.lifeonline.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.AvatarDisplay
import com.lifeonline.app.ui.components.AvatarPicker
import com.lifeonline.app.ui.components.PixelSegmentedBar
import com.lifeonline.app.ui.components.RadarChart
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.theme.CartridgeAccentRed
import com.lifeonline.app.ui.theme.CartridgeBg
import com.lifeonline.app.ui.theme.CartridgeBorder
import com.lifeonline.app.ui.theme.CartridgeCard
import com.lifeonline.app.ui.theme.CartridgeCardWhite
import com.lifeonline.app.ui.theme.CartridgeGreen
import com.lifeonline.app.ui.theme.CartridgeText
import com.lifeonline.app.ui.theme.CartridgeTextMuted
import com.lifeonline.app.ui.theme.LocalIsCartridgeTheme
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBg
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import kotlinx.coroutines.launch

enum class OnboardingTab {
    QUICK_AI,       // ⚡ AI 一键极速生成
    WIZARD_6_STEP   // 🧭 6步循证深度测评
}

data class ArchetypePreset(
    val name: String,
    val avatar: String,
    val age: String,
    val prompt: String,
    val label: String
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    repository: LifeOnlineRepository,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isCartridge = LocalIsCartridgeTheme.current
    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current

    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val bgColor = if (isCartridge) CartridgeBg else RetroDarkBg
    val cardBg = if (isCartridge) CartridgeCardWhite else RetroCardBg
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber

    // 创角模式切换：默认提供便捷的 AI 一键生成，也可随时切为 6步循证测评
    var selectedTab by remember { mutableStateOf(OnboardingTab.QUICK_AI) }

    // 基础身份
    var playerName by remember { mutableStateOf("玩家") }
    var selectedAvatar by remember { mutableStateOf("🐻") }
    var playerAge by remember { mutableStateOf("25") }

    // AI 一键生成模式：一句话速写
    var quickPrompt by remember {
        mutableStateOf("25岁软件工程师，热爱跑步健身，有6个月应急备用金，追求专注心流与稳健成长。")
    }

    // 6步向导步骤状态 (1 ~ 6)
    var currentStep by remember { mutableIntStateOf(1) }
    var tecInput by remember { mutableStateOf("计算机软件工程，独立开发过工具软件，擅长系统架构设计与工程攻坚。") }
    var vitInput by remember { mutableStateOf("日常步数6000左右，坚持每周跑步2次，但偶尔写代码熬夜至凌晨，希望养成22:30前规律作息。") }
    var sanInput by remember { mutableStateOf("能保持深度专注心流2小时，抗挫力尚可，但面对突发压力偶有内耗，希望筑牢心理安全垫。") }
    var goldInput by remember { mutableStateOf("有6个月应急备用金储备，有记账习惯与定投意识，追求高性价比消费与稳健防御。") }
    var bndInput by remember { mutableStateOf("有默契的核心好友与家人支持系统，偏向深度社交，重视与同频良师益友的长期共赢。") }

    var isLoading by remember { mutableStateOf(false) }
    var genesisResult by remember { mutableStateOf<CharacterGenesisResult?>(null) }

    // 预设天命模版库
    val archetypes = remember {
        listOf(
            ArchetypePreset("代码漫步者", "🦊", "25", "计算机软件工程，独立开发工具，擅长系统架构设计与解决疑难bug，追求专注心流与高性价比技术复利。", "💻 全栈极客"),
            ArchetypePreset("体制逐光者", "🦉", "24", "备考公务员/事业单位，注重结构化思维与申论备战，抗压能力强，追求稳定扎根与行稳致远。", "🏛️ 上岸公考"),
            ArchetypePreset("风行健将", "🐆", "26", "日常坚持慢跑5公里与力量训练，规律22:30前就寝与早起，体能充沛，追求长寿与抗衰老身体底盘。", "🏃 晨跑体魄"),
            ArchetypePreset("金流守望者", "🦁", "28", "有6个月应急储蓄备用金，严格记账与指数定投，防御优先，拒绝盲目消费，构建财务安全垫。", "📈 防御理财"),
            ArchetypePreset("云端漫步客", "🦅", "27", "自由职业与数字游民，从事创意设计与内容创作，深度社交与家人支持，追求心流自由与作品复利。", "🎨 自由游民"),
            ArchetypePreset("清风笃行者", "🐼", "29", "极简主义生活方式，物欲低、内心宁静，重视睡眠与心理安全垫，享受当下微小的确定性幸福。", "🧘 极简极静"),
            ArchetypePreset("知行研习者", "🐱", "23", "沉浸学术科研与研究生深造，深度思考，阅读大量文献，长期主义复利成长。", "📚 考研求索"),
            ArchetypePreset("破壁先锋", "🐻", "25", "兼顾脑力攻坚与体能防御，有稳固的家庭好友支持，全面发展五维属性，追求高性价比人生。", "🛡️ 综合均衡")
        )
    }

    val randomNames = remember {
        listOf("星尘行者", "破壁极客", "从容散修", "晨曦追光者", "青岳行者", "深海潜行者", "赤子匠心", "凌云旅人", "磐石行者")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部大标题 (自适应布局，防止右侧徽章被挤压折行)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "/// 人生 ONLINE · 角色创生 ///",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = "《高性价比人生指南》循证测评 · AI 问询向导",
                    color = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (genesisResult == null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryGreen.copy(alpha = 0.12f))
                        .border(1.2.dp, primaryGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (selectedTab == OnboardingTab.QUICK_AI) "⚡ AI 极速" else "步骤 $currentStep / 6",
                        color = primaryGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (genesisResult == null) {
            // 创角模式切换 Tab (AI 一键极速 vs 6步循证测评)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isCartridge) Color(0xFFEDE9DF) else Color(0xFF14241B))
                    .border(1.2.dp, borderColor, RoundedCornerShape(6.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Tab 1: AI 一键生成
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selectedTab == OnboardingTab.QUICK_AI) primaryGreen else Color.Transparent)
                        .clickable { selectedTab = OnboardingTab.QUICK_AI }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI 一键生成",
                            modifier = Modifier.size(15.dp),
                            tint = if (selectedTab == OnboardingTab.QUICK_AI) Color.White else textColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "⚡ AI 一键极速生成",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == OnboardingTab.QUICK_AI) Color.White else textColor
                        )
                    }
                }

                // Tab 2: 6步循证测评
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (selectedTab == OnboardingTab.WIZARD_6_STEP) primaryGreen else Color.Transparent)
                        .clickable { selectedTab = OnboardingTab.WIZARD_6_STEP }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FormatListNumbered,
                            contentDescription = "6步深度测评",
                            modifier = Modifier.size(15.dp),
                            tint = if (selectedTab == OnboardingTab.WIZARD_6_STEP) Color.White else textColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🧭 6步深度测评",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = if (selectedTab == OnboardingTab.WIZARD_6_STEP) Color.White else textColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedTab == OnboardingTab.QUICK_AI) {
                // =========================================================
                // ⚡ AI 一键极速创角界面 (Quick AI Genesis View)
                // =========================================================
                RetroContainer(title = "⚡ AI 一键极速创角", subtitle = "QUICK GENESIS") {
                    AiInterviewerPrompt(
                        prompt = "「不想经历繁复问答？在此一键写下一句话简介，或直接点击【随机天命】，AI 将根据《高性价比人生指南》瞬间为你量化五维属性、专属称号与天生特质！」",
                        primaryColor = primaryGreen,
                        isCartridge = isCartridge
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. 角色名片与图腾即时预览
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isCartridge) Color(0xFFF5F1E8) else Color(0xFF14241B), RoundedCornerShape(8.dp))
                            .border(1.2.dp, borderColor, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(if (isCartridge) Color(0xFFEDE9DF) else RetroDarkBg, RoundedCornerShape(8.dp))
                                .border(1.5.dp, primaryGreen, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AvatarDisplay(avatar = selectedAvatar, size = 54.dp, fontSize = 28.sp)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ID: ${playerName.ifBlank { "玩家" }}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor
                            )
                            Text(
                                text = "历法年龄：$playerAge 岁 · 图腾已就绪",
                                fontSize = 11.sp,
                                color = if (isCartridge) CartridgeTextMuted else Color.Gray,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // 随机代号骰子
                        OutlinedButton(
                            onClick = { playerName = randomNames.random() },
                            shape = RoundedCornerShape(4.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, primaryGreen)
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = "随机代号", modifier = Modifier.size(14.dp), tint = primaryGreen)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("换代号", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = primaryGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 2. 昵称与历法年龄输入
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(modifier = Modifier.weight(1.5f)) {
                            Text(
                                text = "角色昵称 / 代号",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = playerName,
                                onValueChange = { playerName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = primaryGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                )
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "历法年龄",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = playerAge,
                                onValueChange = { playerAge = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = primaryGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3. 选择专属卡头图腾
                    AvatarPicker(
                        selectedAvatar = selectedAvatar,
                        onAvatarSelected = { selectedAvatar = it },
                        isCartridge = isCartridge
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 4. 随机天命角色卡抽取 (一键填入整套模版)
                    Button(
                        onClick = {
                            val picked = archetypes.random()
                            playerName = picked.name
                            selectedAvatar = picked.avatar
                            playerAge = picked.age
                            quickPrompt = picked.prompt
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCartridge) Color(0xFFF7F2E7) else Color(0xFF1E2E22),
                            contentColor = primaryGreen
                        ),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, primaryGreen)
                    ) {
                        Icon(Icons.Default.Casino, contentDescription = "随机天命", modifier = Modifier.size(16.dp), tint = primaryGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "🎲 随机抽取天命角色 (一键配齐图腾、代号与履历)",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = primaryGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 5. 灵感磁贴 (快捷填充常用路线)
                    Text(
                        text = "/// 点击快速注入天命路线模版：",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryGreen,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    QuickChipRow(
                        chips = archetypes.map { it.label },
                        onChipClick = { label ->
                            val match = archetypes.find { it.label == label }
                            if (match != null) {
                                selectedAvatar = match.avatar
                                playerName = match.name
                                quickPrompt = match.prompt
                            }
                        },
                        primaryColor = primaryGreen,
                        isCartridge = isCartridge
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 6. 一句话速写输入框
                    Text(
                        text = "一句话现实经历或理想路线（支持随意补充或修改）：",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    WizardInputField(
                        value = quickPrompt,
                        onValueChange = { quickPrompt = it },
                        textColor = textColor,
                        primaryColor = primaryGreen,
                        borderColor = borderColor
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // 7. 一键生成大按钮
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            scope.launch {
                                isLoading = true
                                val consolidatedInput = buildString {
                                    appendLine("玩家昵称: $playerName, 历法年龄: $playerAge 岁, 卡头图腾: $selectedAvatar")
                                    appendLine("【AI 一键极速创角】: $quickPrompt")
                                }
                                try {
                                    genesisResult = repository.initializeCharacter(
                                        historyInput = consolidatedInput,
                                        playerName = playerName,
                                        avatar = selectedAvatar
                                    )
                                } finally {
                                    isLoading = false
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI 正在一键严密量化五维与天生特质...", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "一键生成", tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "★ 启动 AI 极速创角 · 一键推演生成完整角色 (GENESIS)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                // =========================================================
                // 🧭 6步循证深度测评向导 (6-Step Detailed Assessment)
                // =========================================================
                // 向导进度条
                PixelSegmentedBar(
                    progress = currentStep / 6f,
                    activeColor = primaryGreen
                )

                Spacer(modifier = Modifier.height(14.dp))

                when (currentStep) {
                    // 步骤 1：基础设定 (姓名、卡头头像、年龄)
                    1 -> {
                        RetroContainer(title = "阶段 1/6 · 现实坐标与图腾", subtitle = "IDENTITY") {
                            AiInterviewerPrompt(
                                prompt = "「旅者，欢迎来到现实世界的 RPG 映射系统。在开始测定你的生理与精神五维之前，请首先确定你在现实世界的身份坐标与专属图腾。」",
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 当前形象即时预览
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(if (isCartridge) Color(0xFFF5F1E8) else Color(0xFF14241B), RoundedCornerShape(8.dp))
                                    .border(1.2.dp, borderColor, RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(if (isCartridge) Color(0xFFEDE9DF) else RetroDarkBg, RoundedCornerShape(8.dp))
                                        .border(1.5.dp, primaryGreen, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    AvatarDisplay(avatar = selectedAvatar, size = 54.dp, fontSize = 28.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = "ID: ${playerName.ifBlank { "玩家" }}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Black,
                                        color = textColor
                                    )
                                    Text(
                                        text = "当前历法年龄：$playerAge 岁",
                                        fontSize = 11.sp,
                                        color = if (isCartridge) CartridgeTextMuted else Color.Gray,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // 姓名输入框
                            Text(
                                text = "角色昵称 / 代号",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = playerName,
                                onValueChange = { playerName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = primaryGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // 年龄输入框
                            Text(
                                text = "历法年龄（用于定位人生篇章）",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = playerAge,
                                onValueChange = { playerAge = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = textColor,
                                    unfocusedTextColor = textColor,
                                    focusedBorderColor = primaryGreen,
                                    unfocusedBorderColor = borderColor,
                                    focusedContainerColor = cardBg,
                                    unfocusedContainerColor = cardBg
                                )
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // 头像图腾选择器
                            AvatarPicker(
                                selectedAvatar = selectedAvatar,
                                onAvatarSelected = { selectedAvatar = it },
                                isCartridge = isCartridge
                            )
                        }
                    }

                    // 步骤 2：学识与硬核专业技能 (TEC)
                    2 -> {
                        RetroContainer(title = "阶段 2/6 · 学识与硬核专业技能", subtitle = "TEC") {
                            AiInterviewerPrompt(
                                prompt = "「在知识经济与分工社会中，你的硬核立身技能是什么？在哪些专业领域具备长周期复利的解决问题能力？」",
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            QuickChipRow(
                                chips = listOf("计算机软件架构", "数据分析与量化", "公文写作与申论", "财务精算与风控", "法律法规与合规", "深度学术科研", "外语与跨文化沟通"),
                                onChipClick = { tecInput = "$tecInput $it。" },
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            WizardInputField(
                                value = tecInput,
                                onValueChange = { tecInput = it },
                                textColor = textColor,
                                primaryColor = primaryGreen,
                                borderColor = borderColor
                            )
                        }
                    }

                    // 步骤 3：体魄与天时作息 (VIT)
                    3 -> {
                        RetroContainer(title = "阶段 3/6 · 体魄与天时作息", subtitle = "VIT") {
                            AiInterviewerPrompt(
                                prompt = "「身体是承载命运之重的神殿。《高性价比人生指南》严密监控22:30天时作息与日间久坐。你目前的睡眠、步数与运动习惯如何？」",
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            QuickChipRow(
                                chips = listOf("习惯22:30前规律入睡", "坚持每周慢跑2~3次", "日均步数超8000步", "重视防久坐定时拉伸", "经常熬夜需警惕", "体能充沛精力旺盛"),
                                onChipClick = { vitInput = "$vitInput $it。" },
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            WizardInputField(
                                value = vitInput,
                                onValueChange = { vitInput = it },
                                textColor = textColor,
                                primaryColor = primaryGreen,
                                borderColor = borderColor
                            )
                        }
                    }

                    // 步骤 4：心理意志与心流定力 (SAN)
                    4 -> {
                        RetroContainer(title = "阶段 4/6 · 心理意志与心流", subtitle = "SAN") {
                            AiInterviewerPrompt(
                                prompt = "「面对现实世界的压力、突发挫折或长期不确定性，你的情绪韧性、抗逆力与专注心流时长如何？」",
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            QuickChipRow(
                                chips = listOf("能深度专注2小时以上", "擅长自我心理调节", "具备长期主义抗挫力", "偶尔精神内耗与焦虑", "坚持日常冥想或复盘", "情绪稳定不内耗"),
                                onChipClick = { sanInput = "$sanInput $it。" },
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            WizardInputField(
                                value = sanInput,
                                onValueChange = { sanInput = it },
                                textColor = textColor,
                                primaryColor = primaryGreen,
                                borderColor = borderColor
                            )
                        }
                    }

                    // 步骤 5：财务资产安全垫 (GOLD)
                    5 -> {
                        RetroContainer(title = "阶段 5/6 · 资产防御安全垫", subtitle = "GOLD") {
                            AiInterviewerPrompt(
                                prompt = "「《高性价比人生指南》强调防御先于进攻。你的储蓄率、应急储备金与现金流意识处于何种水平？」",
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            QuickChipRow(
                                chips = listOf("有6个月应急备用金", "坚持每日精细记账", "储蓄率超30%", "零负债轻装上阵", "理性消费极简生活", "积极定投指数资产"),
                                onChipClick = { goldInput = "$goldInput $it。" },
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            WizardInputField(
                                value = goldInput,
                                onValueChange = { goldInput = it },
                                textColor = textColor,
                                primaryColor = primaryGreen,
                                borderColor = borderColor
                            )
                        }
                    }

                    // 步骤 6：人际羁绊与支持系统 (BND)
                    6 -> {
                        RetroContainer(title = "阶段 6/6 · 人际羁绊支持网", subtitle = "BND") {
                            AiInterviewerPrompt(
                                prompt = "「在你的现实生活中，是否有能提供深度情感支持的核心亲密关系、同频战友或高价值良师？」",
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            QuickChipRow(
                                chips = listOf("亲密伴侣默契支持", "核心挚友3~5人", "独行侠·自得其乐", "有行业导师指路", "重视深度而非泛泛社交", "家庭后盾稳固"),
                                onChipClick = { bndInput = "$bndInput $it。" },
                                primaryColor = primaryGreen,
                                isCartridge = isCartridge
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            WizardInputField(
                                value = bndInput,
                                onValueChange = { bndInput = it },
                                textColor = textColor,
                                primaryColor = primaryGreen,
                                borderColor = borderColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 底部向导导航栏 (上一步 / 下一步 / 汇总推演)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (currentStep > 1) {
                        OutlinedButton(
                            onClick = { currentStep-- },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "上一步", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("上一步", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = {
                            if (currentStep < 6) {
                                currentStep++
                            } else {
                                // 汇总所有 6 步回答，交由 AI 严密推演
                                scope.launch {
                                    isLoading = true
                                    val consolidatedInput = buildString {
                                        appendLine("玩家昵称: $playerName, 历法年龄: $playerAge 岁, 卡头图腾: $selectedAvatar")
                                        appendLine("【学识与硬核技能 TEC】: $tecInput")
                                        appendLine("【体魄与天时作息 VIT】: $vitInput")
                                        appendLine("【心智意志与专注 SAN】: $sanInput")
                                        appendLine("【资产防御安全垫 GOLD】: $goldInput")
                                        appendLine("【人际羁绊支持网 BND】: $bndInput")
                                    }
                                    try {
                                        genesisResult = repository.initializeCharacter(
                                            historyInput = consolidatedInput,
                                            playerName = playerName,
                                            avatar = selectedAvatar
                                        )
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.weight(if (currentStep == 1) 1f else 1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AI 考官正在全面量化五维与特质...", fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        } else {
                            Text(
                                text = if (currentStep < 6) "确认 · 进入下一步" else "★ 汇总推演角色 (GENESIS)",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                            if (currentStep < 6) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "下一步", modifier = Modifier.size(16.dp), tint = Color.White)
                            }
                        }
                    }
                }
            }
        } else {
            // =========================================================
            // 角色创生完成面板 (Generated Character Genesis View)
            // =========================================================
            val res = genesisResult!!
            RetroContainer(title = "★ 角色档案创生完毕", subtitle = "初始等级: Lv.${res.level}") {
                // 玩家名片卡
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isCartridge) Color(0xFFF7F3EB) else Color(0xFF14241B), RoundedCornerShape(8.dp))
                        .border(1.5.dp, primaryGreen, RoundedCornerShape(8.dp))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(if (isCartridge) Color(0xFFEDE9DF) else RetroDarkBg, RoundedCornerShape(8.dp))
                            .border(2.dp, primaryGreen, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AvatarDisplay(avatar = selectedAvatar, size = 60.dp, fontSize = 32.sp)
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = playerName.ifBlank { "玩家" },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = textColor
                        )
                        Text(
                            text = "头衔: ${res.title}",
                            fontSize = 13.sp,
                            color = accentRed,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lv.${res.level} · 初始健康五维已注入",
                            fontSize = 11.sp,
                            color = if (isCartridge) CartridgeTextMuted else Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // AI 考官评语
                Text(
                    text = "/// 综合测评旁白：",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = res.commentary,
                    color = textColor,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 五维雷达图
                Text(
                    text = "/// 初始五维成长雷达图：",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                RadarChart(
                    tec = res.tec,
                    vit = res.vit,
                    san = res.san,
                    gold = res.gold,
                    rel = res.rel
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 天生特质列表
                Text(
                    text = "/// 天生特质与先天气运 (TRAITS)：",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                res.initialTraits.forEach { trait ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (trait.isDebuff) "[劣] " else "[优] ",
                            color = if (trait.isDebuff) CartridgeAccentRed else primaryGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${trait.name}: ${trait.description}",
                            color = textColor,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 底部双按钮：正式出征 or 重新构筑
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { genesisResult = null },
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, primaryGreen)
                    ) {
                        Text(
                            text = "↺ 重新生成",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = primaryGreen,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Button(
                        onClick = onComplete,
                        modifier = Modifier.weight(1.8f).height(46.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                    ) {
                        Text(
                            text = "★ 正式出征 · 进入人生",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }
    }
}

/**
 * AI 考官问询旁白小组件
 */
@Composable
private fun AiInterviewerPrompt(
    prompt: String,
    primaryColor: Color,
    isCartridge: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCartridge) Color(0xFFF9F7F2) else Color(0xFF14241B))
            .border(
                1.dp,
                if (isCartridge) CartridgeBorder.copy(alpha = 0.4f) else primaryColor.copy(alpha = 0.4f),
                RoundedCornerShape(6.dp)
            )
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🤖", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "AI 考官循证发问：",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = primaryColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prompt,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = if (isCartridge) CartridgeText else RetroGreenText,
                lineHeight = 17.sp
            )
        }
    }
}

/**
 * 快捷关键词点选标签流
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickChipRow(
    chips: List<String>,
    onChipClick: (String) -> Unit,
    primaryColor: Color,
    isCartridge: Boolean
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        chips.forEach { chip ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCartridge) Color(0xFFEDE9DF) else Color(0xFF14241B))
                    .border(
                        1.dp,
                        if (isCartridge) CartridgeBorder.copy(alpha = 0.5f) else primaryColor.copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { onChipClick(chip) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "+ $chip",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isCartridge) CartridgeText else RetroGreenText
                )
            }
        }
    }
}

/**
 * 向导输入框
 */
@Composable
private fun WizardInputField(
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color,
    primaryColor: Color,
    borderColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp),
        maxLines = 5,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        )
    )
}
