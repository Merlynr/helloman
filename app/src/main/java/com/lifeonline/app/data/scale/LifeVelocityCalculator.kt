package com.lifeonline.app.data.scale

import com.lifeonline.app.data.model.UserStats

/**
 * 生命流速计算结果
 */
data class LifeVelocityResult(
    val factor: Float,           // 流速倍率 (例如 0.85f ~ 1.35f)
    val factorText: String,       // 例如 "1.0x"
    val statusTag: String,        // 例如 "稳健从容"、"健康延展"、"气血亏虚·加速流失"
    val isWarning: Boolean,       // 是否报警
    val diagnosticSummary: String // 诊断细节
)

object LifeVelocityCalculator {

    /**
     * 根据身体状态（VIT气血、SAN心力、HP体魄、MP专注）以及今日传感器步数计算当前生命流速
     */
    fun calculate(stats: UserStats, steps: Int): LifeVelocityResult {
        var baseFactor = 1.0f

        // 1. 气血 VIT 惩罚与加成 (基准为 30)
        when {
            stats.vit < 20 -> baseFactor += 0.20f // 气血极虚，身体加速折旧
            stats.vit < 30 -> baseFactor += 0.10f
            stats.vit >= 60 -> baseFactor -= 0.10f // 气血充盈，抗衰自律延展
            stats.vit >= 80 -> baseFactor -= 0.15f
        }

        // 2. 心力 SAN 与精力 MP 惩罚
        if (stats.san < 20 || stats.mp < 30) {
            baseFactor += 0.10f // 长期内耗或专注透支
        }

        // 3. 计步传感器加成
        if (steps >= 8000) {
            baseFactor -= 0.05f
        } else if (steps < 2000) {
            baseFactor += 0.05f // 长期久坐代谢停滞
        }

        // 4. 打卡连击加成
        if (stats.vitStreak >= 7) {
            baseFactor -= 0.05f // 连续早起自律
        }

        val clamped = baseFactor.coerceIn(0.75f, 1.45f)
        val formattedFactor = String.format("%.2f", clamped)

        return when {
            clamped > 1.15f -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "⚠️ 气血透支 · 生命加速流逝",
                isWarning = true,
                diagnosticSummary = "体魄或心力处于低位，久坐或作息透支正在加速身体折损。请及时补充睡眠与轻量快走以降低流速。"
            )
            clamped < 0.95f -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "✨ 气血充盈 · 健康从容延展",
                isWarning = false,
                diagnosticSummary = "早起自律与活力步数形成良性代谢保护，身体基础机能高效运转，生命周期稳健延展。"
            )
            else -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "🟢 稳步推进 · 节律正常",
                isWarning = false,
                diagnosticSummary = "当前生活状态均衡，各项消耗处于人体标准基线，保持好节奏继续攻坚本篇章发展任务。"
            )
        }
    }
}
