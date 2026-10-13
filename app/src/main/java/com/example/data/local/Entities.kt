package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_styles")
data class SavedStyleEntity(
    @PrimaryKey val hairstyleId: String,
    val name: String,
    val gender: String,
    val length: String,
    val description: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "generation_history")
data class GenerationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val hairstyleName: String,
    val hairColor: String,
    val faceShape: String,
    val matchPercentage: Int,
    val stylistNotes: String,
    val imageBase64: String? = null,
    val localUri: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
