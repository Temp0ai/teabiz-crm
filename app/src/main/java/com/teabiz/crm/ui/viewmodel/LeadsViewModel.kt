package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.repository.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LeadsViewModel @Inject constructor(
    private val leadRepository: LeadRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _selectedPriority = MutableStateFlow("")
    val selectedPriority: StateFlow<String> = _selectedPriority

    val leads: StateFlow<List<Lead>> = combine(
        _searchQuery,
        _selectedFilter,
        _selectedCategory,
        _selectedPriority
    ) { query, filter, category, priority ->
        Quadruple(query, filter, category, priority)
    }.flatMapLatest { (query, filter, category, priority) ->
        when {
            query.isNotBlank() -> leadRepository.searchLeads(query)
            priority.isNotBlank() -> leadRepository.getLeadsByPriority(priority)
            else -> leadRepository.getAllLeads()
        }
    }.map { leads ->
        val filter = _selectedFilter.value
        val category = _selectedCategory.value
        leads.filter { lead ->
            val matchesStatus = filter == "ALL" || lead.status == filter
            val matchesCategory = category.isBlank() || lead.productInterest.any { it.contains(category, ignoreCase = true) }
            matchesStatus && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun updateCategory(category: String) {
        _selectedCategory.value = if (_selectedCategory.value == category) "" else category
    }

    fun updatePriority(priority: String) {
        _selectedPriority.value = if (_selectedPriority.value == priority) "" else priority
    }

    fun addLead(lead: Lead) {
        viewModelScope.launch {
            val score = leadRepository.calculateLeadScore(lead)
            val priority = leadRepository.getPriorityFromScore(score)
            leadRepository.insertLead(lead.copy(leadScore = score, priority = priority))
        }
    }

    fun updateLead(lead: Lead) {
        viewModelScope.launch {
            val score = leadRepository.calculateLeadScore(lead)
            val priority = leadRepository.getPriorityFromScore(score)
            leadRepository.updateLead(lead.copy(leadScore = score, priority = priority, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteLead(lead: Lead) {
        viewModelScope.launch {
            leadRepository.deleteLead(lead)
        }
    }

    fun getLeadById(leadId: String, onResult: (Lead?) -> Unit) {
        viewModelScope.launch {
            val lead = leadRepository.getLeadById(leadId)
            onResult(lead)
        }
    }

    fun updateLeadStatus(leadId: String, status: String) {
        viewModelScope.launch {
            leadRepository.getLeadById(leadId)?.let { lead ->
                val score = leadRepository.calculateLeadScore(lead.copy(status = status))
                val priority = leadRepository.getPriorityFromScore(score)
                leadRepository.updateLead(lead.copy(status = status, leadScore = score, priority = priority, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    // Activity Timeline
    fun getActivitiesForLead(leadId: String): Flow<List<LeadActivity>> {
        return leadRepository.getActivitiesForLead(leadId)
    }

    fun logActivity(leadId: String, type: String, description: String, outcome: String = "") {
        viewModelScope.launch {
            val activity = LeadActivity(
                id = UUID.randomUUID().toString(),
                leadId = leadId,
                type = type,
                description = description,
                timestamp = System.currentTimeMillis(),
                outcome = outcome
            )
            leadRepository.insertActivity(activity)
        }
    }

    // Follow-up Scheduling
    fun scheduleFollowUp(leadId: String, scheduledAt: Long) {
        viewModelScope.launch {
            val followUp = FollowUp(
                leadId = leadId,
                message = "Scheduled follow-up",
                channel = "MANUAL",
                status = "PENDING",
                scheduledAt = scheduledAt
            )
            leadRepository.insertFollowUp(followUp)
        }
    }

    fun updateLeadNextFollowUp(leadId: String, nextFollowUpAt: Long) {
        viewModelScope.launch {
            leadRepository.getLeadById(leadId)?.let { lead ->
                leadRepository.updateLead(lead.copy(nextFollowUpAt = nextFollowUpAt, updatedAt = System.currentTimeMillis()))
            }
        }
    }

    // Lead Sources
    fun getLeadsBySource(source: String): Flow<List<Lead>> = leadRepository.getLeadsBySource(source)

    fun getLeadCountBySource(source: String, onResult: (Int) -> Unit) {
        viewModelScope.launch {
            val leads = leadRepository.getLeadsBySourceList(source)
            onResult(leads.size)
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
