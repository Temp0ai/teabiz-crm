package com.teabiz.crm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.teabiz.crm.data.local.Converters

@Entity(tableName = "campaigns")
@TypeConverters(Converters::class)
data class Campaign(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val targetCategory: String = "",
    val targetClientType: String = "",
    val messageTemplate: String = "",
    val scheduledAt: Long? = null,
    val status: String = "DRAFT",
    val sentCount: Int = 0,
    val failedCount: Int = 0,
    val totalRecipients: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val batchSize: Int = 100,
    val mediaUri: String = "",
    val mediaType: String = "",
    val currentBatch: Int = 0,
    val totalBatches: Int = 0,
    val currentBatchProgress: String = "",
    val sentContacts: String = "",
    val remainingContacts: String = "",
    val targetLeadScore: String = "",
    val targetPriority: String = "",
    val targetSource: String = "",
    val targetCity: String = "",
    val isRecurring: Boolean = false,
    val recurringInterval: String = "",
    val templateId: String = "",
    val followUpAfterHours: Int = 0,
    val followUpMessage: String = "",
    val abTestMessage: String = "",
    val abTestEnabled: Boolean = false,
    val blacklistedContacts: String = "",
    val repliedContacts: String = "",
    val readCount: Int = 0,
    val repliedCount: Int = 0,
    val convertedCount: Int = 0,
    val bestTimeToSend: String = "",
    val language: String = "English",
    val tone: String = "Professional"
)

@Entity(tableName = "campaign_templates")
data class CampaignTemplate(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val messageTemplate: String,
    val category: String = "",
    val tone: String = "Professional",
    val language: String = "English",
    val createdAt: Long = System.currentTimeMillis(),
    val useCount: Int = 0
)

@Entity(tableName = "campaign_blacklist")
data class BlacklistedContact(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val phone: String,
    val name: String = "",
    val reason: String = "",
    val blacklistedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "campaign_analytics")
data class CampaignAnalytics(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val campaignId: String,
    val sentAt: Long,
    val readAt: Long? = null,
    val repliedAt: Long? = null,
    val convertedAt: Long? = null,
    val contactPhone: String,
    val contactName: String,
    val messageType: String = "A"
)

enum class CampaignStatus(val displayName: String) {
    DRAFT("Draft"),
    SCHEDULED("Scheduled"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    PAUSED("Paused"),
    RECURRING("Recurring")
}
