package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "corporate_entities")
data class CorporateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val logoPath: String? = null,
    val physicalAddress: String = "",
    val contactInfo: String = "",
    val registrationNumber: String = "",
    val currency: String = "USD", // User-defined custom currency
    val createdAtMillis: Long = System.currentTimeMillis()
)
