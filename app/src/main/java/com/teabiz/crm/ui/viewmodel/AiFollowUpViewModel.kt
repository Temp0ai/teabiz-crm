package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.repository.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiFollowUpViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val aiService: AiService
) : ViewModel() {

    private val _generatedMessage = MutableStateFlow("")
    val generatedMessage: StateFlow<String> = _generatedMessage

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _selectedTone = MutableStateFlow("Professional")
    val selectedTone: StateFlow<String> = _selectedTone

    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage: StateFlow<String> = _selectedLanguage

    private val _selectedMessageType = MutableStateFlow("initial_inquiry")
    val selectedMessageType: StateFlow<String> = _selectedMessageType

    private val _currentLead = MutableStateFlow<Lead?>(null)
    val currentLead: StateFlow<Lead?> = _currentLead

    fun loadLead(leadId: String) {
        viewModelScope.launch {
            _currentLead.value = leadRepository.getLeadById(leadId)
        }
    }

    fun generateMessage(lead: Lead) {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val response = aiService.generateFollowUpMessage(
                    lead = lead,
                    tone = _selectedTone.value,
                    language = _selectedLanguage.value,
                    messageType = _selectedMessageType.value
                )
                _generatedMessage.value = response.content
            } catch (e: Exception) {
                _generatedMessage.value = "Error generating message: ${e.message}"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    fun saveFollowUp(leadId: String, message: String, channel: String) {
        viewModelScope.launch {
            val followUp = FollowUp(
                leadId = leadId,
                message = message,
                channel = channel,
                isAiGenerated = true,
                aiModel = "ai"
            )
            leadRepository.insertFollowUp(followUp)
            leadRepository.getLeadById(leadId)?.let { lead ->
                leadRepository.updateLead(lead.copy(lastFollowUpAt = System.currentTimeMillis()))
            }
        }
    }

    fun updateTone(tone: String) {
        _selectedTone.value = tone
    }

    fun updateLanguage(language: String) {
        _selectedLanguage.value = language
    }

    fun updateMessageType(type: String) {
        _selectedMessageType.value = type
    }
}
