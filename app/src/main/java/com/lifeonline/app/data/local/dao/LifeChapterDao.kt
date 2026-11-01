package com.lifeonline.app.data.local.dao

import androidx.room.*
import com.lifeonline.app.data.model.LifeChapter
import kotlinx.coroutines.flow.Flow

@Dao
interface LifeChapterDao {
    @Query("SELECT * FROM life_chapters ORDER BY startAge ASC")
    fun getAllChaptersFlow(): Flow<List<LifeChapter>>

    @Query("SELECT * FROM life_chapters WHERE isActive = 1 LIMIT 1")
    fun getActiveChapterFlow(): Flow<LifeChapter?>

    @Query("SELECT * FROM life_chapters WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveChapter(): LifeChapter?

    @Query("SELECT * FROM life_chapters WHERE id = :id")
    suspend fun getChapterById(id: Long): LifeChapter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapter(chapter: LifeChapter): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChapters(chapters: List<LifeChapter>)

    @Update
    suspend fun updateChapter(chapter: LifeChapter)

    @Delete
    suspend fun deleteChapter(chapter: LifeChapter)
}
