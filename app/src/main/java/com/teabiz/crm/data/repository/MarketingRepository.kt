package com.teabiz.crm.data.repository

import com.teabiz.crm.data.local.AppDatabase
import com.teabiz.crm.data.model.*
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketingRepository @Inject constructor(
    private val database: AppDatabase
) {
    private val seoKeywordDao = database.seoKeywordDao()
    private val competitorDao = database.competitorDao()
    private val contentCalendarDao = database.contentCalendarDao()
    private val gmbPostDao = database.gmbPostDao()
    private val savedFilterDao = database.savedFilterDao()
    private val appSettingsDao = database.appSettingsDao()

    // SEO Keywords
    fun getAllKeywords(): Flow<List<SeoKeyword>> = seoKeywordDao.getAllKeywords()
    fun searchKeywords(query: String): Flow<List<SeoKeyword>> = seoKeywordDao.searchKeywords(query)
    suspend fun insertKeyword(keyword: SeoKeyword): Long = seoKeywordDao.insertKeyword(keyword)
    suspend fun insertKeywords(keywords: List<SeoKeyword>): List<Long> = seoKeywordDao.insertKeywords(keywords)
    suspend fun updateKeyword(keyword: SeoKeyword) = seoKeywordDao.updateKeyword(keyword)
    suspend fun deleteKeyword(keyword: SeoKeyword) = seoKeywordDao.deleteKeyword(keyword)
    suspend fun deleteAllKeywords() = seoKeywordDao.deleteAllKeywords()

    // Competitors
    fun getAllCompetitors(): Flow<List<Competitor>> = competitorDao.getAllCompetitors()
    suspend fun getCompetitorById(id: Int): Competitor? = competitorDao.getCompetitorById(id)
    suspend fun insertCompetitor(competitor: Competitor): Long = competitorDao.insertCompetitor(competitor)
    suspend fun updateCompetitor(competitor: Competitor) = competitorDao.updateCompetitor(competitor)
    suspend fun deleteCompetitor(competitor: Competitor) = competitorDao.deleteCompetitor(competitor)

    // Content Calendar
    fun getAllContent(): Flow<List<ContentCalendar>> = contentCalendarDao.getAllContent()
    fun getContentByStatus(status: String): Flow<List<ContentCalendar>> = contentCalendarDao.getContentByStatus(status)
    fun getContentByPlatform(platform: String): Flow<List<ContentCalendar>> = contentCalendarDao.getContentByPlatform(platform)
    suspend fun getDueContent(currentTime: Long): List<ContentCalendar> = contentCalendarDao.getDueContent(currentTime)
    suspend fun insertContent(content: ContentCalendar): Long = contentCalendarDao.insertContent(content)
    suspend fun updateContent(content: ContentCalendar) = contentCalendarDao.updateContent(content)
    suspend fun deleteContent(content: ContentCalendar) = contentCalendarDao.deleteContent(content)

    // GMB Posts
    fun getAllGmbPosts(): Flow<List<GmbPost>> = gmbPostDao.getAllPosts()
    suspend fun getGmbPostById(id: Int): GmbPost? = gmbPostDao.getPostById(id)
    suspend fun insertGmbPost(post: GmbPost): Long = gmbPostDao.insertPost(post)
    suspend fun updateGmbPost(post: GmbPost) = gmbPostDao.updatePost(post)
    suspend fun deleteGmbPost(post: GmbPost) = gmbPostDao.deletePost(post)

    // Saved Filters
    fun getAllFilters(): Flow<List<SavedFilter>> = savedFilterDao.getAllFilters()
    suspend fun insertFilter(filter: SavedFilter): Long = savedFilterDao.insertFilter(filter)
    suspend fun deleteFilter(filter: SavedFilter) = savedFilterDao.deleteFilter(filter)

    // App Settings
    fun getAllSettings(): Flow<List<AppSetting>> = appSettingsDao.getAllSettings()
    suspend fun getSetting(key: String): String? = appSettingsDao.getSetting(key)
    fun getSettingFlow(key: String): Flow<String?> = appSettingsDao.getSettingFlow(key)
    suspend fun setSetting(key: String, value: String) = appSettingsDao.setSetting(AppSetting(key, value))
    suspend fun deleteSetting(key: String) = appSettingsDao.deleteSetting(key)
}
