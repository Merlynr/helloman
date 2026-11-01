package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作息与睡眠质量分析实体
 * 规则：
 * - 晚于 22:30 入睡即判定为熬夜
 * - 07:00 前后起为正常/优质作息
 */
@Entity(tableName = "sleep_records")
data class SleepRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String = "",          // 日期，如 "2026-11-01"
    val sleepTimeMillis: Long = 0L,        // 昨夜入睡时间戳
    val wakeTimeMillis: Long = 0L,         // 今晨苏醒时间戳
    val durationMinutes: Int = 0,          // 总睡眠分钟数
    val isLateNight: Boolean = false,      // 是否晚于 22:30 入睡
    val isEarlyBird: Boolean = false,      // 是否早于等于 07:30 起床且睡足
    val sleepScore: Int = 85,              // 作息综合健康分 (0-100)
    val statusTitle: String = "正常作息",   // RPG状态称号：如 "气血充盈·早起"、"熬夜透支·气血亏虚"
    val description: String = "",          // 详细评估文本
    val vitDelta: Int = 10,                // 气血变化值 (+15 或 -20)
    val hpDelta: Int = 0,                  // HP 变动
    val isDismissed: Boolean = false       // 用户是否已在晨间弹窗确认过
)

/**
 * 白天久坐监测状态
 */
data class SedentaryStatus(
    val isSedentary: Boolean = false,
    val inactiveMinutes: Int = 0,
    val stepsWalkedSinceSedentary: Int = 0,
    val targetStepsToClear: Int = 100
)
