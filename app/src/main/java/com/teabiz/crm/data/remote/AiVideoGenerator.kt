package com.teabiz.crm.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Environment
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiVideoGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiService: GeminiService
) {

    data class GeneratedVideo(
        val title: String,
        val description: String,
        val script: String,
        val visualGuide: String,
        val hashtags: List<String>,
        val musicSuggestion: String,
        val duration: String,
        val callToAction: String,
        val platform: String,
        val generatedImagePath: String? = null
    )

    data class GeneratedImage(
        val prompt: String,
        val style: String,
        val width: Int,
        val height: Int,
        val imagePath: String? = null,
        val description: String = ""
    )

    suspend fun generateVideoContent(
        product: String,
        platform: String,
        duration: String = "30 seconds",
        style: String = "Professional",
        aspectRatio: String = "9:16"
    ): GeneratedVideo {
        if (!geminiService.isConfigured()) {
            return generateFallbackVideo(product, platform)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-2.0-flash-exp",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Create a detailed video script for: $product
                Platform: $platform
                Duration: $duration
                Style: $style
                Aspect Ratio: $aspectRatio
                
                Provide in this exact format:
                TITLE: catchy video title
                DESCRIPTION: brief video overview (2-3 sentences)
                SCRIPT: scene-by-scene script with timing
                VISUAL_GUIDE: detailed visual instructions for each scene
                HASHTAGS: 8 relevant hashtags separated by comma
                MUSIC: music style suggestion
                CTA: call to action text
            """.trimIndent()

            val response = model.generateContent(prompt)
            val content = response.text ?: ""

            parseVideoContent(content, product, platform, duration)
        } catch (e: Exception) {
            generateFallbackVideo(product, platform)
        }
    }

    private fun parseVideoContent(content: String, product: String, platform: String, duration: String): GeneratedVideo {
        var title = "$product - Premium Quality Video"
        var description = "A compelling video showcasing $product for your business"
        var script = ""
        var visualGuide = ""
        var hashtags = listOf("#teapremix", "#coffeepremix", "#vendingmachine", "#teabusiness", "#coffeebusiness", "#b2b", "#wholesale", "#premiumquality")
        var music = "Upbeat corporate"
        var cta = "Contact us for bulk orders!"

        for (line in content.lines()) {
            when {
                line.startsWith("TITLE:") -> title = line.substringAfter(":").trim()
                line.startsWith("DESCRIPTION:") -> description = line.substringAfter(":").trim()
                line.startsWith("SCRIPT:") -> script = line.substringAfter(":").trim()
                line.startsWith("VISUAL_GUIDE:") -> visualGuide = line.substringAfter(":").trim()
                line.startsWith("HASHTAGS:") -> hashtags = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("MUSIC:") -> music = line.substringAfter(":").trim()
                line.startsWith("CTA:") -> cta = line.substringAfter(":").trim()
            }
        }

        return GeneratedVideo(
            title = title,
            description = description,
            script = script,
            visualGuide = visualGuide,
            hashtags = hashtags,
            musicSuggestion = music,
            duration = duration,
            callToAction = cta,
            platform = platform
        )
    }

    private fun generateFallbackVideo(product: String, platform: String): GeneratedVideo {
        return GeneratedVideo(
            title = "$product - Premium Quality Video",
            description = "A compelling video showcasing the quality and benefits of $product",
            script = """
                Scene 1 (0-5s): Opening shot - Steaming cup of tea/coffee with product logo
                Scene 2 (5-10s): Product showcase - Display $product packaging
                Scene 3 (10-15s): Manufacturing - Show quality production process
                Scene 4 (15-20s): Benefits - Highlight time and cost savings
                Scene 5 (20-25s): Customer testimonial - Satisfied customer quote
                Scene 6 (25-30s): Call to action - Contact details and offer
            """.trimIndent(),
            visualGuide = "Use warm colors, professional lighting, close-up product shots, customer satisfaction scenes",
            hashtags = listOf("#teapremix", "#coffeepremix", "#vendingmachine", "#teabusiness", "#coffeebusiness", "#b2b", "#wholesale", "#premiumquality"),
            musicSuggestion = "Upbeat, inspiring corporate background music",
            duration = "30 seconds",
            callToAction = "📞 Contact us for bulk orders! 🌐 www.teabiz.com",
            platform = platform
        )
    }

    suspend fun generateImageContent(
        product: String,
        style: String = "Professional",
        aspectRatio: String = "1:1",
        purpose: String = "Social Media Post"
    ): GeneratedImage {
        if (!geminiService.isConfigured()) {
            return generateFallbackImage(product, style)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-2.0-flash-exp",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Generate a detailed image prompt for: $product
                Style: $style
                Purpose: $purpose
                Aspect Ratio: $aspectRatio
                
                Provide:
                - Detailed visual description for AI image generation
                - Color palette suggestions
                - Composition guidelines
                - Text overlays needed
            """.trimIndent()

            val response = model.generateContent(prompt)
            val description = response.text ?: ""

            GeneratedImage(
                prompt = description,
                style = style,
                width = if (aspectRatio == "1:1") 1080 else if (aspectRatio == "16:9") 1920 else 1080,
                height = if (aspectRatio == "1:1") 1080 else if (aspectRatio == "9:16") 1920 else 1080,
                description = description
            )
        } catch (e: Exception) {
            generateFallbackImage(product, style)
        }
    }

    private fun generateFallbackImage(product: String, style: String): GeneratedImage {
        return GeneratedImage(
            prompt = "Professional product photography of $product, clean white background, studio lighting, high resolution",
            style = style,
            width = 1080,
            height = 1080,
            description = "A clean, professional product image suitable for social media and marketing materials."
        )
    }

    suspend fun generateBatchContent(
        products: List<String>,
        platform: String,
        count: Int = 5
    ): List<GeneratedVideo> {
        return products.take(count).map { product ->
            generateVideoContent(product, platform)
        }
    }

    fun getYouTubeDescription(video: GeneratedVideo): String {
        return """
            ${video.title}
            
            ${video.description}
            
            📋 Script:
            ${video.script}
            
            🎵 Music: ${video.musicSuggestion}
            
            🔗 Contact us for bulk orders!
            
            ${video.hashtags.joinToString(" ")}
            
            #teabiz #coffeeteam #vendingmachines #teapremix #coffeepremix #b2b #wholesale
        """.trimIndent()
    }

    fun getInstagramCaption(video: GeneratedVideo): String {
        val shortHashtags = video.hashtags.take(5).joinToString(" ")
        return """
            ✨ ${video.title}
            
            ${video.description}
            
            💪 Why choose us?
            ✅ Premium Quality
            ✅ Competitive Prices
            ✅ Pan India Delivery
            
            ${video.callToAction}
            
            $shortHashtags
        """.trimIndent()
    }

    fun getWhatsAppMessage(video: GeneratedVideo): String {
        return """
            🎬 *${video.title}*
            
            ${video.description}
            
            📋 *Video Script Preview:*
            ${video.script.lines().take(3).joinToString("\n")}
            
            ${video.callToAction}
            
            ${video.hashtags.take(3).joinToString(" ")}
        """.trimIndent()
    }

    fun getLinkedInPost(video: GeneratedVideo): String {
        return """
            🚀 ${video.title}
            
            ${video.description}
            
            In today's competitive market, having the right tea/coffee solutions can transform your business.
            
            💡 Key Benefits:
            • Premium quality products
            • Cost-effective solutions
            • Reliable supply chain
            
            ${video.callToAction}
            
            ${video.hashtags.take(5).joinToString(" ")}
        """.trimIndent()
    }

    fun getFacebookAd(video: GeneratedVideo): String {
        return """
            📢 ${video.title}
            
            ${video.description}
            
            🔥 Limited Time Offer!
            Get the best deals on bulk orders.
            
            ${video.callToAction}
            
            ${video.hashtags.take(3).joinToString(" ")}
        """.trimIndent()
    }
}
