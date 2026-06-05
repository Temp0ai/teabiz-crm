package com.teabiz.crm.ui.viewmodel

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.WhatsAppCatalogOfferSender
import com.teabiz.crm.data.remote.WhatsAppCatalogOfferSender.CatalogProduct
import com.teabiz.crm.data.remote.WhatsAppCatalogOfferSender.OfferMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsAppOfferViewModel @Inject constructor(
    application: Application,
    private val offerSender: WhatsAppCatalogOfferSender
) : AndroidViewModel(application) {

    private val _catalogProducts = MutableStateFlow<List<CatalogProduct>>(emptyList())
    val catalogProducts = _catalogProducts.asStateFlow()

    private val _generatedOffer = MutableStateFlow<OfferMessage?>(null)
    val generatedOffer = _generatedOffer.asStateFlow()

    private val _bulkMessages = MutableStateFlow<List<Triple<String, String, String>>>(emptyList())
    val bulkMessages = _bulkMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage = _statusMessage.asStateFlow()

    private val _sendProgress = MutableStateFlow(Pair(0, 0))
    val sendProgress = _sendProgress.asStateFlow()

    fun fetchCatalog(businessPhone: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Fetching catalog..."
            try {
                val products = offerSender.fetchCatalog(businessPhone)
                _catalogProducts.value = products
                _statusMessage.value = "Found ${products.size} products"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateOffer(
        product: String,
        discount: String,
        catalogLink: String,
        gmbAddress: String,
        phoneNumber: String,
        language: String = "English",
        tone: String = "Professional"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating offer with AI..."
            try {
                val offer = offerSender.generateOffer(product, discount, catalogLink, gmbAddress, phoneNumber, language, tone)
                _generatedOffer.value = offer
                _statusMessage.value = "Offer generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateBulkMessages(
        contacts: List<Pair<String, String>>,
        product: String,
        discount: String,
        catalogLink: String,
        gmbAddress: String,
        phoneNumber: String,
        language: String = "English",
        personalized: Boolean = true
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating ${contacts.size} personalized messages..."
            try {
                val messages = offerSender.generateBulkMessages(contacts, product, discount, catalogLink, gmbAddress, phoneNumber, language, personalized)
                _bulkMessages.value = messages
                _statusMessage.value = "Generated ${messages.size} messages!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun openWhatsApp(phone: String, message: String) {
        val context = getApplication<Application>()
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun shareOffer(message: String) {
        val context = getApplication<Application>()
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Share Offer"))
    }

    fun clearOffer() {
        _generatedOffer.value = null
        _bulkMessages.value = emptyList()
    }
}
