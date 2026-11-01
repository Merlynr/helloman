package com.lifeonline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeonline.app.data.model.BuffItem
import kotlinx.coroutines.flow.Flow

@Dao
interface BuffDao {
    @Query("SELECT * FROM buff_items ORDER BY isDebuff ASC, id DESC")
    fun getAllBuffsFlow(): Flow<List<BuffItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuff(buff: BuffItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBuffs(buffs: List<BuffItem>)

    @Delete
    suspend fun deleteBuff(buff: BuffItem)

    @Query("DELETE FROM buff_items WHERE remainingDays <= 0")
    suspend fun cleanExpiredBuffs()
}
