package com.lifeonline.app.data.local.dao

import androidx.room.*
import com.lifeonline.app.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements ORDER BY (unlockedAt IS NULL), unlockedAt DESC")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE unlockedAt IS NOT NULL ORDER BY unlockedAt DESC")
    suspend fun getUnlockedAchievements(): List<Achievement>

    @Query("SELECT * FROM achievements WHERE id = :id LIMIT 1")
    suspend fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: Achievement)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)

    @Query("SELECT COUNT(*) FROM achievements")
    suspend fun getTotalCount(): Int

    @Query("SELECT COUNT(*) FROM achievements WHERE unlockedAt IS NOT NULL")
    suspend fun getUnlockedCount(): Int
}
