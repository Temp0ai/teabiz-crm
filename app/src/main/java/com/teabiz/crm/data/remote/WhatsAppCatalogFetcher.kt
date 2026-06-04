package com.teabiz.crm.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppCatalogFetcher @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    data class CatalogProduct(
        val name: String,
        val description: String = "",
        val price: String = "",
        val imageUrl: String = "",
        val category: String = ""
    )

    suspend fun fetchCatalog(phoneNumber: String): List<CatalogProduct> {
        return withContext(Dispatchers.IO) {
            try {
                val cleanPhone = phoneNumber.replace(Regex("[^0-9]"), "")
                val url = "https://wa.me/c/$cleanPhone"

                val request = Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                    .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                    .addHeader("Accept-Language", "en-US,en;q=0.5")
                    .get()
                    .build()

                val response = okHttpClient.newCall(request).execute()
                val html = response.body?.string() ?: ""

                parseCatalogFromHtml(html)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun parseCatalogFromHtml(html: String): List<CatalogProduct> {
        val products = mutableListOf<CatalogProduct>()

        val productPatterns = listOf(
            Regex("\"name\"\\s*:\\s*\"([^\"]+)\""),
            Regex("\"title\"\\s*:\\s*\"([^\"]+)\""),
            Regex("\"description\"\\s*:\\s*\"([^\"]+)\""),
            Regex("\"price\"\\s*:\\s*\"([^\"]+)\""),
            Regex("\"image_url\"\\s*:\\s*\"([^\"]+)\""),
            Regex("\"category\"\\s*:\\s*\"([^\"]+)\"")
        )

        val nameMatches = productPatterns[0].findAll(html).map { it.groupValues[1] }.toList()
        val titleMatches = productPatterns[1].findAll(html).map { it.groupValues[1] }.toList()
        val descMatches = productPatterns[2].findAll(html).map { it.groupValues[1] }.toList()
        val priceMatches = productPatterns[3].findAll(html).map { it.groupValues[1] }.toList()
        val imageMatches = productPatterns[4].findAll(html).map { it.groupValues[1] }.toList()
        val categoryMatches = productPatterns[5].findAll(html).map { it.groupValues[1] }.toList()

        val maxsize = maxOf(nameMatches.size, titleMatches.size, 0)

        for (i in 0 until maxsize) {
            val name = nameMatches.getOrElse(i) { titleMatches.getOrElse(i) { "" } }
            if (name.isNotBlank() && name.length > 2) {
                products.add(
                    CatalogProduct(
                        name = name,
                        description = descMatches.getOrElse(i) { "" },
                        price = priceMatches.getOrElse(i) { "" },
                        imageUrl = imageMatches.getOrElse(i) { "" },
                        category = categoryMatches.getOrElse(i) { "" }
                    )
                )
            }
        }

        return products.distinctBy { it.name }
    }

    suspend fun generateMessagesFromCatalog(
        products: List<CatalogProduct>,
        tone: String = "Professional",
        language: String = "English",
        geminiService: GeminiService
    ): Map<String, String> {
        val messages = mutableMapOf<String, String>()

        for (product in products) {
            val message = if (geminiService.isConfigured()) {
                try {
                    geminiService.generateSmartMessage(
                        productType = "${product.name} - ${product.description}",
                        tone = tone,
                        language = language,
                        messageType = "promotional"
                    )
                } catch (e: Exception) {
                    generateFallbackMessage(product, tone, language)
                }
            } else {
                generateFallbackMessage(product, tone, language)
            }
            messages[product.name] = message.ifBlank { generateFallbackMessage(product, tone, language) }
        }

        return messages
    }

    private fun generateFallbackMessage(product: CatalogProduct, tone: String, language: String): String {
        val priceText = if (product.price.isNotBlank()) " at ${product.price}" else ""
        val descText = if (product.description.isNotBlank()) "\n${product.description}" else ""

        return when {
            language == "Hindi" -> """
                ${product.name} - विशेष ऑफर!$priceText
                
                ${if (product.description.isNotBlank()) product.description else "प्रीमियम क्वालिटी"}
                
                ✅ बेस्ट क्वालिटी
                ✅ कॉम्पिटिटिव प्राइस
                ✅ बल्क ऑर्डर पर डिस्काउंट
                
                ऑर्डर करने के लिए अभी संपर्क करें! 📞
            """.trimIndent()

            language == "Marathi" -> """
                ${product.name} - विशेष ऑफर!$priceText
                
                ${if (product.description.isNotBlank()) product.description else "प्रीमियम क्वालिटी"}
                
                ✅ बेस्ट क्वालिटी
                ✅ कॉम्पिटिटिव प्राइस
                ✅ बल्क ऑर्डरवर डिस्काउंट
                
                ऑर्डर करण्यासाठी आत्ताच संपर्क साधा! 📞
            """.trimIndent()

            else -> """
                ${product.name} - Special Offer!$priceText
                $descText
                
                ✅ Premium Quality
                ✅ Competitive Pricing
                ✅ Bulk Order Discounts
                
                Contact us now to order! 📞
            """.trimIndent()
        }.trimIndent()
    }
}
