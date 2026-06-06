package com.teabiz.crm.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.local.BlacklistDao
import com.teabiz.crm.data.local.CampaignAnalyticsDao
import com.teabiz.crm.data.local.CampaignTemplateDao
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.repository.LeadRepository
import com.teabiz.crm.data.repository.MarketingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import javax.inject.Inject

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val leadRepository: LeadRepository,
    private val whatsappService: WhatsAppService,
    private val aiService: AiService,
    private val marketingRepository: MarketingRepository,
    private val templateDao: CampaignTemplateDao,
    private val blacklistDao: BlacklistDao,
    private val analyticsDao: CampaignAnalyticsDao
) : ViewModel() {

    val campaigns: StateFlow<List<Campaign>> = leadRepository.getAllCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val templates: StateFlow<List<CampaignTemplate>> = templateDao.getAllTemplates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val blacklistedContacts: StateFlow<List<BlacklistedContact>> = blacklistDao.getAllBlacklisted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _campaignState = MutableStateFlow<CampaignState>(CampaignState.Idle)
    val campaignState: StateFlow<CampaignState> = _campaignState

    private val _sendProgress = MutableStateFlow(Pair(0, 0))
    val sendProgress: StateFlow<Pair<Int, Int>> = _sendProgress

    private val _sendStatus = MutableStateFlow("")
    val sendStatus: StateFlow<String> = _sendStatus

    private val _selectedMediaUri = MutableStateFlow<Uri?>(null)
    val selectedMediaUri: StateFlow<Uri?> = _selectedMediaUri

    private val _contactCount = MutableStateFlow(0)
    val contactCount: StateFlow<Int> = _contactCount

    private val _filteredLeads = MutableStateFlow<List<Lead>>(emptyList())
    val filteredLeads: StateFlow<List<Lead>> = _filteredLeads

    private val _selectedMediaType = MutableStateFlow("")
    val selectedMediaType: StateFlow<String> = _selectedMediaType

    private val _isGeneratingAiText = MutableStateFlow(false)
    val isGeneratingAiText: StateFlow<Boolean> = _isGeneratingAiText

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused

    private val _isStopped = MutableStateFlow(false)

    private val _sentContacts = MutableStateFlow<List<String>>(emptyList())
    val sentContacts: StateFlow<List<String>> = _sentContacts

    private val _remainingContacts = MutableStateFlow<List<String>>(emptyList())
    val remainingContacts: StateFlow<List<String>> = _remainingContacts

    private val _activeCampaignId = MutableStateFlow<String?>(null)
    val activeCampaignId: StateFlow<String?> = _activeCampaignId

    private val _campaignAnalytics = MutableStateFlow<CampaignPerformance?>(null)
    val campaignAnalytics: StateFlow<CampaignPerformance?> = _campaignAnalytics

    private val _bestTimeSuggestion = MutableStateFlow("")
    val bestTimeSuggestion: StateFlow<String> = _bestTimeSuggestion

    init {
        calculateBestTimeToSend()
        loadBusinessSettings()
    }

    private fun loadBusinessSettings() {
        viewModelScope.launch {
            val name = marketingRepository.getSetting("business_name") ?: "TeaBiz"
            val address = marketingRepository.getSetting("business_address") ?: ""
            val phone = marketingRepository.getSetting("business_phone") ?: ""
            aiService.configureBusiness(name, address, phone)
        }
    }

    private fun calculateBestTimeToSend() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        val suggestion = when {
            hour in 9..11 -> "Good morning! 9-11 AM is the best time for B2B messages. High open rates."
            hour in 14..16 -> "Afternoon slot (2-4 PM) works well for business inquiries."
            hour in 19..21 -> "Evening (7-9 PM) is great for promotional messages. People relax and check WhatsApp."
            dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY -> "Weekend! Consider scheduling for Monday morning instead."
            else -> "Current time is good for sending. Best times: 9-11 AM, 2-4 PM, 7-9 PM."
        }
        _bestTimeSuggestion.value = suggestion
    }

    fun getContactCountByCategory(category: String) {
        viewModelScope.launch {
            val leads = leadRepository.getWhatsAppOptInLeads().first()
            val blacklisted = blacklistDao.getAllBlacklisted().first().map { it.phone }
            val count = if (category.isBlank()) {
                leads.count { it.phone !in blacklisted }
            } else {
                leads.count { lead ->
                    lead.productInterest.any { it.contains(category, ignoreCase = true) } && lead.phone !in blacklisted
                }
            }
            _contactCount.value = count
            _filteredLeads.value = if (category.isBlank()) {
                leads.filter { it.phone !in blacklisted }
            } else {
                leads.filter { lead ->
                    lead.productInterest.any { it.contains(category, ignoreCase = true) } && lead.phone !in blacklisted
                }
            }
        }
    }

    fun getAllLeadsCount() {
        viewModelScope.launch {
            val leads = leadRepository.getWhatsAppOptInLeads().first()
            val blacklisted = blacklistDao.getAllBlacklisted().first().map { it.phone }
            _contactCount.value = leads.count { it.phone !in blacklisted }
            _filteredLeads.value = leads.filter { it.phone !in blacklisted }
        }
    }

    fun getFilteredLeads(
        category: String = "",
        priority: String = "",
        source: String = "",
        city: String = "",
        minScore: Int = 0
    ) {
        viewModelScope.launch {
            val leads = leadRepository.getWhatsAppOptInLeads().first()
            val blacklisted = blacklistDao.getAllBlacklisted().first().map { it.phone

            }
            var filtered = leads.filter { it.phone !in blacklisted }

            if (category.isNotBlank()) {
                filtered = filtered.filter { lead ->
                    lead.productInterest.any { it.contains(category, ignoreCase = true) }
                }
            }
            if (priority.isNotBlank()) {
                filtered = filtered.filter { lead -> lead.priority.equals(priority, ignoreCase = true) }
            }
            if (source.isNotBlank()) {
                filtered = filtered.filter { lead -> lead.source.equals(source, ignoreCase = true) }
            }
            if (city.isNotBlank()) {
                filtered = filtered.filter { it.city.contains(city, ignoreCase = true) }
            }
            if (minScore > 0) {
                filtered = filtered.filter { it.leadScore >= minScore }
            }

            _contactCount.value = filtered.size
            _filteredLeads.value = filtered
        }
    }

    fun createCampaign(
        name: String,
        template: String,
        category: String,
        batchSize: Int = 100,
        mediaUri: String = "",
        mediaType: String = "",
        scheduledAt: Long? = null,
        priority: String = "",
        source: String = "",
        city: String = "",
        followUpAfterHours: Int = 0,
        followUpMessage: String = "",
        abTestMessage: String = "",
        language: String = "English",
        tone: String = "Professional"
    ) {
        viewModelScope.launch {
            val status = if (scheduledAt != null && scheduledAt > System.currentTimeMillis()) "SCHEDULED" else "DRAFT"
            val campaign = Campaign(
                name = name,
                messageTemplate = template,
                targetCategory = category,
                batchSize = batchSize,
                mediaUri = mediaUri,
                mediaType = mediaType,
                status = status,
                scheduledAt = scheduledAt,
                targetPriority = priority,
                targetSource = source,
                targetCity = city,
                followUpAfterHours = followUpAfterHours,
                followUpMessage = followUpMessage,
                abTestMessage = abTestMessage,
                abTestEnabled = abTestMessage.isNotBlank(),
                language = language,
                tone = tone
            )
            leadRepository.insertCampaign(campaign)
        }
    }

    fun setSelectedMedia(uri: Uri?, type: String = "") {
        _selectedMediaUri.value = uri
        _selectedMediaType.value = type
    }

    fun clearSelectedMedia() {
        _selectedMediaUri.value = null
        _selectedMediaType.value = ""
    }

    fun generateAiMessage(
        campaignName: String,
        productCategory: String,
        tone: String = "Professional",
        language: String = "English",
        messageType: String = "promotional",
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isGeneratingAiText.value = true
            try {
                val dummyLead = Lead(
                    name = "Customer",
                    productInterest = if (productCategory.isNotBlank()) listOf(productCategory) else listOf("Tea Premix"),
                    company = "",
                    city = "",
                    message = ""
                )
                val response = aiService.generateFollowUpMessage(
                    lead = dummyLead,
                    tone = tone,
                    language = language,
                    messageType = messageType
                )
                onResult(response.content)
            } catch (e: Exception) {
                onResult("Generate a $tone promotional message about ${productCategory.ifBlank { "tea premix" }} in $language. Highlight quality, pricing, and bulk deals. Include a call to action.")
            } finally {
                _isGeneratingAiText.value = false
            }
        }
    }

    fun sendCampaign(campaignId: String) {
        viewModelScope.launch {
            _campaignState.value = CampaignState.Sending
            _isStopped.value = false
            _isPaused.value = false
            _activeCampaignId.value = campaignId
            _sentContacts.value = emptyList()
            _remainingContacts.value = emptyList()
            try {
                val campaign = leadRepository.getCampaignById(campaignId) ?: return@launch
                val leads = leadRepository.getWhatsAppOptInLeads().first()

                val blacklisted = blacklistDao.getAllBlacklisted().first().map { it.phone }
                var filteredLeads = if (campaign.targetCategory.isNotBlank()) {
                    leads.filter { lead ->
                        lead.productInterest.any { it.contains(campaign.targetCategory, ignoreCase = true) }
                    }
                } else {
                    leads
                }.filter { it.phone.isNotBlank() && it.phone !in blacklisted }

                if (campaign.targetPriority.isNotBlank()) {
                    filteredLeads = filteredLeads.filter { lead ->
                        lead.priority.equals(campaign.targetPriority, ignoreCase = true)
                    }
                }
                if (campaign.targetSource.isNotBlank()) {
                    filteredLeads = filteredLeads.filter { lead ->
                        lead.source.equals(campaign.targetSource, ignoreCase = true)
                    }
                }
                if (campaign.targetCity.isNotBlank()) {
                    filteredLeads = filteredLeads.filter {
                        it.city.contains(campaign.targetCity, ignoreCase = true)
                    }
                }

                if (filteredLeads.isEmpty()) {
                    _campaignState.value = CampaignState.Error("No contacts found with phone numbers")
                    return@launch
                }

                val total = filteredLeads.size
                _sendProgress.value = Pair(0, total)
                _sendStatus.value = "Preparing to send to $total contacts..."
                _remainingContacts.value = filteredLeads.map { "${it.name} (${it.phone})" }

                leadRepository.updateCampaign(campaign.copy(
                    status = "RUNNING",
                    totalRecipients = total,
                    totalBatches = 1,
                    remainingContacts = filteredLeads.joinToString("\n") { "${it.name} - ${it.phone}" }
                ))

                var sentCount = 0
                var failedCount = 0
                val sentList = mutableListOf<String>()
                val remainingList = filteredLeads.map { "${it.name} (${it.phone})" }.toMutableList()
                val messagesA = mutableListOf<String>()
                val messagesB = mutableListOf<String>()

                val bizName = marketingRepository.getSetting("business_name") ?: "TeaBiz"
                val bizAddress = marketingRepository.getSetting("business_address") ?: ""
                val bizPhone = marketingRepository.getSetting("business_phone") ?: ""

                var messageVariationIndex = 0

                for ((index, lead) in filteredLeads.withIndex()) {
                    while (_isPaused.value) {
                        kotlinx.coroutines.delay(500L)
                    }

                    if (_isStopped.value) {
                        leadRepository.updateCampaign(campaign.copy(
                            status = "PAUSED",
                            sentCount = sentCount,
                            failedCount = failedCount,
                            totalRecipients = total,
                            sentContacts = sentList.joinToString("\n"),
                            remainingContacts = remainingList.joinToString("\n")
                        ))
                        _campaignState.value = CampaignState.Paused(sentCount, failedCount, remainingList.size)
                        _activeCampaignId.value = null
                        return@launch
                    }

                    val isTypeA = if (campaign.abTestEnabled) index % 2 == 0 else true
                    val messageTemplate = if (isTypeA) campaign.messageTemplate else campaign.abTestMessage.ifBlank { campaign.messageTemplate }

                    val baseMessage = messageTemplate
                        .replace("{name}", lead.name)
                        .replace("{company}", lead.company)
                        .replace("{product}", lead.productInterest.joinToString(", "))
                        .replace("{city}", lead.city)
                        .replace("{address}", bizAddress)
                        .replace("{phone}", bizPhone)
                        .replace("{business}", bizName)

                    val personalizedMessage = addMessageVariation(baseMessage, messageVariationIndex++)
                    val cleanPhone = lead.phone.replace(Regex("[^0-9]"), "")
                    val contactInfo = "${lead.name} (${lead.phone})"
                    
                    // Add country code if missing (India default)
                    val finalPhone = if (cleanPhone.length == 10) {
                        "91$cleanPhone"
                    } else if (cleanPhone.length == 12 && cleanPhone.startsWith("91")) {
                        cleanPhone
                    } else if (cleanPhone.length > 12) {
                        cleanPhone.takeLast(12)
                    } else {
                        cleanPhone
                    }
                    
                    if (finalPhone.length < 12) {
                        failedCount++
                        _sendProgress.value = Pair(index + 1, total)
                        remainingList.remove(contactInfo)
                        continue
                    }

                    try {
                        val url = "https://wa.me/$finalPhone?text=${android.net.Uri.encode(personalizedMessage)}"
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        context.startActivity(intent)

                        sentCount++
                        sentList.add(contactInfo)
                        remainingList.remove(contactInfo)
                        _sentContacts.value = sentList.toList()
                        _remainingContacts.value = remainingList.toList()
                        _sendProgress.value = Pair(index + 1, total)
                        _sendStatus.value = "Opened WhatsApp for ${lead.name} (${index + 1}/$total)"

                        val analytics = CampaignAnalytics(
                            campaignId = campaignId,
                            sentAt = System.currentTimeMillis(),
                            contactPhone = lead.phone,
                            contactName = lead.name,
                            messageType = if (isTypeA) "A" else "B"
                        )
                        analyticsDao.insertAnalytics(analytics)

                        if (isTypeA) messagesA.add(personalizedMessage) else messagesB.add(personalizedMessage)

                        if (index < filteredLeads.lastIndex) {
                            val baseDelay = Random.nextLong(20_000, 50_000)

                            val breakBonus = if ((sentCount) % 10 == 0) {
                                _sendStatus.value = "Taking a short break after $sentCount messages..."
                                Random.nextLong(90_000, 180_000)
                            } else 0L

                            val activityGap = if ((sentCount) % 25 == 0) {
                                _sendStatus.value = "Activity gap pause..."
                                Random.nextLong(300_000, 600_000)
                            } else 0L

                            val totalDelay = baseDelay + breakBonus + activityGap
                            _sendStatus.value = "Waiting ${totalDelay / 1000}s before next message..."
                            kotlinx.coroutines.delay(totalDelay)
                        }
                    } catch (e: Exception) {
                        failedCount++
                        _sendProgress.value = Pair(index + 1, total)
                    }
                }

                leadRepository.updateCampaign(campaign.copy(
                    status = "COMPLETED",
                    sentCount = sentCount,
                    failedCount = failedCount,
                    totalRecipients = total,
                    completedAt = System.currentTimeMillis(),
                    currentBatch = 1,
                    currentBatchProgress = "Complete",
                    sentContacts = sentList.joinToString("\n"),
                    remainingContacts = ""
                ))

                _activeCampaignId.value = null
                _campaignState.value = CampaignState.Completed(sentCount, failedCount)
            } catch (e: Exception) {
                _campaignState.value = CampaignState.Error(e.message ?: "Campaign failed")
                _activeCampaignId.value = null
            }
        }
    }

    fun pauseCampaign() {
        _isPaused.value = true
        _sendStatus.value = "Campaign paused"
    }

    fun resumeCampaign() {
        _isPaused.value = false
        _sendStatus.value = "Campaign resumed"
    }

    fun stopCampaign() {
        _isStopped.value = true
        _isPaused.value = false
        _sendStatus.value = "Campaign stopped"
    }

    fun loadCampaignAnalytics(campaignId: String) {
        viewModelScope.launch {
            val campaign = leadRepository.getCampaignById(campaignId) ?: return@launch
            val readCount = analyticsDao.getReadCount(campaignId)
            val repliedCount = analyticsDao.getRepliedCount(campaignId)
            val convertedCount = analyticsDao.getConvertedCount(campaignId)

            val typeA = analyticsDao.getAnalyticsByType(campaignId, "A")
            val typeB = analyticsDao.getAnalyticsByType(campaignId, "B")

            _campaignAnalytics.value = CampaignPerformance(
                campaignId = campaignId,
                campaignName = campaign.name,
                totalSent = campaign.sentCount,
                readCount = readCount,
                repliedCount = repliedCount,
                convertedCount = convertedCount,
                typeASent = typeA.size,
                typeBSent = typeB.size,
                typeAReplied = typeA.count { it.repliedAt != null },
                typeBReplied = typeB.count { it.repliedAt != null }
            )
        }
    }

    fun shareMediaToWhatsApp(phone: String, mediaUri: String, caption: String) {
        viewModelScope.launch {
            try {
                val uri = Uri.parse(mediaUri)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = if (mediaUri.contains("video") || _selectedMediaType.value.contains("video")) "video/*" else "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, caption)
                    putExtra("jid", "$phone@s.whatsapp.net")
                    setPackage("com.whatsapp")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Send via WhatsApp").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (e: Exception) {
                com.teabiz.crm.util.WhatsAppUtils.openWhatsAppBusiness(context, phone, caption)
            }
        }
    }

    fun deleteCampaign(campaign: Campaign) {
        viewModelScope.launch {
            leadRepository.deleteCampaign(campaign)
        }
    }

    fun resetState() {
        _campaignState.value = CampaignState.Idle
        _sendStatus.value = ""
        _sendProgress.value = Pair(0, 0)
        _isPaused.value = false
        _isStopped.value = false
        _sentContacts.value = emptyList()
        _remainingContacts.value = emptyList()
        _activeCampaignId.value = null
    }

    private fun addMessageVariation(message: String, index: Int): String {
        val variations = listOf(
            { msg: String -> msg },
            { msg: String -> msg.replace("!", "! ").trimEnd() },
            { msg: String -> msg.replace(".", ". ").trimEnd() },
            { msg: String -> msg + "\u200B" },
            { msg: String -> msg.replace("  ", " ") },
            { msg: String -> msg + " " },
            { msg: String -> msg.replace("Hello", "Hi").replace("hello", "hi") },
            { msg: String -> msg.replace("Thank you", "Thanks").replace("thank you", "thanks") },
            { msg: String -> msg.replace("Dear", "Hi").replace("dear", "hi") },
            { msg: String -> msg.replace("best regards", "regards").replace("Best Regards", "Regards") },
        )
        return variations[index % variations.size](message)
    }

    fun saveTemplate(name: String, message: String, category: String, tone: String, language: String) {
        viewModelScope.launch {
            val template = CampaignTemplate(
                name = name,
                messageTemplate = message,
                category = category,
                tone = tone,
                language = language
            )
            templateDao.insertTemplate(template)
        }
    }

    fun deleteTemplate(template: CampaignTemplate) {
        viewModelScope.launch {
            templateDao.deleteTemplate(template)
        }
    }

    fun addBlacklisted(phone: String, name: String, reason: String) {
        viewModelScope.launch {
            val contact = BlacklistedContact(
                phone = phone,
                name = name,
                reason = reason
            )
            blacklistDao.addBlacklisted(contact)
        }
    }

    fun removeBlacklisted(contact: BlacklistedContact) {
        viewModelScope.launch {
            blacklistDao.removeBlacklisted(contact)
        }
    }

    sealed class CampaignState {
        data object Idle : CampaignState()
        data object Sending : CampaignState()
        data class Completed(val sent: Int, val failed: Int) : CampaignState()
        data class Paused(val sent: Int, val failed: Int, val remaining: Int) : CampaignState()
        data class Error(val message: String) : CampaignState()
    }

    data class CampaignPerformance(
        val campaignId: String,
        val campaignName: String,
        val totalSent: Int,
        val readCount: Int,
        val repliedCount: Int,
        val convertedCount: Int,
        val typeASent: Int = 0,
        val typeBSent: Int = 0,
        val typeAReplied: Int = 0,
        val typeBReplied: Int = 0
    )
}
