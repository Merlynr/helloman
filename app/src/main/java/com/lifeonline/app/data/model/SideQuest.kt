package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 支线挑战 / 中期微目标实体 (如: 《情绪自救手册》精读、驾照攻坚、减脂3kg)
 */
@Entity(tableName = "side_quests")
data class SideQuest(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,                // 支线标题 (e.g. 情绪自救手册)
    val description: String = "",      // 详细说明 (e.g. 精读伯恩斯认知疗法，掌握情绪ABC)
    val icon: String = "📖",          // 类别图标 (📖, 🚗, 🏃, 💻, 🎯)
    val currentProgress: Int = 0,     // 当前完成量 (e.g. 18)
    val targetTotal: Int = 10,        // 目标总量 (e.g. 25)
    val unit: String = "章",           // 单位 (章, 节, 课, km, 天)
    val targetAttribute: String = "SAN", // 关联属性 (TEC, VIT, SAN, GOLD, BND)
    val campaignId: Long? = null,      // 所属主线战役ID (可选联动)
    val expRewardPerStep: Int = 15,    // 每次推进奖励的 EXP
    val isActive: Boolean = true,
    val isCompleted: Boolean = false
) {
    val progressPercent: Float
        get() = if (targetTotal > 0) (currentProgress.toFloat() / targetTotal).coerceIn(0f, 1f) else 0f
}
