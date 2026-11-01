package com.lifeonline.app

import com.lifeonline.app.data.model.UserStats
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UserStatsEvolutionTest {

    @Test
    fun testLevelUpCalculation() {
        var stats = UserStats(
            level = 1,
            exp = 80,
            maxExp = 100
        )

        val expGained = 50
        var newExp = stats.exp + expGained
        var newLevel = stats.level
        var newMaxExp = stats.maxExp

        while (newExp >= newMaxExp) {
            newExp -= newMaxExp
            newLevel += 1
            newMaxExp = (newMaxExp * 1.25f).toInt()
        }

        assertEquals(2, newLevel)
        assertEquals(30, newExp)
        assertEquals(125, newMaxExp)
    }

    @Test
    fun testMultiLevelUpCalculation() {
        var stats = UserStats(
            level = 1,
            exp = 0,
            maxExp = 100
        )

        // 巨额经验掉落 (+350 EXP)
        val expGained = 350
        var newExp = stats.exp + expGained
        var newLevel = stats.level
        var newMaxExp = stats.maxExp

        while (newExp >= newMaxExp) {
            newExp -= newMaxExp
            newLevel += 1
            newMaxExp = (newMaxExp * 1.25f).toInt()
        }

        assertTrue("等级应连升多级", newLevel >= 3)
        assertTrue("剩余经验必须小于新等级的经验上限", newExp < newMaxExp)
    }
}
