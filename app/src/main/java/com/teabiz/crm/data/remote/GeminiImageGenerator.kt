package com.teabiz.crm.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.ResponseStoppedException
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiImageGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiService: GeminiService
) {

    data class ImageResult(
        val success: Boolean,
        val description: String,
        val prompt: String,
        val errorMessage: String = ""
    )

    suspend fun generateImage(
        prompt: String,
        style: String = "Professional product photography",
        aspectRatio: String = "1:1"
    ): ImageResult {
        if (!geminiService.isConfigured()) {
            return ImageResult(
                success = false,
                description = "",
                prompt = prompt,
                errorMessage = "Gemini API key not configured"
            )
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-2.0-flash-exp",
                apiKey = geminiService.getApiKey()
            )

            val enhancedPrompt = """
                Generate an image: $prompt
                Style: $style
                Aspect Ratio: $aspectRatio
                Make it suitable for tea/coffee business marketing.
                High quality, professional, visually appealing.
            """.trimIndent()

            val response = model.generateContent(content {
                text(enhancedPrompt)
            })

            ImageResult(
                success = true,
                description = response.text ?: "Image generated successfully",
                prompt = prompt
            )
        } catch (e: Exception) {
            ImageResult(
                success = false,
                description = "",
                prompt = prompt,
                errorMessage = e.message ?: "Unknown error"
            )
        }
    }

    suspend fun generateVideoPrompt(
        product: String,
        style: String = "Cinematic",
        duration: String = "30 seconds"
    ): String {
        if (!geminiService.isConfigured()) {
            return "Generate a $style video of $product, duration $duration, professional quality"
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-2.0-flash-exp",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Create a detailed Veo 3 video generation prompt for:
                Product: $product
                Style: $style
                Duration: $duration
                
                Include:
                - Scene descriptions
                - Camera movements
                - Lighting
                - Music mood
                - Text overlays
            """.trimIndent()

            val response = model.generateContent(prompt)
            response.text ?: "Generate a professional video of $product"
        } catch (e: Exception) {
            "Generate a professional video of $product with $style style"
        }
    }

    suspend fun analyzeAndSuggestImage(
        product: String,
        platform: String
    ): List<String> {
        if (!geminiService.isConfigured()) {
            return listOf(
                "Product on white background",
                "Lifestyle shot with cup",
                "Manufacturing process",
                "Happy customers",
                "Packaging showcase"
            )
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-2.0-flash-exp",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Suggest 5 image ideas for $product on $platform.
                Format: one suggestion per line, start each with a number.
            """.trimIndent()

            val response = model.generateContent(prompt)
            response.text?.lines()?.filter { it.isNotBlank() }?.take(5) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
