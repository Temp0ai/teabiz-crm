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
    val completedAt: Long? = null
)

enum class CampaignStatus(val displayName: String) {
    DRAFT("Draft"),
    SCHEDULED("Scheduled"),
    RUNNING("Running"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    PAUSED("Paused")
}
