package com.teabiz.crm.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "follow_ups",
    foreignKeys = [
        ForeignKey(
            entity = Lead::class,
            parentColumns = ["id"],
            childColumns = ["leadId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["leadId"])]
)
data class FollowUp(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val leadId: String,
    val message: String,
    val channel: String = "WHATSAPP",
    val status: String = "PENDING",
    val scheduledAt: Long? = null,
    val sentAt: Long? = null,
    val deliveredAt: Long? = null,
    val readAt: Long? = null,
    val isAiGenerated: Boolean = false,
    val aiModel: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class FollowUpChannel(val displayName: String) {
    WHATSAPP("WhatsApp"),
    SMS("SMS"),
    EMAIL("Email"),
    PHONE("Phone Call")
}

enum class FollowUpStatus(val displayName: String) {
    PENDING("Pending"),
    SENT("Sent"),
    DELIVERED("Delivered"),
    READ("Read"),
    FAILED("Failed")
}
