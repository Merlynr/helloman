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
    val rel: Int = 20,   // 人脉 / 社交 / 亲密关系

    // 生存槽位
    val hp: Int = 100,
    val maxHp: Int = 100,
    val mp: Int = 100,
    val maxMp: Int = 100,

    // 是否已完成初始化经历录入
    val isInitialized: Boolean = false,
    val backgroundStory: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
