package com.lifeonline.app.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.lifeonline.app.data.model.CharacterGenesisResult
import com.lifeonline.app.data.repository.LifeOnlineRepository
import com.lifeonline.app.ui.components.RadarChart
import com.lifeonline.app.ui.components.RetroContainer
import com.lifeonline.app.ui.theme.RetroAmber
import com.lifeonline.app.ui.theme.RetroCardBg
import com.lifeonline.app.ui.theme.RetroCardBorder
import com.lifeonline.app.ui.theme.RetroDarkBg
import com.lifeonline.app.ui.theme.RetroGreenPrimary
import com.lifeonline.app.ui.theme.RetroGreenText
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    repository: LifeOnlineRepository,
    onComplete: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var inputExperience by remember {
        mutableStateOf(
            "学历与专业：计算机软件工程\n" +
            "过往重要成就：独立开发并上线开源工具，坚持健身半年，有长期理财储蓄意识\n" +
            "生活现状与习惯：偶尔熬夜写代码，心流状态好，社交偏内向，希望提升抗压力与规律作息"
        )
    }
    var isLoading by remember { mutableStateOf(false) }
    var genesisResult by remember { mutableStateOf<CharacterGenesisResult?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RetroDarkBg)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // 顶部欢迎标语
        Text(
            text = "/// 人生 ONLINE · 角色觉醒 ///",
            color = RetroGreenPrimary,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = "基于《高性价比人生指南》循证标尺，将你的现实过往经历转化为真实的 RPG 角色属性面板。",
            color = RetroGreenText.copy(alpha = 0.8f),
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (genesisResult == null) {
            // 输入经历表单
            RetroContainer(title = "录入个人现实经历与成就") {
                Text(
                    text = "提示：输入你的学历专业、工作经历、重要里程碑成就、目前的生活习惯或优缺点。AI 将客观量化你的初始五维属性与天生特质。",
                    color = RetroGreenText.copy(alpha = 0.7f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = inputExperience,
                    onValueChange = { inputExperience = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
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

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isLoading = true
                            try {
                                genesisResult = repository.initializeCharacter(inputExperience)
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    enabled = !isLoading && inputExperience.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroGreenPrimary,
                        contentColor = RetroDarkBg
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = RetroDarkBg,
                            modifier = Modifier.width(20.dp).height(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("AI 正在严密推演属性...", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    } else {
                        Text(
                            text = "[ 生成人生角色面板 (GENESIS) ]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            // 展示生成好的初始面板
            val res = genesisResult!!
            RetroContainer(title = "★ 角色生成完毕", subtitle = "初始等级: Lv.${res.level}") {
                Text(
                    text = "头衔：${res.title}",
                    color = RetroAmber,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = res.commentary,
                    color = RetroGreenText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 五维雷达图
                RadarChart(
                    tec = res.tec,
                    vit = res.vit,
                    san = res.san,
                    gold = res.gold,
                    rel = res.rel
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "/// 天生特质 (TRAITS)",
                    color = RetroGreenPrimary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )

                res.initialTraits.forEach { trait ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                    ) {
                        Text(
                            text = if (trait.isDebuff) "[劣] " else "[优] ",
                            color = if (trait.isDebuff) Color(0xFFEF4444) else RetroGreenPrimary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${trait.name}: ${trait.description}",
                            color = RetroGreenText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onComplete,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RetroGreenPrimary,
                        contentColor = RetroDarkBg
                    ),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "[ 正式进入人生 ONLINE ]",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
