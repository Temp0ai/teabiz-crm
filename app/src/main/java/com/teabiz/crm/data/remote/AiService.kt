package com.teabiz.crm.data.remote

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.teabiz.crm.data.model.AIResponse
import com.teabiz.crm.data.model.Lead
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private val gson = Gson()
    private var apiKey: String = ""
    private var model: String = "gpt-4"

    fun configure(apiKey: String, model: String = "gpt-4") {
        this.apiKey = apiKey
        this.model = model
    }

    suspend fun generateFollowUpMessage(
        lead: Lead,
        tone: String = "Professional",
        language: String = "English",
        messageType: String = "initial_inquiry"
    ): AIResponse {
        val prompt = buildPrompt(lead, tone, language, messageType)
        return generateResponse(prompt)
    }

    suspend fun generateBulkMessages(
        leads: List<Lead>,
        tone: String = "Professional",
        language: String = "English"
    ): Map<String, AIResponse> {
        val results = mutableMapOf<String, AIResponse>()
        for (lead in leads) {
            val response = generateFollowUpMessage(lead, tone, language)
            results[lead.id] = response
        }
        return results
    }

    suspend fun generateReviewResponse(
        review: String,
        rating: Int,
        businessName: String,
        tone: String = "Professional"
    ): AIResponse {
        val prompt = """
            You are a professional customer service representative for $businessName, a tea and coffee products business.
            
            A customer left a $rating-star review:
            "$review"
            
            Generate a professional, empathetic response that:
            - Thanks the customer for their feedback
            - ${if (rating >= 3) "Expresses gratitude for positive review" else "Addresses concerns and offers resolution"}
            - Maintains $tone tone
            - Encourages continued business
            - Is concise (2-3 sentences max)
            
            Response:
        """.trimIndent()

        return generateResponse(prompt)
    }

    suspend fun generateSEOContent(
        keyword: String,
        contentType: String = "blog_post",
        targetAudience: String = "tea and coffee business owners"
    ): AIResponse {
        val prompt = """
            Generate $contentType content optimized for the keyword "$keyword".
            Target audience: $targetAudience
            
            Requirements:
            - Include the keyword naturally 3-5 times
            - Write engaging, informative content
            - Use subheadings and bullet points
            - Include a compelling introduction
            - End with a clear call-to-action
            
            Content:
        """.trimIndent()

        return generateResponse(prompt)
    }

    suspend fun generateHashtags(
        productType: String,
        platform: String = "Instagram",
        count: Int = 15
    ): AIResponse {
        val prompt = """
            Generate $count relevant hashtags for a $platform post about "$productType" for a tea/coffee business.
            
            Include:
            - 5 high-volume industry hashtags
            - 5 niche-specific hashtags
            - 3 location-based hashtags (India-focused)
            - 2 trending/relevant hashtags
            
            Format: one hashtag per line, without the # symbol
            
            Hashtags:
        """.trimIndent()

        return generateResponse(prompt)
    }

    suspend fun analyzeCompetitor(
        competitorName: String,
        website: String,
        industry: String = "tea and coffee"
    ): AIResponse {
        val prompt = """
            Analyze the competitor "$competitorName" (website: $website) in the $industry industry.
            
            Provide analysis on:
            1. Estimated strengths
            2. Potential weaknesses
            3. Market positioning
            4. Content strategy assessment
            5. Opportunities to differentiate
            
            Analysis:
        """.trimIndent()

        return generateResponse(prompt)
    }

    private fun buildPrompt(lead: Lead, tone: String, language: String, messageType: String): String {
        val products = lead.productInterest.joinToString(", ")
        val clientInfo = if (lead.company.isNotBlank()) "from ${lead.company}" else ""
        val cityInfo = if (lead.city.isNotBlank()) "in ${lead.city}" else ""

        return when (messageType) {
            "initial_inquiry" -> """
                Create a professional follow-up message for ${lead.name} $clientInfo $cityInfo who inquired about: $products.
                
                Their inquiry message: "${lead.message}"
                
                Requirements:
                - Tone: $tone
                - Language: $language
                - Thank them for their interest
                - Highlight product quality and benefits
                - Mention competitive pricing
                - Include a clear call-to-action
                - Keep it concise (3-4 sentences)
                - Add relevant product recommendations
                
                Message:
            """.trimIndent()

            "stale_lead" -> """
                Re-engage ${lead.name} $clientInfo who showed interest in $products but hasn't responded.
                
                Requirements:
                - Tone: $tone
                - Language: $language
                - Create urgency with limited-time offer
                - Mention new products or updates
                - Offer a special discount
                - Keep it friendly and professional
                - Include a clear next step
                
                Message:
            """.trimIndent()

            "post_purchase" -> """
                Send a thank-you message to ${lead.name} $clientInfo for their recent purchase of $products.
                
                Requirements:
                - Tone: $tone
                - Language: $language
                - Thank them sincerely
                - Request feedback/review
                - Suggest complementary products
                - Mention referral program
                - Include support contact
                
                Message:
            """.trimIndent()

            "promotional" -> """
                Create a promotional message for ${lead.name} $clientInfo about our $products.
                
                Requirements:
                - Tone: $tone
                - Language: $language
                - Highlight special offer/discount
                - Emphasize value proposition
                - Create FOMO (fear of missing out)
                - Include clear pricing if applicable
                - Strong call-to-action
                
                Message:
            """.trimIndent()

            else -> """
                Create a message for ${lead.name} about $products.
                Tone: $tone, Language: $language
                
                Message:
            """.trimIndent()
        }
    }

    suspend fun generateResponse(prompt: String): AIResponse {
        return withContext(Dispatchers.IO) {
            try {
                if (apiKey.isBlank()) {
                    return@withContext AIResponse(
                        content = generateFallbackMessage(prompt),
                        model = "fallback",
                        tokensUsed = 0
                    )
                }

                val body = gson.toJson(mapOf(
                    "model" to model,
                    "messages" to listOf(
                        mapOf("role" to "system", "content" to "You are a professional sales and marketing AI for a tea and coffee products business. Generate compelling, personalized messages that drive engagement and sales."),
                        mapOf("role" to "user", "content" to prompt)
                    ),
                    "max_tokens" to 500,
                    "temperature" to 0.7
                ))

                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .post(body.toRequestBody("application/json".toMediaType()))
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (response.isSuccessful) {
                    val jsonResponse = gson.fromJson(responseBody, OpenAIResponse::class.java)
                    val content = jsonResponse.choices.firstOrNull()?.message?.content ?: ""
                    AIResponse(
                        content = content.trim(),
                        model = model,
                        tokensUsed = jsonResponse.usage?.totalTokens ?: 0
                    )
                } else {
                    AIResponse(
                        content = generateFallbackMessage(prompt),
                        model = "fallback",
                        tokensUsed = 0
                    )
                }
            } catch (e: Exception) {
                AIResponse(
                    content = generateFallbackMessage(prompt),
                    model = "fallback",
                    tokensUsed = 0
                )
            }
        }
    }

    private fun generateFallbackMessage(prompt: String): String {
        return when {
            prompt.contains("follow-up", ignoreCase = true) || prompt.contains("inquiry", ignoreCase = true) ->
                "Thank you for your interest in our products! We'd love to discuss how our premium tea and coffee solutions can benefit your business. Please let us know a convenient time for a quick call, or feel free to ask any questions. Looking forward to hearing from you!"
            prompt.contains("stale", ignoreCase = true) || prompt.contains("re-engage", ignoreCase = true) ->
                "Hi! We noticed you were interested in our products earlier. We have some exciting new offers and product updates we'd love to share with you. Would you like to schedule a quick chat to explore how we can work together?"
            prompt.contains("thank", ignoreCase = true) || prompt.contains("post_purchase", ignoreCase = true) ->
                "Thank you for choosing us! We hope you're enjoying your purchase. Your feedback means the world to us. If you need anything, don't hesitate to reach out. We also have a referral program - share with friends and earn rewards!"
            prompt.contains("promotional", ignoreCase = true) ->
                "Special offer just for you! Get exclusive deals on our premium tea and coffee products. Limited time offer - contact us today to learn more about our business partnership programs and bulk pricing!"
            prompt.contains("hashtag", ignoreCase = true) ->
                "TeaLovers, CoffeeTime, ChaiLovers, PremixLife, TeaBusiness, CoffeeShop, IndianChai, TeaVendor, CoffeeWholesale, MasalaChai, TeaEntrepreneur, CoffeeCulture, ChaiPeCharcha, TeaIndustry, CoffeeBusiness"
            else ->
                "Thank you for your interest. We'd be happy to help you with our range of tea and coffee products. Please let us know your requirements and we'll get back to you shortly."
        }
    }
}

data class OpenAIResponse(
    val choices: List<Choice>,
    val usage: Usage?
)

data class Choice(
    val message: MessageContent,
    val finishReason: String?
)

data class MessageContent(
    val role: String,
    val content: String
)

data class Usage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)
