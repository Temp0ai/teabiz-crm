package com.teabiz.crm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.remote.AiVideoGenerator
import com.teabiz.crm.data.remote.AiVideoGenerator.GeneratedVideo
import com.teabiz.crm.data.remote.AiVideoGenerator.GeneratedImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiVideoViewModel @Inject constructor(
    application: Application,
    private val videoGenerator: AiVideoGenerator
) : AndroidViewModel(application) {

    private val _videoContent = MutableStateFlow<GeneratedVideo?>(null)
    val videoContent = _videoContent.asStateFlow()

    private val _imageContent = MutableStateFlow<GeneratedImage?>(null)
    val imageContent = _imageContent.asStateFlow()

    private val _batchVideos = MutableStateFlow<List<GeneratedVideo>>(emptyList())
    val batchVideos = _batchVideos.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _statusMessage = MutableStateFlow("")
    val statusMessage = _statusMessage.asStateFlow()

    private val _copiedContent = MutableStateFlow("")
    val copiedContent = _copiedContent.asStateFlow()

    fun generateVideo(
        product: String,
        platform: String,
        duration: String = "30 seconds",
        style: String = "Professional",
        aspectRatio: String = "9:16"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating video content with AI..."
            try {
                val video = videoGenerator.generateVideoContent(product, platform, duration, style, aspectRatio)
                _videoContent.value = video
                _statusMessage.value = "Video content generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateImage(
        product: String,
        style: String = "Professional",
        aspectRatio: String = "1:1",
        purpose: String = "Social Media Post"
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating image content with AI..."
            try {
                val image = videoGenerator.generateImageContent(product, style, aspectRatio, purpose)
                _imageContent.value = image
                _statusMessage.value = "Image content generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun generateBatch(products: List<String>, platform: String, count: Int = 5) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Generating batch content..."
            try {
                val videos = videoGenerator.generateBatchContent(products, platform, count)
                _batchVideos.value = videos
                _statusMessage.value = "Batch content generated!"
            } catch (e: Exception) {
                _statusMessage.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getYoutubeDescription(): String {
        return _videoContent.value?.let { videoGenerator.getYouTubeDescription(it) } ?: ""
    }

    fun getInstagramCaption(): String {
        return _videoContent.value?.let { videoGenerator.getInstagramCaption(it) } ?: ""
    }

    fun getWhatsAppMessage(): String {
        return _videoContent.value?.let { videoGenerator.getWhatsAppMessage(it) } ?: ""
    }

    fun getLinkedInPost(): String {
        return _videoContent.value?.let { videoGenerator.getLinkedInPost(it) } ?: ""
    }

    fun getFacebookAd(): String {
        return _videoContent.value?.let { videoGenerator.getFacebookAd(it) } ?: ""
    }

    fun clearContent() {
        _videoContent.value = null
        _imageContent.value = null
        _batchVideos.value = emptyList()
    }
}
