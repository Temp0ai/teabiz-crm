package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CampaignDao {
    @Query("SELECT * FROM campaigns ORDER BY createdAt DESC")
    fun getAllCampaigns(): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE id = :campaignId")
    suspend fun getCampaignById(campaignId: String): Campaign?

    @Query("SELECT * FROM campaigns WHERE status = :status ORDER BY createdAt DESC")
    fun getCampaignsByStatus(status: String): Flow<List<Campaign>>

    @Query("SELECT * FROM campaigns WHERE status = 'SCHEDULED' AND scheduledAt <= :currentTime")
    suspend fun getScheduledCampaigns(currentTime: Long): List<Campaign>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampaign(campaign: Campaign): Long

    @Update
    suspend fun updateCampaign(campaign: Campaign)

    @Delete
    suspend fun deleteCampaign(campaign: Campaign)

    @Query("SELECT COUNT(*) FROM campaigns")
    fun getCampaignCount(): Flow<Int>

    @Query("SELECT SUM(sentCount) FROM campaigns")
    fun getTotalSentCount(): Flow<Int?>

    @Query("SELECT SUM(repliedCount) FROM campaigns")
    fun getTotalRepliedCount(): Flow<Int?>

    @Query("SELECT SUM(convertedCount) FROM campaigns")
    fun getTotalConvertedCount(): Flow<Int?>
}

@Dao
interface CampaignTemplateDao {
    @Query("SELECT * FROM campaign_templates ORDER BY useCount DESC")
    fun getAllTemplates(): Flow<List<CampaignTemplate>>

    @Query("SELECT * FROM campaign_templates WHERE id = :templateId")
    suspend fun getTemplateById(templateId: String): CampaignTemplate?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: CampaignTemplate): Long

    @Update
    suspend fun updateTemplate(template: CampaignTemplate)

    @Delete
    suspend fun deleteTemplate(template: CampaignTemplate)

    @Query("UPDATE campaign_templates SET useCount = useCount + 1 WHERE id = :templateId")
    suspend fun incrementUseCount(templateId: String)
}

@Dao
interface BlacklistDao {
    @Query("SELECT * FROM campaign_blacklist ORDER BY blacklistedAt DESC")
    fun getAllBlacklisted(): Flow<List<BlacklistedContact>>

    @Query("SELECT EXISTS(SELECT 1 FROM campaign_blacklist WHERE phone = :phone)")
    suspend fun isBlacklisted(phone: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBlacklisted(contact: BlacklistedContact)

    @Delete
    suspend fun removeBlacklisted(contact: BlacklistedContact)

    @Query("DELETE FROM campaign_blacklist WHERE phone = :phone")
    suspend fun removeByPhone(phone: String)
}

@Dao
interface CampaignAnalyticsDao {
    @Query("SELECT * FROM campaign_analytics WHERE campaignId = :campaignId ORDER BY sentAt DESC")
    fun getCampaignAnalytics(campaignId: String): Flow<List<CampaignAnalytics>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnalytics(analytics: CampaignAnalytics)

    @Query("SELECT COUNT(*) FROM campaign_analytics WHERE campaignId = :campaignId AND readAt IS NOT NULL")
    suspend fun getReadCount(campaignId: String): Int

    @Query("SELECT COUNT(*) FROM campaign_analytics WHERE campaignId = :campaignId AND repliedAt IS NOT NULL")
    suspend fun getRepliedCount(campaignId: String): Int

    @Query("SELECT COUNT(*) FROM campaign_analytics WHERE campaignId = :campaignId AND convertedAt IS NOT NULL")
    suspend fun getConvertedCount(campaignId: String): Int

    @Query("SELECT * FROM campaign_analytics WHERE campaignId = :campaignId AND messageType = :type")
    suspend fun getAnalyticsByType(campaignId: String, type: String): List<CampaignAnalytics>
}
