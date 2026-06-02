package com.teabiz.crm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
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
    application: Application,
    private val leadRepository: LeadRepository,
    private val gmailService: GmailService,
    private val aiService: AiService
) : AndroidViewModel(application) {

    private val _importState = MutableStateFlow<ImportState>(ImportState.Idle)
    val importState: StateFlow<ImportState> = _importState

    private val _importSessions = leadRepository.getAllImportSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val importSessions: StateFlow<List<ImportSession>> = _importSessions

    fun importLeads(leads: List<Lead>) {
        viewModelScope.launch {
            _importState.value = ImportState.Processing
            try {
                val result = leadRepository.importLeads(leads)
                val session = ImportSession(
                    fileName = "Manual Import",
                    fileType = "MANUAL",
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
