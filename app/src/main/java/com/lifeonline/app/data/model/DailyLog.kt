package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 每日流水记录与 AI 战报结算实体
 */
@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateString: String,      // 格式：2026-11-01
    val rawInput: String,        // 用户录入的原始流水账
    val dialogue: String,        // AI 生成的 RPG 战报旁白
    val expGained: Int,          // 获得的总经验
    val tecDelta: Int = 0,       // 智力变动
    val vitDelta: Int = 0,       // 气血变动
    val sanDelta: Int = 0,       // 心力变动
    val goldDelta: Int = 0,      // 财力变动
    val relDelta: Int = 0,       // 人脉变动
    val stepCount: Int = 0,      // 自动采集的步数
    
    // 《高性价比人生指南》三层标尺多周期评估
    val shortTermEval: String = "",  // 近期评价：微习惯与投入产出比
    val midTermEval: String = "",    // 阶段性评价：习惯固化与抗风险
    val longTermEval: String = "",   // 长期评价：复利型资产与防重大风险
    
    val timestamp: Long = System.currentTimeMillis()
)
