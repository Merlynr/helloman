package com.lifeonline.app.data.model

data class DecomposedTask(
    val title: String,
    val taskType: TaskType = TaskType.DAILY,
    val targetAttribute: String = "VIT", // TEC, VIT, SAN, GOLD, REL
    val expReward: Int = 15,
    var isSelected: Boolean = true
)
