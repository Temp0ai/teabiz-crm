package com.teabiz.crm.data.remote

import android.content.Context
import android.net.Uri
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhatsAppCatalogOfferSender @Inject constructor(
    @ApplicationContext private val context: Context,
    private val geminiService: GeminiService,
    private val okHttpClient: OkHttpClient
) {

    data class CatalogProduct(
        val name: String,
        val price: String,
        val description: String,
        val imageUrl: String = ""
    )

    data class OfferMessage(
        val title: String,
        val message: String,
        val catalogLink: String,
        val gmbAddress: String,
        val phoneNumber: String,
        val discount: String = "",
        val validity: String = ""
    )

    data class BulkSendJob(
        val totalContacts: Int,
        var sentCount: Int = 0,
        var failedCount: Int = 0,
        val messages: MutableList<String> = mutableListOf(),
        var isRunning: Boolean = false,
        var isPaused: Boolean = false
    )

    suspend fun fetchCatalog(businessPhone: String): List<CatalogProduct> {
        return try {
            val cleanPhone = businessPhone.replace(Regex("[^0-9]"), "")
            val url = "https://wa.me/c/$cleanPhone"
            
            val request = Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()

            val response = okHttpClient.newCall(request).execute()
            val html = response.body?.string() ?: ""

            val products = mutableListOf<CatalogProduct>()
            
            val productPattern = Regex("\"catalog_product_name\":\"([^\"]+)\"|\"name\":\"([^\"]+)\"")
            val pricePattern = Regex("\"price\":\"([^\"]+)\"|\"formatted_price\":\"([^\"]+)\"")
            val descPattern = Regex("\"description\":\"([^\"]+)\"|\"catalog_product_description\":\"([^\"]+)\"")
            
            val names = productPattern.findAll(html).mapNotNull { 
                it.groupValues[1].ifBlank { it.groupValues[2] } 
            }.toList()
            
            val prices = pricePattern.findAll(html).mapNotNull { 
                it.groupValues[1].ifBlank { it.groupValues[2] } 
            }.toList()
            
            val descriptions = descPattern.findAll(html).mapNotNull { 
                it.groupValues[1].ifBlank { it.groupValues[2] } 
            }.toList()

            names.forEachIndexed { index, name ->
                products.add(
                    CatalogProduct(
                        name = name,
                        price = prices.getOrElse(index) { "Contact for price" },
                        description = descriptions.getOrElse(index) { "" }
                    )
                )
            }

            if (products.isEmpty()) {
                products.addAll(getDefaultProducts())
            }

            products
        } catch (e: Exception) {
            getDefaultProducts()
        }
    }

    private fun getDefaultProducts(): List<CatalogProduct> {
        return listOf(
            CatalogProduct("Tea Premix", "₹180/kg", "Premium instant tea premix"),
            CatalogProduct("Coffee Premix", "₹220/kg", "Rich instant coffee premix"),
            CatalogProduct("Tea Vending Machine", "₹35,000", "2-option automatic tea machine"),
            CatalogProduct("Coffee Vending Machine", "₹45,000", "3-option coffee machine"),
            CatalogProduct("Tea Powder", "₹150/kg", "Premium CTC tea powder"),
            CatalogProduct("Coffee Powder", "₹280/kg", "Fine ground coffee powder")
        )
    }

    suspend fun generateOffer(
        product: String,
        discount: String = "10%",
        catalogLink: String = "",
        gmbAddress: String = "",
        phoneNumber: String = "",
        language: String = "English",
        tone: String = "Professional"
    ): OfferMessage {
        if (!geminiService.isConfigured()) {
            return generateDefaultOffer(product, discount, catalogLink, gmbAddress, phoneNumber)
        }

        return try {
            val model = GenerativeModel(
                modelName = geminiService.getSelectedModel(),
                apiKey = geminiService.getApiKey()
            )

            val prompt = """
                Create a WhatsApp promotional message for: $product
                
                Offer: $discount discount
                Catalog: $catalogLink
                Store Address: $gmbAddress
                Contact: $phoneNumber
                Language: $language
                Tone: $tone
                
                Requirements:
                - Start with eye-catching emoji
                - Mention the product and offer
                - Include catalog link for browsing
                - Include store address
                - Include contact number
                - Add urgency (limited time)
                - End with call to action
                - Use WhatsApp formatting (*bold*, _italic_)
                
                Format the complete message.
            """.trimIndent()

            val response = model.generateContent(prompt)
            val message = response.text ?: ""

            OfferMessage(
                title = "Special Offer - $product",
                message = message,
                catalogLink = catalogLink,
                gmbAddress = gmbAddress,
                phoneNumber = phoneNumber,
                discount = discount
            )
        } catch (e: Exception) {
            generateDefaultOffer(product, discount, catalogLink, gmbAddress, phoneNumber)
        }
    }

    private fun generateDefaultOffer(
        product: String,
        discount: String,
        catalogLink: String,
        gmbAddress: String,
        phoneNumber: String
    ): OfferMessage {
        val message = """
            🎉 *SPECIAL OFFER!* 🎉
            
            *$product* at *$discount OFF!*
            
            ✅ Premium Quality
            ✅ Best Price Guaranteed
            ✅ Pan India Delivery
            
            📱 *View Full Catalog:*
            $catalogLink
            
            📍 *Visit Us:*
            $gmbAddress
            
            📞 *Order Now:*
            $phoneNumber
            
            ⏰ *Limited Time Offer!*
            
            *Message us "ORDER" to place your order!*
        """.trimIndent()

        return OfferMessage(
            title = "Special Offer - $product",
            message = message,
            catalogLink = catalogLink,
            gmbAddress = gmbAddress,
            phoneNumber = phoneNumber,
            discount = discount
        )
    }

    suspend fun generateBulkMessages(
        contacts: List<Pair<String, String>>,
        product: String,
        discount: String,
        catalogLink: String,
        gmbAddress: String,
        phoneNumber: String,
        language: String = "English",
        personalized: Boolean = true
    ): List<Triple<String, String, String>> {
        val results = mutableListOf<Triple<String, String, String>>()
        
        val baseOffer = generateOffer(product, discount, catalogLink, gmbAddress, phoneNumber, language)
        
        for ((name, phone) in contacts) {
            val message = if (personalized) {
                baseOffer.message
                    .replace("{name}", name)
                    .replace("{phone}", phone)
            } else {
                baseOffer.message
            }
            
            results.add(Triple(name, phone, message))
        }
        
        return results
    }

    fun createWhatsAppLink(phone: String, message: String): String {
        val cleanPhone = phone.replace(Regex("[^0-9]"), "")
        val encodedMessage = Uri.encode(message)
        return "https://wa.me/$cleanPhone?text=$encodedMessage"
    }
}
