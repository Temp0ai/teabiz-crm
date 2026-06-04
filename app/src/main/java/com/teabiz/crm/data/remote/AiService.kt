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
        val isHindi = prompt.contains("Language: Hindi", ignoreCase = true)
        val isMarathi = prompt.contains("Language: Marathi", ignoreCase = true)
        val lang = when {
            isHindi -> "hi"
            isMarathi -> "mr"
            else -> "en"
        }

        val productHint = Regex("who (?:inquired about|showed interest in|recent purchase of|about our) (.+?)(?:\\.|\\n)").find(prompt)?.groupValues?.get(1) ?: "our products"

        return when {
            prompt.contains("competitor", ignoreCase = true) && prompt.contains("analyze", ignoreCase = true) ->
                """
                **Competitor Analysis**

                **Strengths:**
                • Established brand presence in the market
                • Wide distribution network
                • Strong online visibility and social media presence
                • Competitive pricing strategy

                **Weaknesses:**
                • Limited product customization options
                • Slower response to market trends
                • Customer service could be improved

                **Market Positioning:**
                • Mid-range pricing targeting small to medium businesses
                • Focused on bulk B2B sales
                • Strong in local/regional markets

                **Opportunities to Differentiate:**
                • Offer personalized product blends and flavors
                • Provide better after-sales support and training
                • Launch loyalty programs for repeat customers
                • Use AI-powered customer engagement tools
                • Focus on organic/sustainable product lines

                **Recommendation:** Focus on superior customer service and product quality to stand out.
                """.trimIndent()

            prompt.contains("follow-up", ignoreCase = true) || prompt.contains("inquiry", ignoreCase = true) ->
                when (lang) {
                    "hi" -> "नमस्ते! $productHint में आपकी रुचि के लिए धन्यवाद। हम आपके व्यवसाय के लिए हमारे प्रीमियम उत्पादों के लाभों पर चर्चा करना चाहेंगे। कृपया हमें एक सुविधाजनक समय बताएं, या कोई भी प्रश्न पूछें। आपसे सुनने की प्रतीक्षा रहेगी!"
                    "mr" -> "नमस्कार! $productHint मध्ये आपल्या आवडीबद्दल धन्यवाद. आमच्या प्रीमियम उत्पादनांचे तुमच्या व्यवसायासाठी काय फायदे आहेत यावर आम्ही चर्चा करू इच्छितो. कृपया आम्हाला एक सुविधाजनक वेळ सांगा, किंवा कोणताही प्रश्न विचारा. तुमच्याकडून ऐकण्याची आतुरता!"
                    else -> "Thank you for your interest in $productHint! We'd love to discuss how our premium tea and coffee solutions can benefit your business. Please let us know a convenient time for a quick call, or feel free to ask any questions. Looking forward to hearing from you!"
                }
            prompt.contains("stale", ignoreCase = true) || prompt.contains("re-engage", ignoreCase = true) ->
                when (lang) {
                    "hi" -> "नमस्ते! हमने देखा कि आप $productHint में रुचि रखते थे। हमारे पास कुछ रोमांचक नए ऑफर और उत्पाद अपडेट हैं जो हम आपके साथ साझा करना चाहेंगे। क्या आप एक त्वरित चैट शेड्यूल करना चाहेंगे?"
                    "mr" -> "नमस्कार! आम्हाला दिसले की तुम्हाला $productHint मध्ये आस्ती होती. आमच्याकडे काही रोमांचक नवीन ऑफर आणि उत्पादन अपडेट आहेत जे आम्ही तुमच्याशी शेअर करू इच्छितो. तुम्ही एक जलद चैट शेड्यूल करू इच्छाल का?"
                    else -> "Hi! We noticed you were interested in $productHint earlier. We have some exciting new offers and product updates we'd love to share with you. Would you like to schedule a quick chat to explore how we can work together?"
                }
            prompt.contains("thank", ignoreCase = true) || prompt.contains("post_purchase", ignoreCase = true) ->
                when (lang) {
                    "hi" -> "धन्यवाद $productHint के लिए चुनने के लिए! हम आशा करते हैं कि आप अपनी खरीदारी का आनंद ले रहे हैं। आपकी प्रतिक्रिया हमारे लिए बहुत मायने रखती है। यदि आपको किसी भी चीज़ की आवश्यकता है, तो बेझिझक हमसे संपर्क करें। हमारे पास एक रेफरल प्रोग्राम भी है!"
                    "mr" -> "$productHint निवडल्याबद्दल धन्यवाद! आम्हाला आशा आहे की तुम्ही तुमच्या खरेदीचा आनंद घेत आहात. तुमचा अभिप्राय आमच्यासाठी खूप महत्त्वाचा आहे. काहीही हवे असल्यास आमच्याशी संपर्क साधा. आमच्याकडे रेफरल प्रोग्रामही आहे!"
                    else -> "Thank you for choosing $productHint! We hope you're enjoying your purchase. Your feedback means the world to us. If you need anything, don't hesitate to reach out. We also have a referral program - share with friends and earn rewards!"
                }
            prompt.contains("promotional", ignoreCase = true) ->
                when (lang) {
                    "hi" -> "केवल आपके लिए विशेष ऑफर! $productHint पर विशेष छूट प्राप्त करें। सीमित समय का ऑफर - आज ही हमसे संपर्क करें और अपने व्यवसाय के लिए बल्क प्राइसिंग के बारे में जानें!"
                    "mr" -> "फक्त तुमच्यासाठी विशेष ऑफर! $productHint वर विशेष सवलत मिळवा. मर्यादित काळाचा ऑफर - आजच आमच्याशी संपर्क साधा आणि बल्क प्राइसिंगबद्दल जाणून घ्या!"
                    else -> "Special offer just for you! Get exclusive deals on $productHint. Limited time offer - contact us today to learn more about our business partnership programs and bulk pricing!"
                }
            else ->
                when (lang) {
                    "hi" -> "आपकी रुचि के लिए धन्यवाद। हम $productHint की अपनी रेंज के साथ आपकी मदद करने के लिए तैयार हैं। कृपया हमें अपनी आवश्यकताएं बताएं और हम जल्द ही आपसे संपर्क करेंगे।"
                    "mr" -> "तुमच्या आवडीबद्दल धन्यवाद. आम्ही $productHint च्या आमच्या रेंजसह तुम्हाला मदत करण्यासाठी तयार आहोत. कृपया आम्हाला तुमच्या गरजा सांगा आणि आम्ही लवकरच तुमच्याशी संपर्क साधू."
                    else -> "Thank you for your interest. We'd be happy to help you with our range of $productHint. Please let us know your requirements and we'll get back to you shortly."
                }
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
