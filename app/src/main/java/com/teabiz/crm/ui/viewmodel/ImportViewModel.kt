package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.GmailService
import com.teabiz.crm.data.repository.LeadRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImportViewModel @Inject constructor(
    private val leadRepository: LeadRepository,
    private val gmailService: GmailService,
    private val aiService: AiService
) : ViewModel() {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    private val _importSessions = leadRepository.getAllImportSessions()
    val importSessions: StateFlow<List<ImportSession>> = _importSessions

    val gmailAuthState: StateFlow<GmailAuthState> = gmailService.authState

    fun importLeads(leads: List<Lead>) {
        viewModelScope.launch {
            _importState.value = ImportState.Processing
            try {
                val result = leadRepository.importLeads(leads)
                val session = ImportSession(
                    fileName = "Excel Import",
                    fileType = "EXCEL",
                    totalRows = leads.size,
                    importedRows = result.imported,
                    duplicateRows = result.duplicates,
                    failedRows = result.failed,
                    status = "COMPLETED"
                )
                leadRepository.insertImportSession(session)
                _importState.value = ImportState.Completed(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Import failed")
            }
        }
    }

    fun fetchGmailLeads(authCode: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Processing
            try {
                gmailService.authenticate(authCode)
                val emails = gmailService.fetchEmails()
                val leads = emails.mapNotNull { email -> emailToLead(email) }
                val result = leadRepository.importLeads(leads)

                val session = ImportSession(
                    fileName = "Gmail Import",
                    fileType = "GMAIL",
                    totalRows = emails.size,
                    importedRows = result.imported,
                    duplicateRows = result.duplicates,
                    failedRows = result.failed,
                    status = "COMPLETED"
                )
                leadRepository.insertImportSession(session)
                _importState.value = ImportState.Completed(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Gmail import failed")
            }
        }
    }

    fun searchGmailLeads(query: String) {
        viewModelScope.launch {
            _importState.value = ImportState.Processing
            try {
                val emails = gmailService.searchEmails(query)
                val leads = emails.mapNotNull { email -> emailToLead(email) }
                val result = leadRepository.importLeads(leads)

                val session = ImportSession(
                    fileName = "Gmail Search: $query",
                    fileType = "GMAIL",
                    totalRows = emails.size,
                    importedRows = result.imported,
                    duplicateRows = result.duplicates,
                    failedRows = result.failed,
                    status = "COMPLETED"
                )
                leadRepository.insertImportSession(session)
                _importState.value = ImportState.Completed(result)
            } catch (e: Exception) {
                _importState.value = ImportState.Error(e.message ?: "Gmail search failed")
            }
        }
    }

    private fun emailToLead(email: EmailMessage): Lead? {
        val name = email.fromName.ifBlank { email.from.substringBefore("@") }
        if (name.isBlank() && email.from.isBlank()) return null

        val phone = extractPhone(email.body)
        val products = extractProducts(email.subject + " " + email.body)

        return Lead(
            name = name,
            email = email.from,
            phone = phone,
            productInterest = products,
            message = email.subject + "\n\n" + email.snippet,
            source = "GMAIL",
            status = "NEW"
        )
    }

    private fun extractPhone(text: String): String {
        val phoneRegex = Regex("(?:\\+91|0)?[6-9]\\d{9}")
        return phoneRegex.find(text)?.value ?: ""
    }

    private fun extractProducts(text: String): List<String> {
        val products = mutableListOf<String>()
        val lowerText = text.lowercase()

        val productKeywords = mapOf(
            "Tea Premix" to listOf("tea premix", "chai premix", "tea powder premix"),
            "Coffee Premix" to listOf("coffee premix", "instant coffee premix"),
            "Nescafe Premix" to listOf("nescafe premix", "nescafe"),
            "Tea Machine" to listOf("tea machine", "chai machine", "tea maker"),
            "Coffee Machine" to listOf("coffee machine", "coffee maker", "espresso machine"),
            "Nescafe Machine" to listOf("nescafe machine", "nescafe maker")
        )

        productKeywords.forEach { (category, keywords) ->
            if (keywords.any { lowerText.contains(it) }) {
                products.add(category)
            }
        }

        if (products.isEmpty()) products.add("Other")
        return products
    }

    fun resetState() {
        _importState.value = ImportState.Idle
    }

    sealed class ImportState {
        data object Idle : ImportState()
        data object Processing : ImportState()
        data class Completed(val result: ImportResult) : ImportState()
        data class Error(val message: String) : ImportState()
    }
}
