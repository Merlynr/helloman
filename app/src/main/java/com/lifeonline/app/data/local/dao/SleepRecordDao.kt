package com.lifeonline.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lifeonline.app.data.model.SleepRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepRecordDao {

    @Query("SELECT * FROM sleep_records WHERE dateString = :dateString LIMIT 1")
    suspend fun getSleepRecordByDate(dateString: String): SleepRecord?

    @Query("SELECT * FROM sleep_records ORDER BY id DESC LIMIT 1")
    fun getLatestSleepRecordFlow(): Flow<SleepRecord?>

    @Query("SELECT * FROM sleep_records ORDER BY id DESC LIMIT 1")
    suspend fun getLatestSleepRecord(): SleepRecord?

    @Query("SELECT * FROM sleep_records ORDER BY dateString DESC LIMIT 7")
    fun getRecent7DaysSleepRecordsFlow(): Flow<List<SleepRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSleepRecord(record: SleepRecord): Long

    @Update
    suspend fun updateSleepRecord(record: SleepRecord)

    @Query("UPDATE sleep_records SET isDismissed = 1 WHERE id = :id")
    suspend fun markDismissed(id: Long)
}
