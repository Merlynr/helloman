package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 阶段战役实体 (如：2026公务员考试上岸冲刺)
 */
@Entity(tableName = "campaigns")
data class Campaign(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val targetDate: String = "2026-12-01",
    val currentProgress: Int = 504,
    val targetTotal: Int = 1200,
    val unit: String = "题",
    val chapterId: Long = 1,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val stageName: String = "阶段二：真题与模块题海冲刺"
) {
    val progressPercent: Float
        get() = if (targetTotal > 0) (currentProgress.toFloat() / targetTotal).coerceIn(0f, 1f) else 0f
}
