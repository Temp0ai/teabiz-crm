package com.teabiz.crm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.teabiz.crm.data.local.Converters

@Entity(tableName = "leads")
@TypeConverters(Converters::class)
data class Lead(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val email: String = "",
    val phone: String = "",
    val company: String = "",
    val productInterest: List<String> = emptyList(),
    val clientType: String = "",
    val message: String = "",
    val city: String = "",
    val country: String = "",
    val source: String = "MANUAL",
    val status: String = "NEW",
    val tags: List<String> = emptyList(),
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val lastFollowUpAt: Long? = null,
    val whatsappOptIn: Boolean = true,
    val leadScore: Int = 0,
    val priority: String = "NORMAL",
    val nextFollowUpAt: Long? = null,
    val dealValue: Double = 0.0,
    val assignedTo: String = "",
    val conversionProbability: Int = 0,
    val bestTimeToContact: String = "",
    val estimatedLtv: Double = 0.0,
    val churnRisk: Int = 0,
    val aiInsights: String = "",
    val lastAiAnalysisAt: Long? = null,
    val responsePattern: String = ""
)

@Entity(tableName = "lead_activities")
@TypeConverters(Converters::class)
data class LeadActivity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val leadId: String,
    val type: String,
    val description: String,
    val timestamp: Long = System.currentTimeMillis(),
    val outcome: String = ""
)

enum class LeadSource(val displayName: String) {
    GMAIL("Gmail"),
    WHATSAPP("WhatsApp"),
    GMB("Google My Business"),
    MANUAL("Manual"),
    EXCEL("Excel Import"),
    WEBSITE("Website"),
    REFERRAL("Referral"),
    PHONE("Phone Call"),
    INDIAMART("IndiaMART"),
    KAGGLE("Kaggle"),
    JSTDL("JSTDL"),
    JUSTDIAL("Just Dial"),
    DEALER("Dealer"),
    DISTRIBUTOR("Distributor"),
    MACHINE("Machine"),
    ORDER("Order")
}

enum class LeadStatus(val displayName: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    FOLLOW_UP("Follow-up"),
    NEGOTIATION("Negotiation"),
    CONVERTED("Converted"),
    LOST("Lost")
}

enum class ProductCategory(val displayName: String) {
    TEA_PREMIX("Tea Premix"),
    COFFEE_PREMIX("Coffee Premix"),
    NESCAFE_PREMIX("Nescafe Premix"),
    TEA_MACHINE("Tea Machine"),
    COFFEE_MACHINE("Coffee Machine"),
    NESCAFE_MACHINE("Nescafe Machine"),
    ACCESSORIES("Accessories"),
    OTHER("Other")
}

enum class ClientType(val displayName: String) {
    SOCIETY("Society/Housing"),
    CAFE("Cafe"),
    RESTAURANT("Restaurant"),
    OFFICE("Office/Corporate"),
    RETAIL_STALL("Retail Stall"),
    MANUFACTURING("Manufacturing"),
    WHOLESALER("Wholesaler"),
    HOTEL("Hotel"),
    HOSPITAL("Hospital"),
    SCHOOL("School/Educational"),
    OTHER("Other")
}

enum class GeographicScope(val displayName: String) {
    LOCAL("Local"),
    NATIONAL("National"),
    INTERNATIONAL("International")
}

enum class LeadPriority(val displayName: String) {
    HOT("Hot"),
    WARM("Warm"),
    NORMAL("Normal"),
    COLD("Cold")
}
