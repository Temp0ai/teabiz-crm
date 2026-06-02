package com.teabiz.crm.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.AiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HashtagViewModel @Inject constructor(
    private val aiService: AiService
) : ViewModel() {

    private val _generatedHashtags = MutableStateFlow<List<String>>(emptyList())
    val generatedHashtags: StateFlow<List<String>> = _generatedHashtags

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _selectedPlatform = MutableStateFlow("Instagram")
    val selectedPlatform: StateFlow<String> = _selectedPlatform

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    fun setImageUri(uri: Uri) {
        _selectedImageUri.value = uri
    }

    fun updatePlatform(platform: String) {
        _selectedPlatform.value = platform
    }

    fun generateHashtags() {
        viewModelScope.launch {
            _isGenerating.value = true
            try {
                val platform = _selectedPlatform.value
                val hasImage = _selectedImageUri.value != null

                val prompt = buildString {
                    appendLine("Generate optimized hashtags for a $platform post about tea premix products.")

                    if (hasImage) {
                        appendLine("The post includes an image/video of a tea premix product, machine, or preparation setup.")
                        appendLine("The content shows a tea business context.")
                    }

                    appendLine()
                    appendLine("Requirements:")
                    appendLine("- 10 high-volume hashtags (1M+ posts)")
                    appendLine("- 10 niche tea/coffee industry hashtags")
                    appendLine("- 5 India/local business hashtags")
                    appendLine("- 5 trending tea/chai hashtags")
                    appendLine("- 3 product-specific hashtags (premix, machine, vending)")
                    appendLine("- 2 engagement hashtags")

                    if (platform == "Instagram") {
                        appendLine("- Optimize for Instagram algorithm (mix of sizes)")
                    } else if (platform == "Facebook") {
                        appendLine("- Optimize for Facebook reach (fewer, more targeted)")
                    } else {
                        appendLine("- For Instagram: include 30 hashtags")
                        appendLine("- For Facebook: include 10 hashtags")
                    }

                    appendLine()
                    appendLine("Format: one hashtag per line, without the # symbol")
                    appendLine("Include popular tea hashtags like: chai, teatime, masalachai, indianchai, chaiwallah, tealovers")
                }

                val response = aiService.generateHashtags(
                    productType = "tea premix, chai premix, tea vending machine, instant chai, tea business",
                    platform = platform,
                    count = if (platform == "Both") 40 else 25
                )

                val hashtags = response.content.lines()
                    .map { it.trim().removePrefix("#").removePrefix("- ").removePrefix("* ") }
                    .filter { it.isNotBlank() && it.length > 2 }
                    .distinct()

                _generatedHashtags.value = hashtags
            } catch (e: Exception) {
                _generatedHashtags.value = listOf(
                    "teapremix", "chaipremix", "teatime", "masalachai", "indianchai",
                    "chaiwallah", "tealovers", "teabusiness", "teavending", "instantchai",
                    "chailover", "teapremixlife", "chaiwallahs", "teavendor", "coffeepremix",
                    "teastartup", "chaigeneration", "indiatea", "teaindustry", "coffeemachine",
                    "tealoversofinstagram", "chailoversofinstagram", "teavendorlife",
                    "chaipecharcha", "teaculture", "indianchaiwallah", "teatimeindia",
                    "chaiaddict", "premixlife", "teavendingmachine"
                )
            } finally {
                _isGenerating.value = false
            }
        }
    }
}
