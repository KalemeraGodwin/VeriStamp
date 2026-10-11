package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tickets",
    foreignKeys = [
        ForeignKey(
            entity = EventEntity::class,
            parentColumns = ["id"],
            childColumns = ["eventId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["eventId"]),
        Index(value = ["serialNumber"], unique = true)
    ]
)
data class TicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val eventId: Long,
    val serialNumber: String,
    val attendeeName: String,
    val attendeeEmail: String = "",
    val category: String = "Executive VIP",
    val amountPaid: Double,
    val currencySymbol: String = "$",
    val generatedTimestamp: String,
    val generatedAtMillis: Long = System.currentTimeMillis(),
    val checkedIn: Boolean = false,
    val checkedInAt: String? = null,
    val checkedInAtMillis: Long? = null
)
