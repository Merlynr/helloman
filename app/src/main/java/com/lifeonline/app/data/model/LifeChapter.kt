package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 人生宏观篇章实体 (如：25~35 岁 · 立业与职业护城河黄金十年)
 */
@Entity(tableName = "life_chapters")
data class LifeChapter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "25 ~ 35 岁 · 立业与职业护城河黄金十年",
    val startAge: Int = 25,
    val endAge: Int = 35,
    val currentAge: Int = 25,
    val daysPassed: Int = 319,
    val description: String = "根据《高性价比人生指南》，25~35岁是个人核心技能定型、抗风险安全垫搭建的最关键窗口期。此时应防御优先于盲目盲干，全力攻坚体制或专业资质护城河。",
    val focusAttributes: String = "TEC,GOLD,VIT",
    val isActive: Boolean = true
)
