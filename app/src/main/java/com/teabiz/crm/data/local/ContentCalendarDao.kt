package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.ContentCalendar
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentCalendarDao {
    @Query("SELECT * FROM content_calendar ORDER BY createdAt DESC")
    fun getAllContent(): Flow<List<ContentCalendar>>

    @Query("SELECT * FROM content_calendar WHERE status = :status ORDER BY scheduledAt ASC")
    fun getContentByStatus(status: String): Flow<List<ContentCalendar>>

    @Query("SELECT * FROM content_calendar WHERE platform = :platform ORDER BY createdAt DESC")
    fun getContentByPlatform(platform: String): Flow<List<ContentCalendar>>

    @Query("SELECT * FROM content_calendar WHERE scheduledAt IS NOT NULL AND scheduledAt <= :currentTime AND status = 'SCHEDULED'")
    suspend fun getDueContent(currentTime: Long): List<ContentCalendar>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContent(content: ContentCalendar): Long

    @Update
    suspend fun updateContent(content: ContentCalendar)

    @Delete
    suspend fun deleteContent(content: ContentCalendar)
}
