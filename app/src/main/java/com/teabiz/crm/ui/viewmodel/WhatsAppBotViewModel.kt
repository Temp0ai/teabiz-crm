package com.teabiz.crm.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.GeminiService
import com.teabiz.crm.data.remote.WhatsAppService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WhatsAppBotViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val whatsappService: WhatsAppService,
    private val geminiService: GeminiService
) : ViewModel() {

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _autoReplyEnabled = MutableStateFlow(true)
    val autoReplyEnabled: StateFlow<Boolean> = _autoReplyEnabled

    private val _welcomeMessage = MutableStateFlow(
        "Welcome! 👋 Thank you for contacting us.\n\nWe are a leading supplier of tea premix, coffee premix, and vending machines.\n\nHow can we help you today?"
    )
    val welcomeMessage: StateFlow<String> = _welcomeMessage

    private val _conversationHistory = MutableStateFlow<List<Conversation>>(emptyList())
    val conversationHistory: StateFlow<List<Conversation>> = _conversationHistory

    private val _botStatus = MutableStateFlow("")
    val botStatus: StateFlow<String> = _botStatus

    private val _productRecommendations = MutableStateFlow<List<String>>(emptyList())
    val productRecommendations: StateFlow<List<String>> = _productRecommendations

    private var pollingJob: kotlinx.coroutines.Job? = null

    init {
        _productRecommendations.value = listOf(
            "Tea Premix - Instant premix for vending machines",
            "Coffee Premix - Premium coffee for offices & hotels",
            "Vending Machines - Automatic 2-Option & 3-Option",
            "Bulk Orders - Special pricing for 100+ units",
            "Free Demo - Try before you buy"
        )
    }

    fun toggleBot() {
        if (_isRunning.value) {
            stopBot()
        } else {
            startBot()
        }
    }

    private fun startBot() {
        _isRunning.value = true
        _botStatus.value = "Bot started - Listening for messages..."
        startPolling()
    }

    private fun stopBot() {
        _isRunning.value = false
        _botStatus.value = "Bot stopped"
        pollingJob?.cancel()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (_isRunning.value) {
                try {
                    if (whatsappService.checkConnection()) {
                        _botStatus.value = "Connected - Checking for new messages..."
                    } else {
                        _botStatus.value = "Waiting for WhatsApp connection..."
                    }
                } catch (e: Exception) {
                    _botStatus.value = "Error: ${e.message}"
                }
                delay(5000L)
            }
        }
    }

    fun setAutoReply(enabled: Boolean) {
        _autoReplyEnabled.value = enabled
    }

    fun setWelcomeMessage(message: String) {
        _welcomeMessage.value = message
    }

    fun saveSettings() {
        _botStatus.value = "Settings saved"
    }

    suspend fun generateAutoReply(
        userMessage: String,
        contactName: String,
        productInterest: String = ""
    ): String {
        if (!geminiService.isConfigured()) {
            return generateFallbackReply(userMessage, contactName)
        }

        return try {
            val response = geminiService.generateSmartMessage(
                productType = productInterest.ifBlank { "tea and coffee premix" },
                tone = "Friendly",
                language = "English",
                messageType = "promotional"
            )

            if (response.isNotBlank()) {
                response
            } else {
                generateFallbackReply(userMessage, contactName)
            }
        } catch (e: Exception) {
            generateFallbackReply(userMessage, contactName)
        }
    }

    private fun generateFallbackReply(userMessage: String, contactName: String): String {
        val lowerMsg = userMessage.lowercase()

        return when {
            lowerMsg.contains("price") || lowerMsg.contains("cost") || lowerMsg.contains("rate") ->
                "Hi $contactName! 😊\n\nOur best prices:\n• Tea Premix: ₹180/kg\n• Coffee Premix: ₹350/kg\n• Vending Machines: Starting ₹25,000\n\nBulk orders get extra 10-15% discount!\n\nShall I share the full price list?"

            lowerMsg.contains("machine") || lowerMsg.contains("vending") ->
                "Hi $contactName! 🤖\n\nWe have:\n• 2-Option Machine - ₹25,000\n• 3-Option Machine - ₹35,000\n• Premium Machine - ₹45,000\n\nAll machines come with 1 year warranty + free installation!\n\nWant a free demo?"

            lowerMsg.contains("sample") || lowerMsg.contains("demo") || lowerMsg.contains("try") ->
                "Hi $contactName! ✅\n\nFree demo available!\n• We'll send sample products\n• Or arrange a machine demo at your location\n• No commitment required\n\nShare your address, we'll schedule it!"

            lowerMsg.contains("order") || lowerMsg.contains("buy") || lowerMsg.contains("purchase") ->
                "Hi $contactName! 🛒\n\nTo place an order:\n1. Tell us the products you need\n2. Share your delivery address\n3. We'll confirm with pricing\n\nMinimum order: 50 units\nDelivery: 3-5 days pan India\n\nWhat would you like to order?"

            lowerMsg.contains("quality") || lowerMsg.contains("certificate") ->
                "Hi $contactName! ⭐\n\nOur quality assurance:\n• FSSAI certified products\n• ISO 22000 compliant\n• Regular quality testing\n• Premium ingredients only\n\n100% satisfaction guaranteed!\n\nWant to see our certifications?"

            lowerMsg.contains("delivery") || lowerMsg.contains("shipping") ->
                "Hi $contactName! 🚚\n\nDelivery details:\n• Pan India delivery\n• 3-5 business days\n• Free delivery on orders above ₹10,000\n• Track your order via WhatsApp\n\nWhere do you need delivery?"

            lowerMsg.contains("thank") || lowerMsg.contains("thanks") ->
                "You're welcome, $contactName! 😊\n\nWe're always here to help. Feel free to reach out anytime!\n\nHave a great day! ☕"

            lowerMsg.contains("hi") || lowerMsg.contains("hello") || lowerMsg.contains("hey") ->
                "Hi $contactName! 👋\n\nWelcome to our Tea & Coffee business!\n\nWe offer:\n☕ Premium Tea Premix\n☕ Coffee Premix\n🤖 Vending Machines\n📦 Bulk Orders\n\nHow can we help you today?"

            lowerMsg.contains("offer") || lowerMsg.contains("discount") || lowerMsg.contains("deal") ->
                "Hi $contactName! 🎉\n\nSpecial offers this month:\n• 15% off on bulk orders (100+ units)\n• Free machine with 500kg order\n• Free delivery on orders above ₹10,000\n\nDon't miss out! Offer valid till month end.\n\nWant to grab these deals?"

            else ->
                "Hi $contactName! 👋\n\nThank you for your message!\n\nWe are a leading supplier of:\n☕ Tea & Coffee Premix\n🤖 Vending Machines\n📦 Bulk Orders\n\nHow can we help you? You can ask about:\n• Products & Prices\n• Machine Demo\n• Bulk Orders\n• Delivery Info"
        }
    }

    fun addConversation(contactName: String, userMessage: String, botReply: String) {
        val conversation = Conversation(
            contactName = contactName,
            userMessage = userMessage,
            botReply = botReply,
            timestamp = System.currentTimeMillis()
        )
        _conversationHistory.value = _conversationHistory.value + conversation

        if (_conversationHistory.value.size > 50) {
            _conversationHistory.value = _conversationHistory.value.takeLast(50)
        }
    }

    fun sendAutoReply(phone: String, message: String) {
        viewModelScope.launch {
            try {
                val cleanPhone = phone.replace(Regex("[^0-9]"), "")
                if (cleanPhone.length >= 10) {
                    val url = "https://wa.me/$cleanPhone?text=${Uri.encode(message)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
            } catch (e: Exception) {
                _botStatus.value = "Error sending reply: ${e.message}"
            }
        }
    }

    data class Conversation(
        val contactName: String,
        val userMessage: String,
        val botReply: String,
        val timestamp: Long
    )
}
