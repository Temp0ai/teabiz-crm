package com.teabiz.crm.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.GeminiService
import com.teabiz.crm.data.remote.WhatsAppCatalogFetcher
import com.teabiz.crm.data.remote.WhatsAppService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsAppCatalogViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whatsappService: WhatsAppService,
    private val catalogFetcher: WhatsAppCatalogFetcher,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _catalogProducts = MutableStateFlow<List<WhatsAppCatalogFetcher.CatalogProduct>>(emptyList())
    val catalogProducts: StateFlow<List<WhatsAppCatalogFetcher.CatalogProduct>> = _catalogProducts

    private val _generatedMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val generatedMessages: StateFlow<Map<String, String>> = _generatedMessages

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isGeneratingMessages = MutableStateFlow(false)
    val isGeneratingMessages: StateFlow<Boolean> = _isGeneratingMessages

    private val _statusMessage = MutableStateFlow("")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _editingProductIndex = MutableStateFlow<Int?>(null)
    val editingProductIndex: StateFlow<Int?> = _editingProductIndex

    private val _editedMessages = MutableStateFlow<Map<String, String>>(emptyMap())
    val editedMessages: StateFlow<Map<String, String>> = _editedMessages

    fun checkConnection() {
        viewModelScope.launch {
            _isLoading.value = true
            _isConnected.value = whatsappService.checkConnection()
            _isLoading.value = false
        }
    }

    fun fetchCatalog(phoneNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Fetching catalog from $phoneNumber..."
            try {
                val products = catalogFetcher.fetchCatalog(phoneNumber)
                _catalogProducts.value = products
                _statusMessage.value = if (products.isNotEmpty()) "Found ${products.size} products" else "No products found"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
            _isLoading.value = false
        }
    }

    fun generateMessages(tone: String = "Professional", language: String = "English") {
        viewModelScope.launch {
            _isGeneratingMessages.value = true
            _statusMessage.value = "Generating AI messages..."
            try {
                val products = _catalogProducts.value
                val messages = catalogFetcher.generateMessagesFromCatalog(products, tone, language, geminiService)
                _generatedMessages.value = messages
                _statusMessage.value = "Generated ${messages.size} messages"
            } catch (e: Exception) {
                _statusMessage.value = "Error generating messages: ${e.message}"
            }
            _isGeneratingMessages.value = false
        }
    }

    fun updateEditedMessage(productName: String, message: String) {
        _editedMessages.value = _editedMessages.value + (productName to message)
    }

    fun saveEditedMessage(productName: String) {
        val edited = _editedMessages.value[productName] ?: return
        _generatedMessages.value = _generatedMessages.value + (productName to edited)
        _editingProductIndex.value = null
    }

    fun shareProductOnWhatsApp(phone: String, productName: String, message: String) {
        viewModelScope.launch {
            try {
                val finalMessage = _editedMessages.value[productName] ?: message
                val url = "https://wa.me/$phone?text=${Uri.encode(finalMessage)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun shareAllProducts(phone: String) {
        viewModelScope.launch {
            try {
                val allMessages = _generatedMessages.value.values.joinToString("\n\n---\n\n")
                val url = "https://wa.me/$phone?text=${Uri.encode(allMessages)}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            }
        }
    }
}
