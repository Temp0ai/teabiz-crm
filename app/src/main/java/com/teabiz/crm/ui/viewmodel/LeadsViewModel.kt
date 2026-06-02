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
class LeadsViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val aiService: AiService
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter

    private val _selectedCategory = MutableStateFlow("")
    val selectedCategory: StateFlow<String> = _selectedCategory

    val leads: StateFlow<List<Lead>> = combine(
        _searchQuery,
        _selectedFilter,
        _selectedCategory
    ) { query, filter, category ->
        Triple(query, filter, category)
    }.flatMapLatest { (query, filter, category) ->
        when {
            query.isNotBlank() -> leadRepository.searchLeads(query)
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

    fun addLead(lead: Lead) {
        viewModelScope.launch {
            leadRepository.insertLead(lead)
        }
    }

    fun updateLead(lead: Lead) {
        viewModelScope.launch {
            leadRepository.updateLead(lead)
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
                leadRepository.updateLead(lead.copy(status = status, updatedAt = System.currentTimeMillis()))
            }
        }
    }
}
