package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.Lead
import com.teabiz.crm.data.repository.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val leadRepository: LeadRepository
) : ViewModel() {

    val totalLeads: StateFlow<Int> = leadRepository.getLeadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val newLeadCount: StateFlow<Int> = leadRepository.getLeadCountByStatus("NEW")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val followUpCount: StateFlow<Int> = leadRepository.getLeadCountByStatus("FOLLOW_UP")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val convertedCount: StateFlow<Int> = leadRepository.getLeadCountByStatus("CONVERTED")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val recentLeads: StateFlow<List<Lead>> = leadRepository.getRecentLeads(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val campaignCount: StateFlow<Int> = leadRepository.getCampaignCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
