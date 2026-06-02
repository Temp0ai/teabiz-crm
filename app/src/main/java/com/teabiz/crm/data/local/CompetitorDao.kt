package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.Competitor
import kotlinx.coroutines.flow.Flow

@Dao
interface CompetitorDao {
    @Query("SELECT * FROM competitors ORDER BY lastAnalyzed DESC")
    fun getAllCompetitors(): Flow<List<Competitor>>

    @Query("SELECT * FROM competitors WHERE id = :id")
    suspend fun getCompetitorById(id: Int): Competitor?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetitor(competitor: Competitor): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompetitors(competitors: List<Competitor>): List<Long>

    @Update
    suspend fun updateCompetitor(competitor: Competitor)

    @Delete
    suspend fun deleteCompetitor(competitor: Competitor)
}
