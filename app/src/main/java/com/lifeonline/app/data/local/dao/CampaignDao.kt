package com.lifeonline.app.data.local.dao

import androidx.room.*
import com.lifeonline.app.data.model.Campaign
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY isActive DESC, id ASC")
    fun getAllCampaignsFlow(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE isActive = 1 LIMIT 1")
    fun getActiveCampaignFlow(): Flow<Campaign?>

    @Query("SELECT * FROM campaigns WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveCampaign(): Campaign?

    @Query("SELECT * FROM campaigns WHERE id = :id")
    suspend fun getCampaignById(id: Long): Campaign?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaigns(campaigns: List<Campaign>)

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)
}
