package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) for managing keyword alert entities in Room database.
 */
@Dao
interface KeywordDao {

    @Query("SELECT * FROM keyword_alerts ORDER BY createdAt DESC")
    fun getAllKeywords(): Flow<List<KeywordEntity>>

    @Query("SELECT * FROM keyword_alerts WHERE isActive = 1")
    suspend fun getActiveKeywords(): List<KeywordEntity>

    @Query("SELECT * FROM keyword_alerts WHERE id = :id")
    suspend fun getKeywordById(id: Int): KeywordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeyword(keyword: KeywordEntity): Long

    @Update
    suspend fun updateKeyword(keyword: KeywordEntity)

    @Query("UPDATE keyword_alerts SET isActive = :isActive WHERE id = :id")
    suspend fun updateKeywordActiveState(id: Int, isActive: Boolean)

    @Query("UPDATE keyword_alerts SET lastCheckedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateLastCheckedTimestamp(id: Int, timestamp: Long)

    @Query("UPDATE keyword_alerts SET matchCount = matchCount + 1 WHERE id = :id")
    suspend fun incrementMatchCount(id: Int)

    @Query("DELETE FROM keyword_alerts WHERE id = :id")
    suspend fun deleteKeywordById(id: Int)

    @Delete
    suspend fun deleteKeyword(keyword: KeywordEntity)

    @Query("DELETE FROM keyword_alerts")
    suspend fun clearAll()
}
