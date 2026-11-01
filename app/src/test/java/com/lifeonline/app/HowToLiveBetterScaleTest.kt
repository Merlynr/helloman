package com.lifeonline.app

import com.lifeonline.app.data.model.UserStats
import com.lifeonline.app.data.scale.HowToLiveBetterScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HowToLiveBetterScaleTest {

    @Test
    fun testBuildCharacterGenesisPrompt() {
        val input = "计算机专业毕业，工作3年，完成过马拉松，有记账习惯。"
        val prompt = HowToLiveBetterScale.buildCharacterGenesisPrompt(input)
        assertTrue(prompt.contains(input))
        assertTrue(prompt.contains("高性价比人生指南"))
    }

    @Test
    fun testBuildSettlementPrompt() {
        val stats = UserStats(title = "代码剑客", level = 2, tec = 40, vit = 30)
        val prompt = HowToLiveBetterScale.buildSettlementPrompt(
            stats,
            "今天夜跑5公里，学习Kotlin2小时",
            8500,
            listOf("早起打卡", "核心肌群锻炼")
        )
        assertTrue(prompt.contains("8500 步"))
        assertTrue(prompt.contains("代码剑客"))
    }

    @Test
    fun testLocalFallbackSettlement_ExerciseAndLearning() {
        val input = "下午去跑步5公里，晚上学习写代码2小时，午饭外卖花费28元。"
        val result = HowToLiveBetterScale.calculateLocalFallbackSettlement(
            todayInput = input,
            stepCount = 10500,
            completedTasks = listOf("规律作息")
        )

        assertNotNull(result)
        // 步数 >= 10000 且 有跑步，气血必定大幅提升
        assertTrue("VIT 应该显著增长", result.attributeDeltas.vit >= 20)
        // 有学习写代码，智力提升
        assertTrue("TEC 应该有增长", result.attributeDeltas.tec >= 12)
        // 消费 28 元，财力对应减少
        assertEquals(-28, result.attributeDeltas.gold)
        // 经验值掉落显著
        assertTrue("经验应充足", result.expGained >= 40)
        // 包含三层标尺点评
        assertTrue(result.scaleEvaluation.shortTerm.isNotBlank())
        assertTrue(result.scaleEvaluation.midTerm.isNotBlank())
        assertTrue(result.scaleEvaluation.longTerm.isNotBlank())
    }

    @Test
    fun testLocalFallbackSettlement_StayUpLatePenalty() {
        val input = "昨天严重熬夜失眠，今天整个人很疲惫。"
        val result = HowToLiveBetterScale.calculateLocalFallbackSettlement(
            todayInput = input,
            stepCount = 1200,
            completedTasks = emptyList()
        )

        assertTrue("熬夜应当扣减 VIT", result.attributeDeltas.vit < 0)
        assertTrue("熬夜应当扣减 SAN", result.attributeDeltas.san < 0)
        assertTrue("应生成睡眠欠债 DEBUFF", result.debuffsApplied.any { it.contains("睡眠") })
    }

    @Test
    fun testDefaultGenesis() {
        val result = HowToLiveBetterScale.createDefaultGenesis("自学计算机与跑步爱好者")
        assertEquals(1, result.level)
        assertTrue(result.initialTraits.isNotEmpty())
        assertEquals(25, result.tec)
    }
}
