package com.teabiz.crm.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.AiMediaGenerator
import com.teabiz.crm.data.remote.AiMediaGenerator.GeneratedContent
import com.teabiz.crm.data.remote.AiMediaGenerator.ImageAnalysis
import com.teabiz.crm.data.remote.AiMediaGenerator.VideoConcept
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiMediaViewModel @Inject constructor(
    application: Application,
    private val mediaGenerator: AiMediaGenerator
) : AndroidViewModel(application) {

    private val _imageAnalysis = MutableStateFlow<ImageAnalysis?>(null)
    val imageAnalysis = _imageAnalysis.asStateFlow()

    private val _videoConcept = MutableStateFlow<VideoConcept?>(null)
    val videoConcept = _videoConcept.asStateFlow()

    private val _socialPost = MutableStateFlow<GeneratedContent?>(null)
    val socialPost = _socialPost.asStateFlow()

    private val _productDescription = MutableStateFlow<String>("")
    val productDescription = _productDescription.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage = _statusMessage.asStateFlow()

    fun analyzeImage(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Analyzing image..."
            try {
                val analysis = mediaGenerator.analyzeImage(uri)
                _imageAnalysis.value = analysis
                _statusMessage.value = "Image analysis complete!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateVideoConcept(
        product: String,
        platform: String,
        duration: String = "30 seconds",
        style: String = "Professional"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating video concept..."
            try {
                val concept = mediaGenerator.generateVideoConcept(product, platform, duration, style)
                _videoConcept.value = concept
                _statusMessage.value = "Video concept generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateSocialMediaPost(product: String, platform: String, occasion: String = "General") {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating social media post..."
            try {
                val post = mediaGenerator.generateSocialMediaPost(product, platform, occasion)
                _socialPost.value = post
                _statusMessage.value = "Social media post generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateProductDescription(product: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating product description..."
            try {
                val desc = mediaGenerator.generateProductDescription(product)
                _productDescription.value = desc
                _statusMessage.value = "Product description generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearAnalysis() {
        _imageAnalysis.value = null
        _videoConcept.value = null
        _socialPost.value = null
        _productDescription.value = ""
    }
}
