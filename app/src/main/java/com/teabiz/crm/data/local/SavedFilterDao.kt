package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.SavedFilter
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedFilterDao {
    @Query("SELECT * FROM saved_filters ORDER BY createdAt DESC")
    fun getAllFilters(): Flow<List<SavedFilter>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFilter(filter: SavedFilter): Long

    @Delete
    suspend fun deleteFilter(filter: SavedFilter)
}
