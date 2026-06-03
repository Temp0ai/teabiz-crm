package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.data.model.LeadActivity
import kotlinx.coroutines.flow.Flow

@Dao
interface LeadDao {
    @Query("SELECT * FROM leads ORDER BY createdAt DESC")
    fun getAllLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE status = :status ORDER BY createdAt DESC")
    fun getLeadsByStatus(status: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE id = :leadId")
    suspend fun getLeadById(leadId: String): Lead?

    @Query("SELECT * FROM leads WHERE email = :email LIMIT 1")
    suspend fun getLeadByEmail(email: String): Lead?

    @Query("SELECT * FROM leads WHERE phone = :phone LIMIT 1")
    suspend fun getLeadByPhone(phone: String): Lead?

    @Query("SELECT * FROM leads WHERE name LIKE '%' || :query || '%' OR email LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    fun searchLeads(query: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE productInterest LIKE '%' || :category || '%'")
    fun getLeadsByCategory(category: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE clientType = :clientType ORDER BY createdAt DESC")
    fun getLeadsByClientType(clientType: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE city = :city ORDER BY createdAt DESC")
    fun getLeadsByCity(city: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE source = :source ORDER BY createdAt DESC")
    fun getLeadsBySource(source: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE whatsappOptIn = 1 AND status != 'LOST'")
    fun getWhatsAppOptInLeads(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE priority = :priority ORDER BY leadScore DESC")
    fun getLeadsByPriority(priority: String): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE nextFollowUpAt IS NOT NULL AND nextFollowUpAt <= :currentTime ORDER BY nextFollowUpAt ASC")
    fun getLeadsDueForFollowUp(currentTime: Long): Flow<List<Lead>>

    @Query("SELECT * FROM leads ORDER BY leadScore DESC")
    fun getLeadsByScore(): Flow<List<Lead>>

    @Query("SELECT * FROM leads WHERE source = :source")
    suspend fun getLeadsBySourceList(source: String): List<Lead>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLead(lead: Lead): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeads(leads: List<Lead>): List<Long>

    @Update
    suspend fun updateLead(lead: Lead)

    @Delete
    suspend fun deleteLead(lead: Lead)

    @Query("DELETE FROM leads WHERE id = :leadId")
    suspend fun deleteLeadById(leadId: String)

    @Query("SELECT COUNT(*) FROM leads")
    fun getLeadCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE status = :status")
    fun getLeadCountByStatus(status: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM leads WHERE productInterest LIKE '%' || :category || '%'")
    fun getLeadCountByCategory(category: String): Flow<Int>

    @Query("SELECT * FROM leads ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentLeads(limit: Int): Flow<List<Lead>>
}

@Dao
interface LeadActivityDao {
    @Query("SELECT * FROM lead_activities WHERE leadId = :leadId ORDER BY timestamp DESC")
    fun getActivitiesForLead(leadId: String): Flow<List<LeadActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: LeadActivity)

    @Delete
    suspend fun deleteActivity(activity: LeadActivity)

    @Query("DELETE FROM lead_activities WHERE leadId = :leadId")
    suspend fun deleteAllActivitiesForLead(leadId: String)
}
