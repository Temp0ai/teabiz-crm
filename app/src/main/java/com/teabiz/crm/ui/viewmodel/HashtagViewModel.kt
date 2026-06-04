package com.teabiz.crm.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.GeminiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HashtagViewModel @Inject constructor(
    private val aiService: AiService,
    private val geminiService: GeminiService
) : ViewModel() {

    companion object {
        val PRODUCT_TYPES = listOf(
            "Tea Premix",
            "Coffee Premix",
            "Nescafe Premix",
            "Tea Vending Machine",
            "Coffee Vending Machine",
            "Tea & Coffee Vending Machine",
            "2-Option Vending Machine",
            "Tea Machine",
            "Coffee Machine",
            "Instant Chai",
            "Masala Chai Premix",
            "Tea Business",
            "Coffee Business"
        )
    }

    private val _generatedHashtags = MutableStateFlow<List<String>>(emptyList())
    val generatedHashtags: StateFlow<List<String>> = _generatedHashtags

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _selectedPlatform = MutableStateFlow("Instagram")
    val selectedPlatform: StateFlow<String> = _selectedPlatform

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    private val _productType = MutableStateFlow("Tea Premix")
    val productType: StateFlow<String> = _productType

    private val _geminiApiKey = MutableStateFlow("")
    val geminiApiKey: StateFlow<String> = _geminiApiKey

    fun setImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun updatePlatform(platform: String) {
        _selectedPlatform.value = platform
    }

    fun updateProductType(type: String) {
        _productType.value = type
    }

    fun setGeminiApiKey(key: String) {
        _geminiApiKey.value = key
        if (key.isNotBlank()) {
            geminiService.configure(key)
        }
    }

    fun generateHashtags() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val platform = _selectedPlatform.value
                val product = _productType.value
                val count = if (platform == "Both") 40 else 30

                // Try Gemini first (trending + smart)
                var hashtags = emptyList<String>()
                if (geminiService.isConfigured()) {
                    try {
                        val geminiResponse = geminiService.generateTrendyHashtags(product, platform, count)
                        hashtags = parseHashtagsFromResponse(geminiResponse)
                    } catch (_: Exception) {}
                }

                if (hashtags.size >= 5) {
                    _generatedHashtags.value = hashtags
                } else {
                    _generatedHashtags.value = getSmartFallbackHashtags(product, platform)
                }
            } catch (e: Exception) {
                _generatedHashtags.value = getSmartFallbackHashtags(_productType.value, _selectedPlatform.value)
            } finally {
                _isGenerating.value = false
            }
        }
    }

    private fun parseHashtagsFromResponse(content: String): List<String> {
        return content.lines()
            .map { line ->
                line.trim()
                    .removePrefix("#")
                    .removePrefix("- ")
                    .removePrefix("* ")
                    .removePrefix("• ")
                    .replace(Regex("^\\d+\\.\\s*"), "")
                    .replace(Regex("[\"']"), "")
                    .trim()
            }
            .filter { it.isNotBlank() && it.length > 2 && it.length < 50 }
            .filter { !it.contains(" ") || it.contains("_") }
            .filter { !it.contains(":") }
            .filter { !it.startsWith("Generate") && !it.startsWith("Format") && !it.startsWith("Example") && !it.startsWith("You are") }
            .distinct()
    }

    private fun getSmartFallbackHashtags(product: String, platform: String): List<String> {
        val tags = when {
            product.contains("Vending Machine") || product.contains("Machine") -> vendingMachineTags(product)
            product.contains("Tea") && product.contains("Coffee") -> teaAndCoffeeTags()
            product.contains("Coffee") -> coffeeOnlyTags()
            else -> teaOnlyTags()
        }
        return if (platform == "Facebook") tags.take(12) else tags.take(30)
    }

    private fun vendingMachineTags(product: String): List<String> {
        val base = listOf(
            "vendingmachine", "vendingmachinelife", "vendingbusiness", "teavendingmachine",
            "coffeevendingmachine", "teacoffeevendingmachine", "2optionvendingmachine",
            "vendingmachinebusiness", "vendingstartup", "indianvending", "vendingmachinesolutions",
            "automaticvending", "hotbeveragevendingmachine", "teaandcoffeevending",
            "vendingmachinesupplier", "vendingmachinedealer", "vendingmachineindia",
            "teapremixmachine", "instantteavending", "masalachai",
            "teapremix", "chaipremix", "instantchai", "teatime",
            "chaiwallah", "teabusiness", "teavending", "chaiwallahs",
            "tealovers", "tealoversofinstagram", "chailoversofinstagram",
            "coffeepremix", "instantcoffee", "coffeelovers", "coffeeloversofindia",
            "indianchaiwallah", "teatimeindia", "chaipecharcha",
            "cuttingchai", "adrakchai", "chaiaddict"
        )

        return when {
            product.contains("Tea & Coffee") || product.contains("2-Option") -> base
            product.contains("Coffee") -> base.filter { !it.contains("tea") || it.contains("coffee") || it.contains("vending") }
            else -> base.filter { !it.contains("coffee") || it.contains("tea") || it.contains("vending") }
        }.distinct()
    }

    private fun teaAndCoffeeTags(): List<String> = listOf(
        "teapremix", "chaipremix", "coffeepremix", "teatime", "coffeetime",
        "masalachai", "indianchai", "chaiwallah", "tealovers", "coffeelovers",
        "teabusiness", "coffeeshopbusiness", "teavending", "instantchai", "instantcoffee",
        "chailover", "cuttingchai", "adrakchai", "teavendingmachine", "coffeevendingmachine",
        "tealoversofinstagram", "chailoversofinstagram", "coffeeloversofindia",
        "chaipecharcha", "teatimeindia", "teaculture", "teaindustry",
        "chaiaddict", "premixlife", "teapremixindia", "coffeegram",
        "masalachailovers", "teastartup", "coffeemachine", "indianchaiwallah",
        "teavendor", "coffeevend", "beveragelovers", "teacoffee"
    )

    private fun coffeeOnlyTags(): List<String> = listOf(
        "coffee", "coffeelovers", "coffeetime", "espresso", "coffeeshop",
        "coffeepremix", "instantcoffee", "coffeemachine", "nescafe",
        "coffeeshopbusiness", "coffeeloversclub", "coffeevending",
        "coffeeloversofindia", "filtercoffee", "coffeegram", "coffeeoftheday",
        "coffeelove", "coffeestartup", "coffeebusiness", "coffeeloversofinstagram",
        "indiancoffee", "coffeeculture", "coffeblend", "darkroast",
        "coffeegrind", "coffeebean", "brewedcoffee", "coffeepremium",
        "coffeevendingmachine", "hotcoffee", "coldcoffee", "coffeelover",
        "baristalife", "coffeepassion", "morningcoffee", "coffeefirst"
    )

    private fun teaOnlyTags(): List<String> = listOf(
        "teapremix", "chaipremix", "teatime", "masalachai", "indianchai",
        "chaiwallah", "tealovers", "teabusiness", "teavending", "instantchai",
        "chailover", "cuttingchai", "adrakchai", "chaiaddict",
        "morningchai", "eveningchai", "chaipecharcha", "tealoversofinstagram",
        "chailoversofinstagram", "teavendor", "teavendingmachine", "chaiinstant",
        "premixlife", "teapremixindia", "chaiwallahs", "indianchaiwallah",
        "teatimeindia", "delhifoodies", "mumbaifoodies", "teaculture",
        "teaindustry", "teastartup", "teabusinessindia", "masalachailovers",
        "teapremixmachine", "teavendingbusiness", "indiatea", "tealoversclub"
    )
}
