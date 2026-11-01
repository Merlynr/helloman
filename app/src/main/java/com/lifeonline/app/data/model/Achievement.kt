package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AchievementRarity(val label: String, val badgeColorHex: String) {
    BRONZE("铜", "#CD7F32"),
    SILVER("银", "#A8A9AD"),
    GOLD("金", "#D4AF37"),
    SECRET("隐·彩", "#E67E22")
}

enum class AchievementCategory(val label: String) {
    LIFE_GUIDE("高性价比人生"), // 对应《高性价比人生指南》重大人生事件与办事
    HABIT_STREAK("习惯打卡"),   // 连续打卡与作息
    MILESTONE("成长里程碑"),   // 等级与长期坚持
    SECRET("隐藏彩蛋")        // 彩蛋发现
}

@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val icon: String, // emoji 图标
    val rarity: AchievementRarity = AchievementRarity.BRONZE,
    val category: AchievementCategory = AchievementCategory.LIFE_GUIDE,
    val guideChapter: String = "", // e.g. "第27章 · 怀孕和生产"
    val guideTip: String = "", // 《高性价比人生指南》硬核建议/避坑要点
    val triggerKeywords: String = "", // 匹配关键词用逗号隔开，用于即时识别用户日记
    val unlockedAt: Long? = null, // null = 未解锁
    val isHidden: Boolean = false // 隐藏成就在解锁前显示「???」
)
