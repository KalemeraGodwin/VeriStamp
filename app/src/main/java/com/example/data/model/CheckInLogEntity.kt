package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "check_in_logs")
data class CheckInLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serialNumber: String,
    val attendeeName: String,
    val eventName: String,
    val eventId: Long? = null,
    val recordType: String = "TICKET", // "TICKET" or "RECEIPT"
    val status: String, // "GRANTED", "ALREADY_SCANNED", "DENIED", "RECEIPT_VERIFIED"
    val timestampFormatted: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
