package com.teabiz.crm.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.repository.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CampaignsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val leadRepository: LeadRepository,
    private val whatsappService: WhatsAppService,
    private val aiService: AiService
) : ViewModel() {

    val campaigns: StateFlow<List<Campaign>> = leadRepository.getAllCampaigns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _campaignState = MutableStateFlow<CampaignState>(CampaignState.Idle)
    val campaignState: StateFlow<CampaignState> = _campaignState

    private val _sendProgress = MutableStateFlow(Pair(0, 0))
    val sendProgress: StateFlow<Pair<Int, Int>> = _sendProgress

    private val _sendStatus = MutableStateFlow("")
    val sendStatus: StateFlow<String> = _sendStatus

    private val _selectedMediaUri = MutableStateFlow<Uri?>(null)
    val selectedMediaUri: StateFlow<Uri?> = _selectedMediaUri

    private val _selectedMediaType = MutableStateFlow("")
    val selectedMediaType: StateFlow<String> = _selectedMediaType

    private val _isGeneratingAiText = MutableStateFlow(false)
    val isGeneratingAiText: StateFlow<Boolean> = _isGeneratingAiText

    fun createCampaign(
        name: String,
        template: String,
        category: String,
        batchSize: Int = 100,
        mediaUri: String = "",
        mediaType: String = ""
    ) {
        viewModelScope.launch {
            val campaign = Campaign(
                name = name,
                messageTemplate = template,
                targetCategory = category,
                batchSize = batchSize,
                mediaUri = mediaUri,
                mediaType = mediaType,
                status = "DRAFT"
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
            try {
                val campaign = leadRepository.getCampaignById(campaignId) ?: return@launch
                val leads = leadRepository.getWhatsAppOptInLeads().first()

                val filteredLeads = if (campaign.targetCategory.isNotBlank()) {
                    leads.filter { lead ->
                        lead.productInterest.any { it.contains(campaign.targetCategory, ignoreCase = true) }
                    }
                } else {
                    leads
                }

                val messages = filteredLeads.map { lead ->
                    val personalizedMessage = campaign.messageTemplate
                        .replace("{name}", lead.name)
                        .replace("{company}", lead.company)
                        .replace("{product}", lead.productInterest.joinToString(", "))
                    lead.phone to personalizedMessage
                }

                val batchSize = campaign.batchSize.coerceAtLeast(50)
                val totalBatches = (messages.size + batchSize - 1) / batchSize
                val allResults = mutableListOf<WhatsAppMessage>()

                for (batchIndex in 0 until totalBatches) {
                    val start = batchIndex * batchSize
                    val end = minOf(start + batchSize, messages.size)
                    val batchMessages = messages.subList(start, end)

                    _sendStatus.value = "Batch ${batchIndex + 1}/$totalBatches - Sending to ${batchMessages.size} contacts..."

                    leadRepository.updateCampaign(campaign.copy(
                        status = "RUNNING",
                        currentBatch = batchIndex + 1,
                        totalBatches = totalBatches,
                        totalRecipients = filteredLeads.size
                    ))

                    val results = whatsappService.sendBulkMessages(
                        messages = batchMessages,
                        onProgress = { sent, total ->
                            val overallSent = (batchIndex * batchSize) + sent
                            _sendProgress.value = Pair(overallSent, messages.size)
                            _sendStatus.value = "Batch ${batchIndex + 1}/$totalBatches - ${sent}/${total} in this batch (${overallSent}/${messages.size} total)"
                        },
                        onStatus = { status ->
                            _sendStatus.value = status
                        }
                    )
                    allResults.addAll(results)

                    if (batchIndex < totalBatches - 1) {
                        _sendStatus.value = "Batch ${batchIndex + 1} complete. Waiting 30s before next batch..."
                        kotlinx.coroutines.delay(30_000L)
                    }
                }

                val sentCount = allResults.count { it.status == "SENT" }
                val failedCount = allResults.count { it.status == "FAILED" }

                leadRepository.updateCampaign(campaign.copy(
                    status = "COMPLETED",
                    sentCount = sentCount,
                    failedCount = failedCount,
                    totalRecipients = filteredLeads.size,
                    completedAt = System.currentTimeMillis(),
                    currentBatch = totalBatches,
                    currentBatchProgress = "Complete"
                ))

                _campaignState.value = CampaignState.Completed(sentCount, failedCount)
            } catch (e: Exception) {
                _campaignState.value = CampaignState.Error(e.message ?: "Campaign failed")
            }
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
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    val cleanedPhone = phone.replace(Regex("[^0-9]"), "")
                    data = Uri.parse("https://wa.me/$cleanedPhone?text=${Uri.encode(caption)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
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
    }

    sealed class CampaignState {
        data object Idle : CampaignState()
        data object Sending : CampaignState()
        data class Completed(val sent: Int, val failed: Int) : CampaignState()
        data class Error(val message: String) : CampaignState()
    }
}
