package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.ImportSession
import kotlinx.coroutines.flow.Flow

@Dao
interface ImportSessionDao {
    @Query("SELECT * FROM import_sessions ORDER BY startedAt DESC")
    fun getAllImportSessions(): Flow<List<ImportSession>>

    @Query("SELECT * FROM import_sessions WHERE id = :sessionId")
    suspend fun getImportSessionById(sessionId: String): ImportSession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImportSession(session: ImportSession): Long

    @Update
    suspend fun updateImportSession(session: ImportSession)

    @Query("DELETE FROM import_sessions WHERE id = :sessionId")
    suspend fun deleteImportSession(sessionId: String)
}
