package com.teabiz.crm.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teabiz.crm.data.model.*
import com.teabiz.crm.data.remote.AiService
import com.teabiz.crm.data.remote.SEOService
import com.teabiz.crm.data.repository.MarketingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarketingViewModel @Inject constructor(
    private val marketingRepository: MarketingRepository,
    private val seoService: SEOService,
    private val aiService: AiService
) : ViewModel() {

    val keywords: StateFlow<List<SeoKeyword>> = marketingRepository.getAllKeywords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val competitors: StateFlow<List<Competitor>> = marketingRepository.getAllCompetitors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentCalendar: StateFlow<List<ContentCalendar>> = marketingRepository.getAllContent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gmbPosts: StateFlow<List<GmbPost>> = marketingRepository.getAllGmbPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isResearching = MutableStateFlow(false)
    val isResearching: StateFlow<Boolean> = _isResearching

    private val _generatedHashtags = MutableStateFlow<List<String>>(emptyList())
    val generatedHashtags: StateFlow<List<String>> = _generatedHashtags

    private val _researchResults = MutableStateFlow<List<KeywordResearchResult>>(emptyList())
    val researchResults: StateFlow<List<KeywordResearchResult>> = _researchResults

    private val _competitorAnalysis = MutableStateFlow<CompetitorAnalysisResult?>(null)
    val competitorAnalysis: StateFlow<CompetitorAnalysisResult?> = _competitorAnalysis

    fun researchKeywords(keywords: List<String>) {
        viewModelScope.launch {
            _isResearching.value = true
            try {
                val results = keywords.map { keyword ->
                    seoService.getKeywordData(keyword)
                }
                _researchResults.value = results

                // Save to database
                val seoKeywords = results.map { result ->
                    SeoKeyword(
                        keyword = result.keyword,
                        searchVolume = result.searchVolume,
                        competition = result.competition,
                        trend = result.trend,
                        relatedKeywords = result.relatedKeywords
                    )
                }
                marketingRepository.insertKeywords(seoKeywords)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isResearching.value = false
            }
        }
    }

    fun analyzeCompetitor(name: String, website: String) {
        viewModelScope.launch {
            _isResearching.value = true
            try {
                val response = aiService.analyzeCompetitor(name, website)
                val content = response.content

                val sections = content.split("**").filter { it.isNotBlank() }
                val sectionMap = mutableMapOf<String, List<String>>()
                var currentKey = ""
                for (section in sections) {
                    val trimmed = section.trim()
                    if (trimmed.endsWith(":") || trimmed.endsWith(" ")) {
                        currentKey = trimmed.removeSuffix(":").removeSuffix(" ").lowercase()
                    } else if (currentKey.isNotEmpty()) {
                        val items = trimmed.lines().map { it.trim().removePrefix("- ").removePrefix("* ").removePrefix("• ") }.filter { it.isNotBlank() }
                        sectionMap[currentKey] = items
                        currentKey = ""
                    }
                }

                val strengths = sectionMap.entries.find { it.key.contains("strength") }?.value ?: listOf("See AI analysis below")
                val weaknesses = sectionMap.entries.find { it.key.contains("weakness") }?.value ?: emptyList()
                val opportunities = sectionMap.entries.find { it.key.contains("opportunit") }?.value ?: emptyList()
                val positioning = sectionMap.entries.find { it.key.contains("position") }?.value ?: emptyList()

                val analysis = CompetitorAnalysisResult(
                    competitorName = name,
                    website = website,
                    estimatedTraffic = 0,
                    topKeywords = emptyList(),
                    strengths = strengths,
                    weaknesses = weaknesses,
                    opportunities = opportunities + positioning
                )
                _competitorAnalysis.value = analysis

                val competitor = Competitor(
                    name = name,
                    website = website,
                    lastAnalyzed = System.currentTimeMillis()
                )
                marketingRepository.insertCompetitor(competitor)
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isResearching.value = false
            }
        }
    }

    fun generateContent(platform: String, keyword: String, audience: String) {
        viewModelScope.launch {
            try {
                val response = aiService.generateSEOContent(keyword, "social_media_post", audience)
                val content = ContentCalendar(
                    platform = platform,
                    contentType = "AI Generated",
                    caption = response.content,
                    targetAudience = audience,
                    status = "DRAFT"
                )
                marketingRepository.insertContent(content)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun generateHashtags(productType: String, platform: String) {
        viewModelScope.launch {
            try {
                val response = aiService.generateHashtags(productType, platform)
                val hashtags = response.content.lines()
                    .map { it.trim().removePrefix("#") }
                    .filter { it.isNotBlank() }
                _generatedHashtags.value = hashtags
            } catch (e: Exception) {
                _generatedHashtags.value = emptyList()
            }
        }
    }

    fun addCompetitor(competitor: Competitor) {
        viewModelScope.launch {
            marketingRepository.insertCompetitor(competitor)
        }
    }

    fun deleteCompetitor(competitor: Competitor) {
        viewModelScope.launch {
            marketingRepository.deleteCompetitor(competitor)
        }
    }

    fun addContent(content: ContentCalendar) {
        viewModelScope.launch {
            marketingRepository.insertContent(content)
        }
    }

    fun updateContent(content: ContentCalendar) {
        viewModelScope.launch {
            marketingRepository.updateContent(content)
        }
    }

    fun deleteContent(content: ContentCalendar) {
        viewModelScope.launch {
            marketingRepository.deleteContent(content)
        }
    }

    fun clearAnalysis() {
        _competitorAnalysis.value = null
    }

    fun deleteKeyword(keyword: SeoKeyword) {
        viewModelScope.launch {
            marketingRepository.deleteKeyword(keyword)
        }
    }

    fun addGmbPost(post: GmbPost) {
        viewModelScope.launch {
            marketingRepository.insertGmbPost(post)
        }
    }

    fun generateGmbResponse(review: String, rating: Int, businessName: String) {
        viewModelScope.launch {
            try {
                aiService.generateReviewResponse(review, rating, businessName)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
