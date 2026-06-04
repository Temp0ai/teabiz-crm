package com.teabiz.crm.data.remote

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiMediaGenerator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiService: GeminiService
) {

    data class ImageAnalysis(
        val description: String,
        val objects: List<String>,
        val colors: List<String>,
        val mood: String,
        val suggestions: List<String>,
        val caption: String,
        val hashtags: List<String>,
        val productMatch: String
    )

    data class VideoConcept(
        val title: String,
        val description: String,
        val duration: String,
        val script: List<String>,
        val visualCues: List<String>,
        val musicSuggestion: String,
        val captions: String,
        val hashtags: List<String>
    )

    data class GeneratedContent(
        val type: String,
        val content: String,
        val metadata: Map<String, String> = emptyMap()
    )

    suspend fun analyzeImage(uri: Uri): ImageAnalysis {
        if (!geminiService.isConfigured()) {
            return generateFallbackImageAnalysis()
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val inputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                return generateFallbackImageAnalysis()
            }

            val prompt = """
                Analyze this image for a tea/coffee business marketing team. Provide:
                
                1. DESCRIPTION: Detailed description of the image
                2. OBJECTS: List main objects/products visible (comma separated)
                3. COLORS: Dominant colors (comma separated)
                4. MOOD: Overall mood/atmosphere
                5. SUGGESTIONS: 3 marketing suggestions for using this image
                6. CAPTION: Engaging Instagram/Facebook caption
                7. HASHTAGS: 5 relevant hashtags (comma separated)
                8. PRODUCT_MATCH: Which tea/coffee product matches this image
                
                Format each section on a new line with the label.
            """.trimIndent()

            val response = model.generateContent(content {
                image(bitmap)
                text(prompt)
            })

            val content = response.text ?: ""
            parseImageAnalysis(content)
        } catch (e: Exception) {
            generateFallbackImageAnalysis()
        }
    }

    private fun parseImageAnalysis(content: String): ImageAnalysis {
        var description = ""
        var objects = emptyList<String>()
        var colors = emptyList<String>()
        var mood = ""
        var suggestions = emptyList<String>()
        var caption = ""
        var hashtags = emptyList<String>()
        var productMatch = ""

        for (line in content.lines()) {
            when {
                line.startsWith("DESCRIPTION:") -> description = line.substringAfter(":").trim()
                line.startsWith("OBJECTS:") -> objects = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("COLORS:") -> colors = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("MOOD:") -> mood = line.substringAfter(":").trim()
                line.startsWith("SUGGESTIONS:") -> suggestions = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("CAPTION:") -> caption = line.substringAfter(":").trim()
                line.startsWith("HASHTAGS:") -> hashtags = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("PRODUCT_MATCH:") -> productMatch = line.substringAfter(":").trim()
            }
        }

        return ImageAnalysis(description, objects, colors, mood, suggestions, caption, hashtags, productMatch)
    }

    private fun generateFallbackImageAnalysis(): ImageAnalysis {
        return ImageAnalysis(
            description = "A visually appealing image that can be used for tea/coffee business marketing",
            objects = listOf("Tea", "Coffee", "Cup", "Vending Machine"),
            colors = listOf("Brown", "Green", "White"),
            mood = "Professional and inviting",
            suggestions = listOf(
                "Use for Instagram product showcase post",
                "Create WhatsApp Business catalog image",
                "Use in Facebook ad campaign"
            ),
            caption = "Start your day with the perfect cup! ☕ Our premium tea and coffee solutions for your business.",
            hashtags = listOf("teapremix", "coffeepremix", "teatime", "coffeelovers", "vendingmachine"),
            productMatch = "Tea Premix"
        )
    }

    suspend fun generateVideoConcept(
        product: String,
        platform: String,
        duration: String = "30 seconds",
        style: String = "Professional"
    ): VideoConcept {
        if (!geminiService.isConfigured()) {
            return generateFallbackVideoConcept(product, platform)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Create a $style video concept for $product for $platform ($duration).
                
                Provide:
                1. TITLE: Catchy video title
                2. DESCRIPTION: Video concept overview
                3. DURATION: Recommended duration
                4. SCRIPT: 5-7 scene descriptions (one per line)
                5. VISUAL_CUES: 5 visual elements to include
                6. MUSIC: Music style suggestion
                7. CAPTIONS: On-screen text suggestions
                8. HASHTAGS: 5 relevant hashtags
                
                Format each section on a new line with the label.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val content = response.text ?: ""

            parseVideoConcept(content, product, platform)
        } catch (e: Exception) {
            generateFallbackVideoConcept(product, platform)
        }
    }

    private fun parseVideoConcept(content: String, product: String, platform: String): VideoConcept {
        var title = "Product Showcase Video"
        var description = ""
        var duration = "30 seconds"
        var script = emptyList<String>()
        var visualCues = emptyList<String>()
        var music = "Upbeat corporate"
        var captions = ""
        var hashtags = emptyList<String>()

        for (line in content.lines()) {
            when {
                line.startsWith("TITLE:") -> title = line.substringAfter(":").trim()
                line.startsWith("DESCRIPTION:") -> description = line.substringAfter(":").trim()
                line.startsWith("DURATION:") -> duration = line.substringAfter(":").trim()
                line.startsWith("SCRIPT:") -> script = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("VISUAL_CUES:") -> visualCues = line.substringAfter(":").trim().split(",").map { it.trim() }
                line.startsWith("MUSIC:") -> music = line.substringAfter(":").trim()
                line.startsWith("CAPTIONS:") -> captions = line.substringAfter(":").trim()
                line.startsWith("HASHTAGS:") -> hashtags = line.substringAfter(":").trim().split(",").map { it.trim() }
            }
        }

        return VideoConcept(title, description, duration, script, visualCues, music, captions, hashtags)
    }

    private fun generateFallbackVideoConcept(product: String, platform: String): VideoConcept {
        return VideoConcept(
            title = "$product - Premium Quality",
            description = "A compelling video showcasing the quality and benefits of $product for businesses",
            duration = "30 seconds",
            script = listOf(
                "Opening shot: Steaming cup of tea/coffee",
                "Product showcase: $product packaging",
                "Manufacturing process: Quality production",
                "Benefits: Time and cost savings",
                "Customer testimonial",
                "Call to action: Contact us"
            ),
            visualCues = listOf(
                "Close-up of product",
                "Satisfied customers",
                "Professional packaging",
                "Manufacturing facility",
                "Contact details overlay"
            ),
            musicSuggestion = "Upbeat, inspiring corporate",
            captions = "Premium Quality | Best Prices | Bulk Orders",
            hashtags = listOf("teapremix", "coffeepremix", "vendingmachine", "teabusiness", "coffeelovers")
        )
    }

    suspend fun generateSocialMediaPost(
        product: String,
        platform: String,
        occasion: String = "General"
    ): GeneratedContent {
        if (!geminiService.isConfigured()) {
            return generateFallbackSocialPost(product, platform)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Create a social media post for $product on $platform for $occasion.
                
                Provide:
                1. CAPTION: Engaging caption (max 200 words)
                2. HASHTAGS: 10 relevant hashtags
                3. CALL_TO_ACTION: Clear CTA
                4. BEST_TIME: Best posting time
                5. VISUAL_DESC: Describe ideal image/video
                
                Format each on a new line with the label.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val content = response.text ?: ""

            GeneratedContent(
                type = "social_post",
                content = content,
                metadata = mapOf("platform" to platform, "product" to product)
            )
        } catch (e: Exception) {
            generateFallbackSocialPost(product, platform)
        }
    }

    private fun generateFallbackSocialPost(product: String, platform: String): GeneratedContent {
        val caption = """
            ☕ Elevate your business with $product!
            
            ✅ Premium Quality
            ✅ Competitive Prices
            ✅ Bulk Discounts
            ✅ Pan India Delivery
            
            Perfect for cafes, offices, hotels, and restaurants.
            
            📞 Contact us for free demo!
            🌐 www.teabiz.com
        """.trimIndent()

        return GeneratedContent(
            type = "social_post",
            content = caption,
            metadata = mapOf(
                "platform" to platform,
                "product" to product,
                "hashtags" to "teapremix,coffeepremix,vendingmachine,teabusiness,coffeelovers"
            )
        )
    }

    suspend fun generateProductDescription(product: String): String {
        if (!geminiService.isConfigured()) {
            return "Premium $product - High quality, competitive prices, bulk discounts available. FSSAI certified."
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = "Write a compelling product description for: $product\n\nInclude: features, benefits, and a call to action. Max 100 words."

            val response = model.generateContent(prompt)
            response.text?.trim() ?: "Premium $product for your business needs."
        } catch (e: Exception) {
            "Premium $product - High quality, competitive prices, bulk discounts available."
        }
    }
}
