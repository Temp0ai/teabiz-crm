package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.teabiz.crm.data.model.WhatsAppCatalogItem
import com.teabiz.crm.data.remote.WhatsAppService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.viewModelScope

@HiltViewModel
class WhatsAppCatalogViewModel @Inject constructor(
    private val whatsappService: WhatsAppService
) : ViewModel() {

    private val _catalogItems = MutableStateFlow<List<WhatsAppCatalogItem>>(emptyList())
    val catalogItems: StateFlow<List<WhatsAppCatalogItem>> = _catalogItems

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun checkConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            _isConnected.value = whatsappService.checkConnection()
            if (_isConnected.value) {
                _catalogItems.value = whatsappService.getCatalog()
            }
            _isLoading.value = false
        }
    }
}
