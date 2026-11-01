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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    repository: LifeOnlineRepository,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val isCartridge = LocalIsCartridgeTheme.current

    val primaryGreen = if (isCartridge) CartridgeGreen else RetroGreenPrimary
    val bgColor = if (isCartridge) CartridgeBg else RetroDarkBg
    val cardBg = if (isCartridge) CartridgeCardWhite else RetroCardBg
    val textColor = if (isCartridge) CartridgeText else RetroGreenText
    val borderColor = if (isCartridge) CartridgeBorder else RetroCardBorder
    val accentRed = if (isCartridge) CartridgeAccentRed else RetroAmber

    // 向导步骤状态 (1 ~ 6)
    var currentStep by remember { mutableIntStateOf(1) }

    // 步骤 1：基础身份与卡头
    var playerName by remember { mutableStateOf("玩家") }
    var selectedAvatar by remember { mutableStateOf("🐻") }
    var playerAge by remember { mutableStateOf("25") }

    // 步骤 2：学识与硬核专业 (TEC)
    var tecInput by remember { mutableStateOf("计算机软件工程，独立开发过工具软件，擅长系统架构设计与工程攻坚。") }

    // 步骤 3：体魄与天时作息 (VIT)
    var vitInput by remember { mutableStateOf("日常步数6000左右，坚持每周跑步2次，但偶尔写代码熬夜至凌晨，希望养成22:30前规律作息。") }

    // 步骤 4：心理意志与心流 (SAN)
    var sanInput by remember { mutableStateOf("能保持深度专注心流2小时，抗挫力尚可，但面对突发压力偶有内耗，希望筑牢心理安全垫。") }

    // 步骤 5：财务资产与防御 (GOLD)
    var goldInput by remember { mutableStateOf("有6个月应急备用金储备，有记账习惯与定投意识，追求高性价比消费与稳健防御。") }

    // 步骤 6：人际羁绊与人脉 (BND)
    var bndInput by remember { mutableStateOf("有默契的核心好友与家人支持系统，偏向深度社交，重视与同频良师益友的长期共赢。") }

    var isLoading by remember { mutableStateOf(false) }
    var genesisResult by remember { mutableStateOf<CharacterGenesisResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部大标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "/// 人生 ONLINE · 角色创生 ///",
                    color = primaryGreen,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                )
                Text(
                    text = "《高性价比人生指南》循证测评 · AI 考官问询向导",
                    color = if (isCartridge) CartridgeTextMuted else RetroGreenText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp
                )
            }

            if (genesisResult == null) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(primaryGreen.copy(alpha = 0.15f))
                        .border(1.dp, primaryGreen, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "步骤 $currentStep / 6",
                        color = primaryGreen,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (genesisResult == null) {
            // 向导进度条
            PixelSegmentedBar(
                progress = currentStep / 6f,
                activeColor = primaryGreen
            )

            Spacer(modifier = Modifier.height(14.dp))

            when (currentStep) {
                // =========================================================
                // 步骤 1：基础设定 (姓名、卡头头像、年龄)
                // =========================================================
                1 -> {
                    RetroContainer(title = "阶段 1/6 · 现实坐标与图腾", subtitle = "IDENTITY & AVATAR") {
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
                                unfocusedBorderColor = borderColor
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // 历法年龄
                        Text(
                            text = "历法年龄 (用于定位人生篇章)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = playerAge,
                            onValueChange = { playerAge = it.filter { ch -> ch.isDigit() }.take(2) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor,
                                focusedBorderColor = primaryGreen,
                                unfocusedBorderColor = borderColor
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // 卡头图腾选择器
                        AvatarPicker(
                            selectedAvatar = selectedAvatar,
                            onAvatarSelected = { selectedAvatar = it },
                            isCartridge = isCartridge
                        )
                    }
                }

                // =========================================================
                // 步骤 2：学识与硬技能 (TEC)
                // =========================================================
                2 -> {
                    RetroContainer(title = "阶段 2/6 · 智识与硬核技能", subtitle = "TEC / KNOWLEDGE") {
                        AiInterviewerPrompt(
                            prompt = "「在现代社会的竞争网络中，你的核心专业技能与认知壁垒是什么？具备哪些高含金量资质证书或重要攻坚目标？」",
                            primaryColor = primaryGreen,
                            isCartridge = isCartridge
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        QuickChipRow(
                            chips = listOf("计算机软件/IT研发", "公务员/体制内法考", "法学司法/合规", "财会金融/CPA", "工业制造/工程", "设计/数字创意"),
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

                // =========================================================
                // 步骤 3：体魄与天时作息 (VIT)
                // =========================================================
                3 -> {
                    RetroContainer(title = "阶段 3/6 · 体魄与天时作息", subtitle = "VIT / VITALITY") {
                        AiInterviewerPrompt(
                            prompt = "「《黄帝内经》与现代神经科学表明，天时安歇是生命流速的第一基石。你平时的入眠时刻（是否熬夜>22:30）、运动频率与体能如何？」",
                            primaryColor = primaryGreen,
                            isCartridge = isCartridge
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        QuickChipRow(
                            chips = listOf("22:30前规律就寝", "熬夜写代码>01:00", "晨起7点精力充沛", "每周运动慢跑3次", "日间伏案久坐严重", "身体防御垫良好"),
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

                // =========================================================
                // 步骤 4：心理意志与专注 (SAN)
                // =========================================================
                4 -> {
                    RetroContainer(title = "阶段 4/6 · 心智意志与专注", subtitle = "SAN / RESILIENCE") {
                        AiInterviewerPrompt(
                            prompt = "「面对突发逆境、职场压力或长期不确定性时，你的精神抗挫力与专注心流表现如何？是否容易陷入情绪内耗？」",
                            primaryColor = primaryGreen,
                            isCartridge = isCartridge
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        QuickChipRow(
                            chips = listOf("抗压强·内控型人格", "心流深·能持续专注2h", "偶有情绪内耗焦虑", "有冥想/正念习惯", "社交钝感力强", "极强目标执行力"),
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

                // =========================================================
                // 步骤 5：财务资产安全垫 (GOLD)
                // =========================================================
                5 -> {
                    RetroContainer(title = "阶段 5/6 · 资产防御安全垫", subtitle = "GOLD / FORTUNE") {
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

                // =========================================================
                // 步骤 6：人际羁绊与支持系统 (BND)
                // =========================================================
                6 -> {
                    RetroContainer(title = "阶段 6/6 · 人际羁绊支持网", subtitle = "BND / BOND") {
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

                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryGreen),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "★ 正式出征现实世界 · 进入人生 ONLINE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
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
            .background(if (isCartridge) Color(0xFFF3EFE7) else Color(0xFF14241B))
            .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(
                text = "🤖 AI 考官循证发问：",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = prompt,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = if (isCartridge) CartridgeText else RetroGreenText,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

/**
 * 快速标签轻选栏
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
        chips.forEach { chipText ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (isCartridge) Color(0xFFEDE9DF) else Color(0xFF161C15))
                    .border(1.dp, primaryColor.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .clickable { onChipClick(chipText) }
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "+ $chipText",
                    fontSize = 10.sp,
                    color = primaryColor,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
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
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = primaryColor,
            unfocusedBorderColor = borderColor
        ),
        shape = RoundedCornerShape(4.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
    )
}
