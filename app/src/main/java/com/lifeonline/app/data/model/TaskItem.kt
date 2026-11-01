package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskType {
    DAILY,        // 日常习惯打卡
    WEEKLY_MAIN,  // 本周主线任务
    SIDE_QUEST    // 偶发支线挑战
}

@Entity(tableName = "task_items")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val taskType: TaskType = TaskType.DAILY,
    val targetAttribute: String = "VIT", // TEC, VIT, SAN, GOLD, REL
    val expReward: Int = 15,
    val isCompleted: Boolean = false,
    val dateString: String = "",
    val campaignId: Long? = null,
    val campaignTag: String = "",         // e.g. "🎯 2026考公"
    val campaignProgressAdd: Int = 0     // e.g. 50 题
)
