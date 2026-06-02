package com.teabiz.crm.data.repository

import com.teabiz.crm.data.local.AppDatabase
import com.teabiz.crm.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeadRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val leadDao = database.leadDao()
    private val followUpDao = database.followUpDao()
    private val importSessionDao = database.importSessionDao()
    private val campaignDao = database.campaignDao()

    // Lead operations
    fun getAllLeads(): Flow<List<Lead>> = leadDao.getAllLeads()
    fun getLeadsByStatus(status: String): Flow<List<Lead>> = leadDao.getLeadsByStatus(status)
    fun searchLeads(query: String): Flow<List<Lead>> = leadDao.searchLeads(query)
    fun getLeadsByCategory(category: String): Flow<List<Lead>> = leadDao.getLeadsByCategory(category)
    fun getLeadsByClientType(clientType: String): Flow<List<Lead>> = leadDao.getLeadsByClientType(clientType)
    fun getLeadsBySource(source: String): Flow<List<Lead>> = leadDao.getLeadsBySource(source)
    fun getWhatsAppOptInLeads(): Flow<List<Lead>> = leadDao.getWhatsAppOptInLeads()
    fun getRecentLeads(limit: Int = 5): Flow<List<Lead>> = leadDao.getRecentLeads(limit)
    suspend fun getLeadById(leadId: String): Lead? = leadDao.getLeadById(leadId)
    suspend fun getLeadByEmail(email: String): Lead? = leadDao.getLeadByEmail(email)
    suspend fun getLeadByPhone(phone: String): Lead? = leadDao.getLeadByPhone(phone)
    suspend fun insertLead(lead: Lead): Long = leadDao.insertLead(lead)
    suspend fun insertLeads(leads: List<Lead>): List<Long> = leadDao.insertLeads(leads)
    suspend fun updateLead(lead: Lead) = leadDao.updateLead(lead)
    suspend fun deleteLead(lead: Lead) = leadDao.deleteLead(lead)
    suspend fun deleteLeadById(leadId: String) = leadDao.deleteLeadById(leadId)
    fun getLeadCount(): Flow<Int> = leadDao.getLeadCount()
    fun getLeadCountByStatus(status: String): Flow<Int> = leadDao.getLeadCountByStatus(status)
    fun getLeadCountByCategory(category: String): Flow<Int> = leadDao.getLeadCountByCategory(category)

    // FollowUp operations
    fun getAllFollowUps(): Flow<List<FollowUp>> = followUpDao.getAllFollowUps()
    fun getFollowUpsByLead(leadId: String): Flow<List<FollowUp>> = followUpDao.getFollowUpsByLead(leadId)
    fun getPendingFollowUps(currentTime: Long): Flow<List<FollowUp>> = followUpDao.getPendingFollowUps(currentTime)
    suspend fun getPendingFollowUpsOnce(currentTime: Long): List<FollowUp> = followUpDao.getPendingFollowUpsOnce(currentTime)
    suspend fun getLastSuccessfulFollowUp(leadId: String): FollowUp? = followUpDao.getLastSuccessfulFollowUp(leadId)
    suspend fun insertFollowUp(followUp: FollowUp): Long = followUpDao.insertFollowUp(followUp)
    suspend fun insertFollowUps(followUps: List<FollowUp>): List<Long> = followUpDao.insertFollowUps(followUps)
    suspend fun updateFollowUp(followUp: FollowUp) = followUpDao.updateFollowUp(followUp)
    suspend fun deleteFollowUp(followUp: FollowUp) = followUpDao.deleteFollowUp(followUp)
    fun getFollowUpCount(leadId: String): Flow<Int> = followUpDao.getFollowUpCount(leadId)
    fun getFollowUpCountByStatus(status: String): Flow<Int> = followUpDao.getFollowUpCountByStatus(status)

    // Import session operations
    fun getAllImportSessions(): Flow<List<ImportSession>> = importSessionDao.getAllImportSessions()
    suspend fun getImportSessionById(sessionId: String): ImportSession? = importSessionDao.getImportSessionById(sessionId)
    suspend fun insertImportSession(session: ImportSession): Long = importSessionDao.insertImportSession(session)
    suspend fun updateImportSession(session: ImportSession) = importSessionDao.updateImportSession(session)
    suspend fun deleteImportSession(sessionId: String) = importSessionDao.deleteImportSession(sessionId)

    // Campaign operations
    fun getAllCampaigns(): Flow<List<Campaign>> = campaignDao.getAllCampaigns()
    fun getCampaignsByStatus(status: String): Flow<List<Campaign>> = campaignDao.getCampaignsByStatus(status)
    suspend fun getCampaignById(campaignId: String): Campaign? = campaignDao.getCampaignById(campaignId)
    suspend fun insertCampaign(campaign: Campaign): Long = campaignDao.insertCampaign(campaign)
    suspend fun updateCampaign(campaign: Campaign) = campaignDao.updateCampaign(campaign)
    suspend fun deleteCampaign(campaign: Campaign) = campaignDao.deleteCampaign(campaign)
    fun getCampaignCount(): Flow<Int> = campaignDao.getCampaignCount()

    // Batch import with duplicate check
    suspend fun importLeads(leads: List<Lead>): ImportResult {
        var imported = 0
        var duplicates = 0
        var failed = 0

        for (lead in leads) {
            try {
                val existingEmail = lead.email.takeIf { it.isNotBlank() }?.let { getLeadByEmail(it) }
                val existingPhone = lead.phone.takeIf { it.isNotBlank() }?.let { getLeadByPhone(it) }

                if (existingEmail != null || existingPhone != null) {
                    duplicates++
                    continue
                }

                insertLead(lead)
                imported++
            } catch (e: Exception) {
                failed++
            }
        }

        return ImportResult(imported, duplicates, failed)
    }
}
