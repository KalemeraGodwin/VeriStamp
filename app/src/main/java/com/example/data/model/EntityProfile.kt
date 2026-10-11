package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entity_profile")
data class EntityProfile(
    @PrimaryKey
    val id: Int = 1,
    val companyName: String = "VERISTAMP GLOBAL ENTERPRISE",
    val physicalAddress: String = "Corporate Tower, Suite 400, Financial District",
    val contactInfo: String = "+1 (800) 555-0199 | corporate@veristamp.com",
    val registrationNumber: String = "VSTAMP-CORP-9921",
    val currencySymbol: String = "$",
    val logoPath: String? = null,
    val isConfigured: Boolean = false,
    val isDarkMode: Boolean = true
)
