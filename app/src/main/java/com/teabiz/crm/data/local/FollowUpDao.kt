package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.FollowUp
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {
    @Query("SELECT * FROM follow_ups ORDER BY createdAt DESC")
    fun getAllFollowUps(): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE leadId = :leadId ORDER BY createdAt DESC")
    fun getFollowUpsByLead(leadId: String): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE status = 'PENDING' AND (scheduledAt <= :currentTime OR scheduledAt IS NULL)")
    fun getPendingFollowUps(currentTime: Long): Flow<List<FollowUp>>

    @Query("SELECT * FROM follow_ups WHERE status = 'PENDING' AND (scheduledAt <= :currentTime OR scheduledAt IS NULL)")
    suspend fun getPendingFollowUpsOnce(currentTime: Long): List<FollowUp>

    @Query("SELECT * FROM follow_ups WHERE leadId = :leadId AND status != 'FAILED' ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLastSuccessfulFollowUp(leadId: String): FollowUp?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUp): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUps(followUps: List<FollowUp>): List<Long>

    @Update
    suspend fun updateFollowUp(followUp: FollowUp)

    @Delete
    suspend fun deleteFollowUp(followUp: FollowUp)

    @Query("SELECT COUNT(*) FROM follow_ups WHERE leadId = :leadId")
    fun getFollowUpCount(leadId: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM follow_ups WHERE status = :status")
    fun getFollowUpCountByStatus(status: String): Flow<Int>
}
