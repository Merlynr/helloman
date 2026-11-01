package com.lifeonline.app.data.scale

import com.lifeonline.app.data.model.SedentaryStatus
import com.lifeonline.app.data.model.SleepRecord
import com.lifeonline.app.data.model.UserStats
import java.util.Locale

/**
 * 生命流速计算结果
 */
data class LifeVelocityResult(
    val factor: Float,             // 流速倍率 (例如 0.80f ~ 1.45f)
    val factorText: String,        // 例如 "0.85x" 或 "1.25x"
    val statusTag: String,         // 例如 "稳健从容"、"健康延展"、"气血亏虚·加速流失"
    val isWarning: Boolean,        // 是否报警
    val diagnosticSummary: String, // 诊断细节
    val sleepImpactDesc: String = "",   // 睡眠调谐拆解，例如 "-0.12x (昨夜天时充养)"
    val sedentaryImpactDesc: String = "",// 久坐调谐拆解，例如 "+0.05x (日间伏案停滞)"
    val hasLongevityBuff: Boolean = false // 是否激活 7 天自律 0.80x 龟息抗衰加成
)

object LifeVelocityCalculator {

    /**
     * 根据身体状态、传感器步数、昨夜睡眠质量与日间久坐状态计算生命流速
     */
    fun calculate(
        stats: UserStats,
        steps: Int,
        sleepRecord: SleepRecord? = null,
        sedentaryStatus: SedentaryStatus = SedentaryStatus()
    ): LifeVelocityResult {
        var baseFactor = 1.0f
        var sleepDesc = ""
        var sedentaryDesc = ""
        var longevityBuffActive = false

        // 1. 气血 VIT 基础调节 (基准为 30)
        when {
            stats.vit < 20 -> baseFactor += 0.15f
            stats.vit < 30 -> baseFactor += 0.08f
            stats.vit >= 60 -> baseFactor -= 0.08f
            stats.vit >= 80 -> baseFactor -= 0.12f
        }

        // 2. 心力 SAN 与精力 MP 惩罚
        if (stats.san < 20 || stats.mp < 30) {
            baseFactor += 0.08f
        }

        // 3. 计步传感器加成
        if (steps >= 8000) {
            baseFactor -= 0.05f
        } else if (steps < 2000) {
            baseFactor += 0.04f
        }

        // 4. 昨夜睡眠质量核心调谐 (睡眠为主，占大头)
        if (sleepRecord != null) {
            if (sleepRecord.isLateNight) {
                // 晚于 22:30 入睡即为熬夜
                baseFactor += 0.18f
                sleepDesc = "+0.18x ⚠️ 熬夜透支 (晚于22:30入眠)"
            } else if (sleepRecord.isEarlyBird) {
                // 遵循天时且早起充足
                baseFactor -= 0.12f
                sleepDesc = "-0.12x ✨ 天时安歇 (22:30前歇/7点起)"
            } else {
                // 普通正常作息
                baseFactor -= 0.06f
                sleepDesc = "-0.06x ● 规律作息 (${String.format(Locale.getDefault(), "%.1f", sleepRecord.durationMinutes / 60.0f)}h)"
            }
        }

        // 5. 7 天作息健康连胜奖励 (解锁 0.80x 极慢衰老 Buff)
        if (stats.vitStreak >= 7 && (sleepRecord == null || !sleepRecord.isLateNight)) {
            baseFactor -= 0.08f
            longevityBuffActive = true
        }

        // 6. 日间久坐异常惩罚 (+0.05x 经络淤滞)
        if (sedentaryStatus.isSedentary) {
            baseFactor += 0.05f
            sedentaryDesc = "+0.05x ⚠️ 连续伏案${sedentaryStatus.inactiveMinutes}m 经络受阻"
        }

        val clamped = baseFactor.coerceIn(0.80f, 1.45f)
        val formattedFactor = String.format(Locale.getDefault(), "%.2f", clamped)

        return when {
            longevityBuffActive && clamped <= 0.82f -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "🏆 龟息充盈 · 极慢衰老 (7日自律)",
                isWarning = false,
                diagnosticSummary = "连续7天早起天时安歇，激活【龟息充盈】抗衰护盾！身体代谢消耗降至极低阈值，生命延展复利最大化！",
                sleepImpactDesc = sleepDesc,
                sedentaryImpactDesc = sedentaryDesc,
                hasLongevityBuff = true
            )
            clamped > 1.15f -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "⚠️ 气血透支 · 生命加速流逝",
                isWarning = true,
                diagnosticSummary = "昨夜熬夜或伏案久坐导致阳气未潜藏，生命折旧速度正在加快。建议今日多饮水，今晚22:30前务必归元就寝！",
                sleepImpactDesc = sleepDesc,
                sedentaryImpactDesc = sedentaryDesc,
                hasLongevityBuff = false
            )
            clamped < 0.95f -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "✨ 气血充盈 · 健康从容延展",
                isWarning = false,
                diagnosticSummary = "天时安歇与早起活力形成良性细胞代谢修复，生命流速处于极佳放缓状态，抗衰防线稳固。",
                sleepImpactDesc = sleepDesc,
                sedentaryImpactDesc = sedentaryDesc,
                hasLongevityBuff = false
            )
            else -> LifeVelocityResult(
                factor = clamped,
                factorText = "${formattedFactor}x",
                statusTag = "🟢 稳步推进 · 节律正常",
                isWarning = false,
                diagnosticSummary = "身心机能处于健康平稳区间，各项生理指标符合标准基线，保持此节奏继续攻坚本篇章发展任务。",
                sleepImpactDesc = sleepDesc,
                sedentaryImpactDesc = sedentaryDesc,
                hasLongevityBuff = false
            )
        }
    }
}
