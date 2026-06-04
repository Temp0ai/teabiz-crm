package com.teabiz.crm.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {

    private var apiKey: String = ""

    fun configure(apiKey: String) {
        this.apiKey = apiKey
    }

    fun isConfigured(): Boolean = apiKey.isNotBlank()

    fun getApiKey(): String = apiKey

    suspend fun generateTrendyHashtags(
        productType: String,
        platform: String,
        count: Int = 30
    ): String {
        if (!isConfigured()) return ""

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val prompt = buildString {
                appendLine("You are the TOP social media marketing expert for Indian tea/coffee/vending machine businesses in 2025-2026.")
                appendLine()
                appendLine("Generate $count TRENDY, HIGH-REACH hashtags for a $platform post about: $productType")
                appendLine()
                appendLine("REQUIREMENTS - Hashtags must be:")
                appendLine("- Currently TRENDING on $platform in 2025-2026")
                appendLine("- High engagement and reach potential")
                appendLine("- Relevant to Indian tea/coffee/vending machine industry")
                appendLine("- Mix of viral, popular, medium, and niche hashtags")
                appendLine()
                appendLine("INCLUDE THESE CATEGORIES:")
                appendLine("1. TRENDING/VIRAL hashtags (currently popular in 2025)")
                appendLine("2. INDUSTRY SPECIFIC - tea machine, coffee machine, vending machine, premix")
                appendLine("3. PRODUCT SPECIFIC - $productType related tags")
                appendLine("4. INDIAN BUSINESS - chai, Indian tea culture, vending business India")
                appendLine("5. ENGAGEMENT BOOSTERS - high interaction tags")
                appendLine("6. LOCATION based - India, Indian cities")
                appendLine("7. LIFESTYLE - chai lovers, coffee addicts, tea time")
                appendLine("8. BUSINESS B2B - vending machine supplier, tea business, coffee business")
                appendLine()
                appendLine("PLATFORM RULES:")
                if (platform == "Instagram") {
                    appendLine("- Generate 25-30 hashtags")
                    appendLine("- Mix big (1M+) and niche (10K-100K) hashtags")
                    appendLine("- Include Reels trending hashtags")
                } else if (platform == "Facebook") {
                    appendLine("- Generate 8-12 highly targeted hashtags")
                    appendLine("- Facebook favors fewer relevant tags")
                    appendLine("- Focus on community and local business tags")
                } else {
                    appendLine("- Instagram: 25-30 hashtags")
                    appendLine("- Facebook: 10-12 hashtags")
                }
                appendLine()
                appendLine("OUTPUT FORMAT: Just hashtags, one per line, WITHOUT # symbol")
                appendLine("Do NOT include any explanation, just the hashtags.")
                appendLine()
                appendLine("GENERATE NOW:")
            }

            val response = model.generateContent(prompt)
            response.text ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun generateSmartMessage(
        productType: String,
        tone: String = "Professional",
        language: String = "English",
        messageType: String = "promotional"
    ): String {
        if (!isConfigured()) return ""

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val prompt = buildString {
                appendLine("You are a sales expert for Indian tea/coffee/vending machine business.")
                appendLine()
                appendLine("Generate a $tone WhatsApp message for: $productType")
                appendLine("Language: $language")
                appendLine("Message type: $messageType")
                appendLine()
                appendLine("The message should:")
                appendLine("- Be engaging and persuasive")
                appendLine("- Highlight product benefits and features")
                appendLine("- Include a clear call to action")
                appendLine("- Be concise (3-5 lines max for WhatsApp)")
                appendLine("- Feel personal, not spammy")
                if (language == "Hindi") appendLine("- Write in Hindi (Devanagari script)")
                if (language == "Marathi") appendLine("- Write in Marathi (Devanagari script)")
                appendLine()
                appendLine("MESSAGE:")
            }

            val response = model.generateContent(prompt)
            response.text?.trim() ?: ""
        } catch (e: Exception) {
            ""
        }
    }
}
