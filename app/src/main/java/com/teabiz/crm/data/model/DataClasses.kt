package com.teabiz.crm.data.model

data class WhatsAppMessage(
    val id: String = "",
    val recipientPhone: String,
    val message: String,
    val mediaUrl: String? = null,
    val status: String = "PENDING",
    val sentAt: Long? = null,
    val deliveredAt: Long? = null,
    val readAt: Long? = null
)

data class WhatsAppCatalogItem(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val currency: String = "INR",
    val imageUrl: String? = null,
    val category: String,
    val availability: String = "IN_STOCK",
    val whatsappShareUrl: String = ""
)

data class AIResponse(
    val content: String,
    val model: String,
    val tokensUsed: Int = 0,
    val generatedAt: Long = System.currentTimeMillis()
)

data class KeywordResearchResult(
    val keyword: String,
    val searchVolume: Int,
    val competition: String,
    val cpc: Double = 0.0,
    val trend: String = "stable",
    val relatedKeywords: List<String> = emptyList(),
    val contentSuggestions: List<String> = emptyList()
)

data class CompetitorAnalysisResult(
    val competitorName: String,
    val website: String,
    val estimatedTraffic: Int,
    val topKeywords: List<String>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val opportunities: List<String>,
    val analyzedAt: Long = System.currentTimeMillis()
)

data class ImportResult(
    val imported: Int,
    val duplicates: Int,
    val failed: Int
)
