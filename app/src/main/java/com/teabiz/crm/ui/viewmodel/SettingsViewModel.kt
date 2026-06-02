package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.WhatsAppService
import com.teabiz.crm.data.repository.LeadRepository
import com.teabiz.crm.data.repository.MarketingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val marketingRepository: MarketingRepository,
    private val aiService: AiService,
    private val whatsappService: WhatsAppService
) : ViewModel() {

    val apiKey: StateFlow<String> = marketingRepository.getSettingFlow("openai_api_key")
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val aiModel: StateFlow<String> = marketingRepository.getSettingFlow("ai_model")
        .map { it ?: "gpt-4" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "gpt-4")

    val messageTone: StateFlow<String> = marketingRepository.getSettingFlow("message_tone")
        .map { it ?: "Professional" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Professional")

    val whatsappApiUrl: StateFlow<String> = marketingRepository.getSettingFlow("whatsapp_api_url")
        .map { it ?: "" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val businessName: StateFlow<String> = marketingRepository.getSettingFlow("business_name")
        .map { it ?: "TeaBiz" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "TeaBiz")

    fun saveApiKey(key: String) {
        viewModelScope.launch {
            marketingRepository.setSetting("openai_api_key", key)
            aiService.configure(key, aiModel.value)
        }
    }

    fun saveAiModel(model: String) {
        viewModelScope.launch {
            marketingRepository.setSetting("ai_model", model)
            aiService.configure(apiKey.value, model)
        }
    }

    fun saveMessageTone(tone: String) {
        viewModelScope.launch {
            marketingRepository.setSetting("message_tone", tone)
        }
    }

    fun saveWhatsAppApiUrl(url: String) {
        viewModelScope.launch {
            marketingRepository.setSetting("whatsapp_api_url", url)
            whatsappService.setApiUrl(url)
        }
    }

    fun saveBusinessName(name: String) {
        viewModelScope.launch {
            marketingRepository.setSetting("business_name", name)
        }
    }

    fun saveAll(
        apiKey: String,
        aiModel: String,
        messageTone: String,
        whatsappApiUrl: String,
        businessName: String
    ) {
        viewModelScope.launch {
            marketingRepository.setSetting("openai_api_key", apiKey)
            marketingRepository.setSetting("ai_model", aiModel)
            marketingRepository.setSetting("message_tone", messageTone)
            marketingRepository.setSetting("whatsapp_api_url", whatsappApiUrl)
            marketingRepository.setSetting("business_name", businessName)

            aiService.configure(apiKey, aiModel)
            whatsappService.setApiUrl(whatsappApiUrl)
        }
    }
}
