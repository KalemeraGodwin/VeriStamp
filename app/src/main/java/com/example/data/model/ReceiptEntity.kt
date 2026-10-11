package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val receiptNumber: String,
    val entityId: Long? = null,
    val entityName: String = "",
    val entityLogoPath: String? = null,
    val entityAddress: String? = null,
    val entityContact: String? = null,
    val entityRegistrationNumber: String? = null,
    val ticketId: Long? = null,
    val eventId: Long? = null,
    val eventName: String,
    val attendeeName: String,
    val amount: Double,
    val currencySymbol: String = "USD",
    val cashChequeNo: String,
    val forDescription: String,
    val signatoryRole: String = "Authorized Signatory",
    val dateFormatted: String,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val signaturePath: String? = null,
    val pdfPath: String? = null
)
