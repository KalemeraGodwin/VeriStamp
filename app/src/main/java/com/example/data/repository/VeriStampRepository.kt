package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import com.example.data.database.VeriStampDatabase
import com.example.data.model.CheckInLogEntity
import com.example.data.model.CorporateEntity
import com.example.data.model.EntityProfile
import com.example.data.model.EventEntity
import com.example.data.model.ReceiptEntity
import com.example.data.model.TicketEntity
import com.example.util.DateUtil
import com.example.util.PdfReceiptGenerator
import com.example.util.PdfTicketGenerator
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream

sealed class CheckInResult {
    data class Granted(val ticket: TicketEntity, val logTime: String) : CheckInResult()
    data class AlreadyScanned(val ticket: TicketEntity, val previousLogTime: String) : CheckInResult()
    data class ReceiptVerified(val receipt: ReceiptEntity, val logTime: String) : CheckInResult()
    data class Denied(val message: String, val scannedSerial: String) : CheckInResult()
}

class VeriStampRepository(private val database: VeriStampDatabase) {

    private val profileDao = database.entityProfileDao()
    private val corporateEntityDao = database.corporateEntityDao()
    private val eventDao = database.eventDao()
    private val ticketDao = database.ticketDao()
    private val receiptDao = database.receiptDao()
    private val checkInLogDao = database.checkInLogDao()

    val profileFlow: Flow<EntityProfile?> = profileDao.getProfileFlow()
    val allEntitiesFlow: Flow<List<CorporateEntity>> = corporateEntityDao.getAllEntitiesFlow()
    val allEventsFlow: Flow<List<EventEntity>> = eventDao.getAllEvents()
    val allTicketsFlow: Flow<List<TicketEntity>> = ticketDao.getAllTicketsFlow()
    val allReceiptsFlow: Flow<List<ReceiptEntity>> = receiptDao.getAllReceiptsFlow()
    val checkInLogsFlow: Flow<List<CheckInLogEntity>> = checkInLogDao.getAllLogsFlow()

    fun getLogsByEventFlow(eventName: String): Flow<List<CheckInLogEntity>> {
        return checkInLogDao.getLogsByEventNameFlow(eventName)
    }

    fun getTicketsForEventFlow(eventId: Long): Flow<List<TicketEntity>> {
        return ticketDao.getTicketsForEventFlow(eventId)
    }

    fun getEventFlow(eventId: Long): Flow<EventEntity?> {
        return eventDao.getEventByIdFlow(eventId)
    }

    suspend fun getProfile(): EntityProfile {
        return profileDao.getProfile() ?: EntityProfile().also {
            profileDao.insertOrUpdate(it)
        }
    }

    suspend fun saveProfile(profile: EntityProfile) {
        profileDao.insertOrUpdate(profile)
    }

    suspend fun toggleTheme(isDark: Boolean) {
        profileDao.updateThemeMode(isDark)
    }

    // Corporate Entity Management (Strict 10 Entity Limit)
    suspend fun getCorporateEntityCount(): Int {
        return corporateEntityDao.getEntityCount()
    }

    suspend fun createCorporateEntity(
        name: String,
        logoPath: String?,
        physicalAddress: String,
        contactInfo: String,
        registrationNumber: String,
        currency: String
    ): Result<CorporateEntity> {
        val count = corporateEntityDao.getEntityCount()
        if (count >= 10) {
            return Result.failure(IllegalStateException("Entity Limit Reached: A maximum of 10 corporate entities can be registered in VeriStamp."))
        }

        val entity = CorporateEntity(
            name = name.trim(),
            logoPath = logoPath,
            physicalAddress = physicalAddress.trim(),
            contactInfo = contactInfo.trim(),
            registrationNumber = registrationNumber.trim(),
            currency = currency.trim().ifEmpty { "USD" }
        )
        val id = corporateEntityDao.insertEntity(entity)
        return Result.success(entity.copy(id = id))
    }

    suspend fun deleteCorporateEntity(entity: CorporateEntity) {
        corporateEntityDao.deleteEntity(entity)
    }

    suspend fun getEntityById(id: Long): CorporateEntity? {
        return corporateEntityDao.getEntityById(id)
    }

    suspend fun createEvent(
        name: String,
        dateMillis: Long,
        location: String,
        description: String,
        flyerPath: String? = null
    ): Long {
        val formattedDate = DateUtil.formatEventDate(dateMillis)
        val event = EventEntity(
            name = name,
            dateMillis = dateMillis,
            dateFormatted = formattedDate,
            location = location,
            description = description,
            flyerPath = flyerPath
        )
        return eventDao.insertEvent(event)
    }

    suspend fun deleteEvent(eventId: Long) {
        eventDao.deleteEventById(eventId)
    }

    suspend fun generateTicket(
        eventId: Long,
        attendeeName: String,
        attendeeEmail: String = "",
        category: String = "Executive VIP",
        amountPaid: Double,
        currencySymbol: String = "USD"
    ): TicketEntity {
        val timestamp = DateUtil.getCurrentTicketTimestamp()
        val serialNumber = DateUtil.generateTicketSerial()

        val ticket = TicketEntity(
            eventId = eventId,
            serialNumber = serialNumber,
            attendeeName = attendeeName.trim(),
            attendeeEmail = attendeeEmail.trim(),
            category = category,
            amountPaid = amountPaid,
            currencySymbol = currencySymbol,
            generatedTimestamp = timestamp,
            generatedAtMillis = System.currentTimeMillis()
        )
        val id = ticketDao.insertTicket(ticket)
        return ticket.copy(id = id)
    }

    suspend fun getTicketBySerial(serialNumber: String): TicketEntity? {
        return ticketDao.getTicketBySerialNumber(serialNumber.trim())
    }

    suspend fun generateTicketPdf(context: Context, ticket: TicketEntity): File {
        val event = eventDao.getEventById(ticket.eventId)
        return PdfTicketGenerator.generateTicketPdf(context, ticket, event)
    }

    // Unified QR code verification for BOTH Tickets and Receipts
    suspend fun verifyAndCheckInTicket(scannedString: String): CheckInResult {
        val trimmed = scannedString.trim()

        // 1. Try Ticket Lookup
        val ticket = ticketDao.getTicketBySerialNumber(trimmed)
        if (ticket != null) {
            val event = eventDao.getEventById(ticket.eventId)
            val eventName = event?.name ?: "Corporate Event"

            if (ticket.checkedIn) {
                val prevTime = ticket.checkedInAt ?: "Earlier"
                checkInLogDao.insertLog(
                    CheckInLogEntity(
                        serialNumber = trimmed,
                        attendeeName = ticket.attendeeName,
                        eventName = eventName,
                        eventId = ticket.eventId,
                        recordType = "TICKET",
                        status = "ALREADY_SCANNED",
                        timestampFormatted = DateUtil.formatCurrentLogTime()
                    )
                )
                return CheckInResult.AlreadyScanned(
                    ticket = ticket,
                    previousLogTime = prevTime
                )
            }

            // Valid & Unused Ticket: Grant access
            val checkInTimestamp = DateUtil.formatCurrentLogTime()
            val nowMillis = System.currentTimeMillis()
            ticketDao.markTicketCheckedIn(ticket.id, checkInTimestamp, nowMillis)

            checkInLogDao.insertLog(
                CheckInLogEntity(
                    serialNumber = trimmed,
                    attendeeName = ticket.attendeeName,
                    eventName = eventName,
                    eventId = ticket.eventId,
                    recordType = "TICKET",
                    status = "GRANTED",
                    timestampFormatted = checkInTimestamp
                )
            )

            return CheckInResult.Granted(
                ticket = ticket.copy(
                    checkedIn = true,
                    checkedInAt = checkInTimestamp,
                    checkedInAtMillis = nowMillis
                ),
                logTime = checkInTimestamp
            )
        }

        // 2. Try Receipt Lookup
        val receipt = receiptDao.getReceiptByNumber(trimmed)
        if (receipt != null) {
            val logTime = DateUtil.formatCurrentLogTime()
            val entityTitle = receipt.entityName.ifEmpty { receipt.eventName }
            checkInLogDao.insertLog(
                CheckInLogEntity(
                    serialNumber = trimmed,
                    attendeeName = receipt.attendeeName,
                    eventName = entityTitle,
                    eventId = receipt.eventId,
                    recordType = "RECEIPT",
                    status = "RECEIPT_VERIFIED",
                    timestampFormatted = logTime
                )
            )
            return CheckInResult.ReceiptVerified(
                receipt = receipt,
                logTime = logTime
            )
        }

        // 3. Unrecognized Code
        val logTime = DateUtil.formatCurrentLogTime()
        checkInLogDao.insertLog(
            CheckInLogEntity(
                serialNumber = trimmed,
                attendeeName = "Unrecognized QR",
                eventName = "N/A",
                recordType = "UNKNOWN",
                status = "DENIED",
                timestampFormatted = logTime
            )
        )
        return CheckInResult.Denied(
            message = "ACCESS DENIED: Invalid QR Code",
            scannedSerial = trimmed
        )
    }

    suspend fun saveSignatureBitmap(context: Context, signatureBitmap: Bitmap, attendeeName: String): String {
        val fileName = "sig_${attendeeName.replace(" ", "_")}_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, fileName)
        FileOutputStream(file).use { out ->
            signatureBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file.absolutePath
    }

    suspend fun createAndCompileReceipt(
        context: Context,
        entityId: Long?,
        ticketId: Long?,
        eventId: Long?,
        eventName: String,
        attendeeName: String,
        amount: Double,
        currencySymbol: String,
        cashChequeNo: String,
        forDescription: String,
        signatureBitmap: Bitmap? = null,
        signatoryRole: String = "Authorized Signatory"
    ): ReceiptEntity {
        val receiptNumber = DateUtil.generateReceiptNumber()
        val dateFormatted = DateUtil.getCurrentReceiptDateTime()

        // Lookup selected entity if provided
        val selectedEntity = if (entityId != null) corporateEntityDao.getEntityById(entityId) else null
        val entityName = selectedEntity?.name ?: "VERISTAMP CORPORATE"
        val entityLogoPath = selectedEntity?.logoPath

        var signaturePath: String? = null
        if (signatureBitmap != null) {
            signaturePath = saveSignatureBitmap(context, signatureBitmap, attendeeName)
        }

        var receipt = ReceiptEntity(
            receiptNumber = receiptNumber,
            entityId = entityId,
            entityName = entityName,
            entityLogoPath = entityLogoPath,
            entityAddress = selectedEntity?.physicalAddress,
            entityContact = selectedEntity?.contactInfo,
            entityRegistrationNumber = selectedEntity?.registrationNumber,
            ticketId = ticketId,
            eventId = eventId,
            eventName = eventName,
            attendeeName = attendeeName.trim(),
            amount = amount,
            currencySymbol = currencySymbol,
            cashChequeNo = cashChequeNo.trim().ifEmpty { "CASH-VERIFIED" },
            forDescription = forDescription.trim(),
            signatoryRole = signatoryRole.trim().ifEmpty { "Authorized Signatory" },
            dateFormatted = dateFormatted,
            createdAtMillis = System.currentTimeMillis(),
            signaturePath = signaturePath
        )

        val id = receiptDao.insertReceipt(receipt)
        receipt = receipt.copy(id = id)

        // Compile PDF strictly using the selected corporate entity's branding, address & logo
        val profile = getProfile()
        val pdfFile = PdfReceiptGenerator.generateReceiptPdf(
            context = context,
            receipt = receipt,
            entityProfile = profile,
            corporateEntity = selectedEntity,
            signatureBitmap = signatureBitmap
        )

        receipt = receipt.copy(pdfPath = pdfFile.absolutePath)
        receiptDao.updateReceipt(receipt)
        return receipt
    }

    suspend fun seedInitialDataIfNeeded(context: Context) {
        val existingProfile = profileDao.getProfile()
        if (existingProfile == null) {
            profileDao.insertOrUpdate(
                EntityProfile(
                    companyName = "VERISTAMP GLOBAL ENTERPRISE",
                    physicalAddress = "Corporate Tower, Suite 400, Financial District",
                    contactInfo = "+1 (800) 555-0199 | desk@veristamp.com",
                    registrationNumber = "VSTAMP-CORP-9921",
                    currencySymbol = "USD",
                    isConfigured = true,
                    isDarkMode = true
                )
            )
        }

        // Remove any default sample entity so user enters/adds an entity themselves
        val existingEntities = corporateEntityDao.getAllEntitiesList()
        existingEntities.find { it.name == "Vanguard Executive Holdings" }?.let {
            corporateEntityDao.deleteEntity(it)
        }

        if (eventDao.getCount() == 0) {
            val galaEventId = eventDao.insertEvent(
                EventEntity(
                    name = "Global Leadership Gala 2026",
                    dateMillis = System.currentTimeMillis() + (86400000L * 14), // 2 weeks out
                    dateFormatted = "Oct 25, 2026 - 19:00",
                    location = "Grand Ballroom, The St. Regis Executive Center",
                    description = "Exclusive annual corporate summit and awards banquet for executive leadership and delegates.",
                    flyerPath = "corporate_flyer"
                )
            )

            // Seed sample tickets
            val t1 = generateTicket(
                eventId = galaEventId,
                attendeeName = "Alexander Vance",
                attendeeEmail = "a.vance@vanguardcorp.com",
                category = "Executive VIP",
                amountPaid = 750.0,
                currencySymbol = "USD"
            )

            val t2 = generateTicket(
                eventId = galaEventId,
                attendeeName = "Eleanor Sterling",
                attendeeEmail = "eleanor.sterling@apexpartners.org",
                category = "Keynote Speaker",
                amountPaid = 0.0,
                currencySymbol = "USD"
            )

            val t3 = generateTicket(
                eventId = galaEventId,
                attendeeName = "Marcus Aurelius Thorne",
                attendeeEmail = "thorne@aegiscapital.com",
                category = "Standard Delegate",
                amountPaid = 450.0,
                currencySymbol = "USD"
            )

            // Pre-checkin t3 using system timezone
            val checkInTime = DateUtil.getCurrentTicketTimestamp()
            ticketDao.markTicketCheckedIn(t3.id, checkInTime, System.currentTimeMillis() - 3600000L)
        }
    }
}
