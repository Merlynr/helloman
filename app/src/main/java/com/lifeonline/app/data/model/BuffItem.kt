package com.lifeonline.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "buff_items")
data class BuffItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val isDebuff: Boolean = false,
    val remainingDays: Int = 1
)
