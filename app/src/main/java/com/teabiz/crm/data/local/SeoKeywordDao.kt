package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.SeoKeyword
import kotlinx.coroutines.flow.Flow

@Dao
interface SeoKeywordDao {
    @Query("SELECT * FROM seo_keywords ORDER BY searchVolume DESC")
    fun getAllKeywords(): Flow<List<SeoKeyword>>

    @Query("SELECT * FROM seo_keywords WHERE keyword LIKE '%' || :query || '%'")
    fun searchKeywords(query: String): Flow<List<SeoKeyword>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: SeoKeyword): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeywords(keywords: List<SeoKeyword>): List<Long>

    @Update
    suspend fun updateKeyword(keyword: SeoKeyword)

    @Delete
    suspend fun deleteKeyword(keyword: SeoKeyword)

    @Query("DELETE FROM seo_keywords")
    suspend fun deleteAllKeywords()
}
