package com.teabiz.crm.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.teabiz.crm.data.local.Converters

@Entity(tableName = "seo_keywords")
@TypeConverters(Converters::class)
data class SeoKeyword(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val keyword: String,
    val searchVolume: Int = 0,
    val competition: String = "LOW",
    val trend: String = "",
    val relatedKeywords: List<String> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "competitors")
@TypeConverters(Converters::class)
data class Competitor(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val website: String = "",
    val keywords: List<String> = emptyList(),
    val estimatedTraffic: Int = 0,
    val topProducts: List<String> = emptyList(),
    val pricingStrategy: String = "",
    val lastAnalyzed: Long = System.currentTimeMillis()
)

@Entity(tableName = "content_calendar")
@TypeConverters(Converters::class)
data class ContentCalendar(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val platform: String,
    val contentType: String,
    val caption: String = "",
    val mediaUrl: String? = null,
    val hashtags: List<String> = emptyList(),
    val targetAudience: String = "",
    val scheduledAt: Long? = null,
    val postedAt: Long? = null,
    val status: String = "DRAFT",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "gmb_posts")
@TypeConverters(Converters::class)
data class GmbPost(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val postId: String = "",
    val summary: String = "",
    val topicType: String = "",
    val language: String = "en",
    val mediaUrls: List<String> = emptyList(),
    val callToAction: String = "",
    val status: String = "DRAFT",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_filters")
@TypeConverters(Converters::class)
data class SavedFilter(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val filterJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String,
    val updatedAt: Long = System.currentTimeMillis()
)
