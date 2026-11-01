package com.lifeonline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lifeonline.app.data.model.DailyLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {
    @Query("SELECT * FROM daily_logs ORDER BY timestamp DESC")
    fun getAllLogsFlow(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs WHERE dateString = :dateStr ORDER BY timestamp DESC")
    suspend fun getLogsForDate(dateStr: String): List<DailyLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DailyLog): Long

    @Query("SELECT SUM(expGained) FROM daily_logs")
    suspend fun getTotalExpGained(): Int?
}
