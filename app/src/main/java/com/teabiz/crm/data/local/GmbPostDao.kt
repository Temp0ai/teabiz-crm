package com.teabiz.crm.data.local

import androidx.room.*
import com.teabiz.crm.data.model.GmbPost
import kotlinx.coroutines.flow.Flow

@Dao
interface GmbPostDao {
    @Query("SELECT * FROM gmb_posts ORDER BY createdAt DESC")
    fun getAllPosts(): Flow<List<GmbPost>>

    @Query("SELECT * FROM gmb_posts WHERE id = :id")
    suspend fun getPostById(id: Int): GmbPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: GmbPost): Long

    @Update
    suspend fun updatePost(post: GmbPost)

    @Delete
    suspend fun deletePost(post: GmbPost)
}
