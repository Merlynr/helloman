package com.lifeonline.app.data.local.dao

import androidx.room.*
import com.lifeonline.app.data.model.SideQuest
import kotlinx.coroutines.flow.Flow

@Dao
interface SideQuestDao {
    @Query("SELECT * FROM side_quests WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveSideQuestsFlow(): Flow<List<SideQuest>>

    @Query("SELECT * FROM side_quests ORDER BY id DESC")
    fun getAllSideQuestsFlow(): Flow<List<SideQuest>>

    @Query("SELECT * FROM side_quests WHERE id = :id LIMIT 1")
    suspend fun getSideQuestById(id: Long): SideQuest?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSideQuest(sideQuest: SideQuest): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSideQuests(sideQuests: List<SideQuest>)

    @Update
    suspend fun updateSideQuest(sideQuest: SideQuest)

    @Delete
    suspend fun deleteSideQuest(sideQuest: SideQuest)

    @Query("SELECT COUNT(*) FROM side_quests")
    suspend fun getCount(): Int
}
