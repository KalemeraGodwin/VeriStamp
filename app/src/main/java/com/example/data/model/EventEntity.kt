package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateMillis: Long,
    val dateFormatted: String,
    val location: String,
    val description: String,
    val flyerPath: String? = null,
    val createdAtMillis: Long = System.currentTimeMillis()
)
