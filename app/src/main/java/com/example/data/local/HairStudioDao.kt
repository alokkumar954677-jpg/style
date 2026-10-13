package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HairStudioDao {

    // Saved Styles
    @Query("SELECT * FROM saved_styles ORDER BY savedAt DESC")
    fun getAllSavedStyles(): Flow<List<SavedStyleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedStyle(style: SavedStyleEntity)

    @Delete
    suspend fun deleteSavedStyle(style: SavedStyleEntity)

    @Query("DELETE FROM saved_styles WHERE hairstyleId = :id")
    suspend fun deleteSavedStyleById(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_styles WHERE hairstyleId = :id)")
    suspend fun isStyleSaved(id: String): Boolean

    // History
    @Query("SELECT * FROM generation_history ORDER BY createdAt DESC")
    fun getAllHistory(): Flow<List<GenerationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: GenerationHistoryEntity): Long

    @Query("DELETE FROM generation_history WHERE id = :id")
    suspend fun deleteHistoryById(id: Long)

    @Query("DELETE FROM generation_history")
    suspend fun clearAllHistory()
}
