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
    private val leadActivityDao = database.leadActivityDao()
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
    fun getLeadsByPriority(priority: String): Flow<List<Lead>> = leadDao.getLeadsByPriority(priority)
    fun getLeadsDueForFollowUp(currentTime: Long): Flow<List<Lead>> = leadDao.getLeadsDueForFollowUp(currentTime)
    fun getLeadsByScore(): Flow<List<Lead>> = leadDao.getLeadsByScore()
    suspend fun getLeadsBySourceList(source: String): List<Lead> = leadDao.getLeadsBySourceList(source)
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

    // Activity operations
    fun getActivitiesForLead(leadId: String): Flow<List<LeadActivity>> = leadActivityDao.getActivitiesForLead(leadId)
    suspend fun insertActivity(activity: LeadActivity) = leadActivityDao.insertActivity(activity)
    suspend fun deleteActivity(activity: LeadActivity) = leadActivityDao.deleteActivity(activity)
    suspend fun deleteAllActivitiesForLead(leadId: String) = leadActivityDao.deleteAllActivitiesForLead(leadId)

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

    // Lead Scoring
    suspend fun calculateLeadScore(lead: Lead): Int {
        var score = 0

        // Source scoring
        score += when (lead.source) {
            "INDIAMART" -> 30
            "JUSTDIAL" -> 25
            "ORDER" -> 40
            "DEALER" -> 20
            "DISTRIBUTOR" -> 20
            "MACHINE" -> 35
            "JSTDL" -> 15
            "KAGGLE" -> 10
            "GMB" -> 20
            "REFERRAL" -> 25
            "WEBSITE" -> 15
            "WHATSAPP" -> 20
            else -> 10
        }

        // Status scoring
        score += when (lead.status) {
            "NEGOTIATION" -> 30
            "FOLLOW_UP" -> 20
            "CONTACTED" -> 15
            "NEW" -> 10
            "CONVERTED" -> 50
            "LOST" -> 0
            else -> 5
        }

        // Recency scoring (more recent = higher)
        val daysSinceCreation = (System.currentTimeMillis() - lead.createdAt) / (1000 * 60 * 60 * 24)
        score += when {
            daysSinceCreation <= 1 -> 20
            daysSinceCreation <= 3 -> 15
            daysSinceCreation <= 7 -> 10
            daysSinceCreation <= 30 -> 5
            else -> 0
        }

        // Follow-up recency
        lead.lastFollowUpAt?.let {
            val daysSinceFollowUp = (System.currentTimeMillis() - it) / (1000 * 60 * 60 * 24)
            score += when {
                daysSinceFollowUp <= 1 -> 15
                daysSinceFollowUp <= 3 -> 10
                daysSinceFollowUp <= 7 -> 5
                else -> 0
            }
        }

        // Has phone number bonus
        if (lead.phone.isNotBlank()) score += 5

        // Has email bonus
        if (lead.email.isNotBlank()) score += 5

        // Has company bonus
        if (lead.company.isNotBlank()) score += 5

        // Product interest bonus
        if (lead.productInterest.isNotEmpty()) score += 5

        // Deal value bonus
        if (lead.dealValue > 0) score += 10

        return score.coerceIn(0, 100)
    }

    fun getPriorityFromScore(score: Int): String {
        return when {
            score >= 70 -> "HOT"
            score >= 45 -> "WARM"
            score >= 20 -> "NORMAL"
            else -> "COLD"
        }
    }

    suspend fun updateLeadScore(leadId: String) {
        val lead = getLeadById(leadId) ?: return
        val score = calculateLeadScore(lead)
        val priority = getPriorityFromScore(score)
        updateLead(lead.copy(leadScore = score, priority = priority))
    }

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

                val score = calculateLeadScore(lead)
                val priority = getPriorityFromScore(score)
                insertLead(lead.copy(leadScore = score, priority = priority))
                imported++
            } catch (e: Exception) {
                failed++
            }
        }

        return ImportResult(imported, duplicates, failed)
    }
}
