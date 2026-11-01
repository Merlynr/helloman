package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 玩家核心属性面板实体
 */
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val name: String = "冒险者",
    val title: String = "初窥门径的探索者",
    val level: Int = 1,
    val exp: Int = 0,
    val maxExp: Int = 100,

    // 五维核心属性 (0 - 100 基准数值)
    val tec: Int = 20,   // 智力 / 脑力 / 技能
    val vit: Int = 20,   // 气血 / 体魄 / 耐力
    val san: Int = 20,   // 心力 / 精神 / 意志
    val gold: Int = 20,  // 财力 / 资产 / 现金流
    val bnd: Int = 20,   // 人脉 / 社交 / 亲密关系

    // 生存槽位
    val hp: Int = 100,
    val maxHp: Int = 100,
    val mp: Int = 100,
    val maxMp: Int = 100,

    // Streak 打卡连续天数
    val vitStreak: Int = 0,     // 气血连续打卡（早起/运动）
    val tecStreak: Int = 0,     // 算力连续打卡（学习）
    val sanStreak: Int = 0,     // 心力连续打卡（冥想）
    val goldStreak: Int = 0,    // 财商连续打卡（记账）
    val bndStreak: Int = 0,     // 人脉连续打卡（社交）
    val lastStreakDate: String = "", // 上次打卡日期 yyyy-MM-dd

    // AI 生成的动态称号，区别于 title
    val aiTitle: String = "",

    // 是否已完成初始化经历录入
    val isInitialized: Boolean = false,
    val backgroundStory: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
