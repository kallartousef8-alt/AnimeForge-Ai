package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime_projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val durationMs: Long = 15000L,
    val aspectRatio: String = "PORTRAIT_9_16",
    val resolution: String = "RES_1080P",
    val fps: Int = 30,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val thumbnailUri: String? = null,
    val clipsJson: String = "[]",
    val audioJson: String = "[]",
    val captionsJson: String = "[]",
    val activeFilter: String = "NONE"
)
