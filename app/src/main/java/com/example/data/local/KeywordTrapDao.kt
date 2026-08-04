package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface KeywordTrapDao {
    @Query("SELECT * FROM keyword_traps ORDER BY createdAt DESC")
    fun getAllTraps(): Flow<List<KeywordTrapEntity>>

    @Query("SELECT * FROM keyword_traps WHERE isActive = 1")
    suspend fun getActiveTraps(): List<KeywordTrapEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrap(trap: KeywordTrapEntity)

    @Query("DELETE FROM keyword_traps WHERE id = :id")
    suspend fun deleteTrapById(id: Int)

    @Query("UPDATE keyword_traps SET isActive = :isActive WHERE id = :id")
    suspend fun updateTrapState(id: Int, isActive: Boolean)

    @Query("UPDATE keyword_traps SET matchCount = matchCount + 1 WHERE id = :id")
    suspend fun incrementMatchCount(id: Int)
}
