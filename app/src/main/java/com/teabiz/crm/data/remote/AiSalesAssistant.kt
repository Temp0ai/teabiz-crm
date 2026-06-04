package com.teabiz.crm.data.remote

import com.teabiz.crm.data.model.Lead
import com.google.ai.client.generativeai.GenerativeModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AiSalesAssistant @Inject constructor(
    private val geminiService: GeminiService
) {
    data class LeadAnalysis(
        val conversionProbability: Int,
        val bestTimeToContact: String,
        val estimatedLtv: Double,
        val churnRisk: Int,
        val aiInsights: String,
        val recommendedAction: String,
        val suggestedAssignment: String
    )

    data class AdCopy(
        val headline: String,
        val body: String,
        val callToAction: String,
        val platform: String
    )

    data class ContentSuggestion(
        val topic: String,
        val contentType: String,
        val bestTime: String,
        val caption: String,
        val hashtags: List<String>
    )

    suspend fun analyzeLead(lead: Lead): LeadAnalysis {
        if (!geminiService.isConfigured()) {
            return generateFallbackAnalysis(lead)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Analyze this lead for a tea/coffee business and provide detailed sales insights:
                
                Lead Info:
                - Name: ${lead.name}
                - Company: ${lead.company}
                - Products Interested: ${lead.productInterest.joinToString(", ")}
                - Source: ${lead.source}
                - Status: ${lead.status}
                - City: ${lead.city}
                - Deal Value: ₹${lead.dealValue}
                - Lead Score: ${lead.leadScore}
                - Last Follow-up: ${if (lead.lastFollowUpAt != null) java.text.SimpleDateFormat("dd MMM yyyy").format(java.util.Date(lead.lastFollowUpAt)) else "Never"}
                - Days since created: ${(System.currentTimeMillis() - lead.createdAt) / (1000 * 60 * 60 * 24)}
                
                Provide analysis in this EXACT format (one value per line):
                CONVERSION_PROBABILITY: (0-100 number)
                BEST_TIME: (e.g., "Morning 9-11 AM" or "Evening 7-9 PM")
                LTV: (estimated lifetime value in INR, assume 12 months)
                CHURN_RISK: (0-100, higher = more likely to churn)
                INSIGHTS: (2-3 key insights about this lead)
                ACTION: (recommended next action)
                ASSIGNMENT: (who should handle: "Sales Team" or "Senior Sales" or "Manager")
            """.trimIndent()

            val response = model.generateContent(prompt)
            val content = response.text ?: ""

            parseLeadAnalysis(content, lead)
        } catch (e: Exception) {
            generateFallbackAnalysis(lead)
        }
    }

    private fun parseLeadAnalysis(content: String, lead: Lead): LeadAnalysis {
        val lines = content.lines()
        var conversion = 50
        var bestTime = "Morning 9-11 AM"
        var ltv = lead.dealValue * 3
        var churn = 30
        var insights = "New lead, follow up promptly"
        var action = "Send product catalog"
        var assignment = "Sales Team"

        for (line in lines) {
            when {
                line.startsWith("CONVERSION_PROBABILITY:") -> conversion = line.substringAfter(":").trim().toIntOrNull() ?: 50
                line.startsWith("BEST_TIME:") -> bestTime = line.substringAfter(":").trim()
                line.startsWith("LTV:") -> ltv = line.substringAfter(":").trim().replace("₹", "").replace(",", "").toDoubleOrNull() ?: lead.dealValue * 3
                line.startsWith("CHURN_RISK:") -> churn = line.substringAfter(":").trim().toIntOrNull() ?: 30
                line.startsWith("INSIGHTS:") -> insights = line.substringAfter(":").trim()
                line.startsWith("ACTION:") -> action = line.substringAfter(":").trim()
                line.startsWith("ASSIGNMENT:") -> assignment = line.substringAfter(":").trim()
            }
        }

        return LeadAnalysis(
            conversionProbability = conversion.coerceIn(0, 100),
            bestTimeToContact = bestTime,
            estimatedLtv = ltv,
            churnRisk = churn.coerceIn(0, 100),
            aiInsights = insights,
            recommendedAction = action,
            suggestedAssignment = assignment
        )
    }

    private fun generateFallbackAnalysis(lead: Lead): LeadAnalysis {
        val daysSinceCreated = (System.currentTimeMillis() - lead.createdAt) / (1000 * 60 * 60 * 24)
        val hasCompany = lead.company.isNotBlank()
        val hasProducts = lead.productInterest.isNotEmpty()
        val hasDealValue = lead.dealValue > 0
        val recentFollowUp = lead.lastFollowUpAt != null && (System.currentTimeMillis() - lead.lastFollowUpAt!!) < 7 * 24 * 60 * 60 * 1000

        var score = 30
        if (hasCompany) score += 15
        if (hasProducts) score += 15
        if (hasDealValue) score += 20
        if (recentFollowUp) score += 10
        if (daysSinceCreated < 7) score += 10
        score = score.coerceIn(0, 100)

        val churn = when {
            daysSinceCreated > 30 -> 70
            daysSinceCreated > 14 -> 50
            daysSinceCreated > 7 -> 30
            else -> 15
        }

        val ltv = if (hasDealValue) lead.dealValue * 6 else 25000.0

        val bestTime = when {
            lead.city.contains("Mumbai", ignoreCase = true) -> "Evening 7-9 PM"
            lead.city.contains("Delhi", ignoreCase = true) -> "Morning 10 AM-12 PM"
            else -> "Morning 9-11 AM"
        }

        return LeadAnalysis(
            conversionProbability = score,
            bestTimeToContact = bestTime,
            estimatedLtv = ltv,
            churnRisk = churn,
            aiInsights = buildString {
                if (!hasCompany) append("Add company info for better scoring. ")
                if (!hasProducts) append("Product interest not specified. ")
                if (daysSinceCreated > 14) append("Lead is getting cold, re-engage quickly. ")
                if (recentFollowUp) append("Recent follow-up shows engagement. ")
                if (hasDealValue) append("Deal value of ₹${lead.dealValue} indicates serious intent. ")
                if (isBlank()) append("Lead looks promising, continue nurturing. ")
            },
            recommendedAction = when {
                churn > 60 -> "Urgent: Send special offer to re-engage"
                score > 70 -> "Send product demo invite"
                hasProducts -> "Send detailed product catalog"
                else -> "Send introductory message with pricing"
            },
            suggestedAssignment = if (score > 70 || lead.dealValue > 50000) "Senior Sales" else "Sales Team"
        )
    }

    suspend fun generateAdCopy(product: String, platform: String, tone: String = "Professional"): AdCopy {
        if (!geminiService.isConfigured()) {
            return generateFallbackAdCopy(product, platform)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Create a $tone $platform ad copy for: $product
                
                Requirements:
                - Headline: catchy, max 10 words
                - Body: compelling, 2-3 sentences
                - Call to Action: clear next step
                - Include pricing if relevant
                - Target: Indian B2B customers
                
                Format:
                HEADLINE: [your headline]
                BODY: [your body]
                CTA: [call to action]
            """.trimIndent()

            val response = model.generateContent(prompt)
            val content = response.text ?: ""

            parseAdCopy(content, product, platform)
        } catch (e: Exception) {
            generateFallbackAdCopy(product, platform)
        }
    }

    private fun parseAdCopy(content: String, product: String, platform: String): AdCopy {
        var headline = "Premium $product"
        var body = "Get the best quality at competitive prices"
        var cta = "Contact us now"

        for (line in content.lines()) {
            when {
                line.startsWith("HEADLINE:") -> headline = line.substringAfter(":").trim()
                line.startsWith("BODY:") -> body = line.substringAfter(":").trim()
                line.startsWith("CTA:") -> cta = line.substringAfter(":").trim()
            }
        }

        return AdCopy(headline, body, cta, platform)
    }

    private fun generateFallbackAdCopy(product: String, platform: String): AdCopy {
        return AdCopy(
            headline = "Premium $product - Best Prices!",
            body = "Get top-quality ${product.lowercase()} at competitive bulk prices. Trusted by 500+ businesses across India. FSSAI certified, guaranteed quality.",
            callToAction = "Get Free Quote →",
            platform = platform
        )
    }

    suspend fun generateContentCalendar(businessType: String = "Tea & Coffee"): List<ContentSuggestion> {
        if (!geminiService.isConfigured()) {
            return generateFallbackContentCalendar()
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Generate 7 content calendar ideas for a $businessType business for one week.
                
                Include variety:
                - Product showcase posts
                - Behind-the-scenes content
                - Customer testimonials
                - Educational content
                - Promotional offers
                - Trending topics
                - Engagement posts
                
                Format each as:
                TOPIC: [topic]
                TYPE: [Reel/Post/Story/Carousel]
                TIME: [best posting time]
                CAPTION: [engaging caption]
                HASHTAGS: [5 relevant hashtags, comma separated]
                
                Generate 7 posts (one per day):
            """.trimIndent()

            val response = model.generateContent(prompt)
            val content = response.text ?: ""

            parseContentCalendar(content)
        } catch (e: Exception) {
            generateFallbackContentCalendar()
        }
    }

    private fun parseContentCalendar(content: String): List<ContentSuggestion> {
        val suggestions = mutableListOf<ContentSuggestion>()
        val blocks = content.split(Regex("(?=TOPIC:)")).filter { it.contains("TOPIC:") }

        for (block in blocks) {
            var topic = ""
            var type = "Post"
            var time = "10 AM"
            var caption = ""
            var hashtags = emptyList<String>()

            for (line in block.lines()) {
                when {
                    line.startsWith("TOPIC:") -> topic = line.substringAfter(":").trim()
                    line.startsWith("TYPE:") -> type = line.substringAfter(":").trim()
                    line.startsWith("TIME:") -> time = line.substringAfter(":").trim()
                    line.startsWith("CAPTION:") -> caption = line.substringAfter(":").trim()
                    line.startsWith("HASHTAGS:") -> hashtags = line.substringAfter(":").trim().split(",").map { it.trim() }
                }
            }

            if (topic.isNotBlank()) {
                suggestions.add(ContentSuggestion(topic, type, time, caption, hashtags))
            }
        }

        return suggestions
    }

    private fun generateFallbackContentCalendar(): List<ContentSuggestion> {
        return listOf(
            ContentSuggestion("Product Monday - Tea Premix showcase", "Post", "10 AM", "Start your week with the perfect cup! ☕ Our premium tea premix delivers restaurant-quality chai at home.", listOf("teapremix", "chai", "mondaymotivation", "tealovers", "indianchai")),
            ContentSuggestion("Behind the Scenes - Our Factory", "Reel", "12 PM", "Ever wondered how your favorite chai premix is made? Take a peek behind the scenes!", listOf("behindthescenes", "manufacturing", "qualityfirst", "madeinindia", "teabusiness")),
            ContentSuggestion("Customer Success Story", "Carousel", "6 PM", "From a small café to 50+ outlets - see how @customer grew with our tea solutions.", listOf("customerstory", "successstory", "businessgrowth", "teabusiness", "cafelife")),
            ContentSuggestion("Tea vs Coffee - Which is Better?", "Story", "8 AM", "The eternal debate! What's your morning pick? ☕ vs 🍵", listOf("teavscoffee", "morningroutine", "coffeelovers", "tealovers", "debate")),
            ContentSuggestion("Flash Sale - 20% Off", "Post", "11 AM", "FLASH SALE! 🔥 Get 20% off on all vending machines. Limited time only!", listOf("flashsale", "discount", "vendingmachine", "deals", "limitedoffer")),
            ContentSuggestion("How to Make Perfect Masala Chai", "Reel", "7 PM", "The secret to perfect masala chai? It's all in the blend! Try our instant masala chai premix.", listOf("masalachai", "recipereel", "tearecipe", "instantchai", "foodie")),
            ContentSuggestion("Weekend Vibes with Chai", "Post", "9 AM", "Weekends are for slow sipping and good conversations. What's your chai story? 🍵", listOf("weekendvibes", "chai", "relax", "teatime", "goodvibes"))
        )
    }

    suspend fun detectColdLeads(leads: List<Lead>): List<LeadAnalysis> {
        val coldLeads = leads.filter { lead ->
            val daysSinceFollowUp = if (lead.lastFollowUpAt != null) {
                (System.currentTimeMillis() - lead.lastFollowUpAt) / (1000 * 60 * 60 * 24)
            } else {
                (System.currentTimeMillis() - lead.createdAt) / (1000 * 60 * 60 * 24)
            }
            daysSinceFollowUp > 14 || lead.status == "LOST"
        }

        return coldLeads.map { analyzeLead(it) }
    }

    suspend fun generateReEngagementMessage(lead: Lead, language: String = "English"): String {
        if (!geminiService.isConfigured()) {
            return generateFallbackReEngagement(lead, language)
        }

        return try {
            val model = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Generate a re-engagement message for this cold lead:
                
                Lead: ${lead.name} from ${lead.company}
                Products interested: ${lead.productInterest.joinToString(", ")}
                Last contact: ${if (lead.lastFollowUpAt != null) java.text.SimpleDateFormat("dd MMM yyyy").format(java.util.Date(lead.lastFollowUpAt)) else "Unknown"}
                
                Requirements:
                - Create urgency with limited-time offer
                - Mention new products or updates
                - Offer special discount
                - Language: $language
                - Keep it friendly, not pushy
                
                Message:
            """.trimIndent()

            val response = model.generateContent(prompt)
            response.text?.trim() ?: generateFallbackReEngagement(lead, language)
        } catch (e: Exception) {
            generateFallbackReEngagement(lead, language)
        }
    }

    private fun generateFallbackReEngagement(lead: Lead, language: String): String {
        return when (language) {
            "Hindi" -> "नमस्ते ${lead.name}! हमने देखा कि आपने ${lead.productInterest.joinToString(", ")} में रुचि दिखाई थी। हमारे पास कुछ रोमांचक नए ऑफर हैं - क्या आप एक त्वरित चैट करना चाहेंगे?"
            "Marathi" -> "नमस्कार ${lead.name}! तुम्ही ${lead.productInterest.joinToString(", ")} मध्ये रुची दाखवली होती. आमच्याकडे काही नवीन ऑफर आहेत - एक जलद चैट करू इच्छाल का?"
            else -> "Hi ${lead.name}! We noticed you were interested in ${lead.productInterest.joinToString(", ")} earlier. We have some exciting new offers and would love to reconnect. Would you like to schedule a quick chat?"
        }
    }
}
