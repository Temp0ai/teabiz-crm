package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.repository.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CampaignsViewModel @Inject constructor(
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

    fun createCampaign(name: String, template: String, category: String) {
        viewModelScope.launch {
            val campaign = Campaign(
                name = name,
                messageTemplate = template,
                targetCategory = category,
                status = "DRAFT"
            )
            leadRepository.insertCampaign(campaign)
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

                var sentCount = 0
                var failedCount = 0

                whatsappService.sendBulkMessages(messages) { sent, total ->
                    _sendProgress.value = Pair(sent, total)
                }.forEach { result ->
                    if (result.status == "SENT") sentCount++ else failedCount++
                }

                leadRepository.updateCampaign(campaign.copy(
                    status = "COMPLETED",
                    sentCount = sentCount,
                    failedCount = failedCount,
                    totalRecipients = filteredLeads.size,
                    completedAt = System.currentTimeMillis()
                ))

                _campaignState.value = CampaignState.Completed(sentCount, failedCount)
            } catch (e: Exception) {
                _campaignState.value = CampaignState.Error(e.message ?: "Campaign failed")
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
    }

    sealed class CampaignState {
        data object Idle : CampaignState()
        data object Sending : CampaignState()
        data class Completed(val sent: Int, val failed: Int) : CampaignState()
        data class Error(val message: String) : CampaignState()
    }
}
