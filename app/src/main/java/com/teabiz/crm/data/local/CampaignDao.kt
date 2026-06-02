package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.Campaign
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :campaignId")
    suspend fun getCampaignById(campaignId: String): Campaign?

    @Query("SELECT * FROM campaigns WHERE status = :status ORDER BY createdAt DESC")
    fun getCampaignsByStatus(status: String): Flow<List<Campaign>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)

    @Query("SELECT COUNT(*) FROM campaigns")
    fun getCampaignCount(): Flow<Int>
}
