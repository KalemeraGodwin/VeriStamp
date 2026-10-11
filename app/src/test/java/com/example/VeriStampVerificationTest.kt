package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.VeriStampDatabase
import com.example.data.model.EntityProfile
import com.example.data.model.ReceiptEntity
import com.example.data.repository.CheckInResult
import com.example.data.repository.VeriStampRepository
import com.example.util.DateUtil
import com.example.util.PdfReceiptGenerator
import com.example.util.QrCodeUtil
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VeriStampVerificationTest {

    private lateinit var database: VeriStampDatabase
    private lateinit var repository: VeriStampRepository
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, VeriStampDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VeriStampRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testTicketSerializationFormat() {
        val serial = DateUtil.generateTicketSerial()
        assertTrue("Serial must start with VSTAMP-", serial.startsWith("VSTAMP-"))
        val parts = serial.split("-")
        assertEquals(3, parts.size)
        assertEquals(4, parts[1].length) // Year
        assertEquals(5, parts[2].length) // Alphanumeric
    }

    @Test
    fun testCheckInLifecycleAndDoubleCheckInPrevention() = runBlocking {
        // 1. Create Event
        val eventId = repository.createEvent(
            name = "Executive Security Summit",
            dateMillis = System.currentTimeMillis() + 86400000L,
            location = "Ballroom Alpha",
            description = "High-level briefing",
            flyerPath = "corporate_flyer"
        )
        assertTrue(eventId > 0)

        // 2. Issue Ticket
        val ticket = repository.generateTicket(
            eventId = eventId,
            attendeeName = "Julian Sterling",
            attendeeEmail = "julian@sterlingcorp.com",
            category = "Executive VIP",
            amountPaid = 800.0,
            currencySymbol = "$"
        )
        assertFalse("New ticket must not be checked in", ticket.checkedIn)
        assertTrue(ticket.serialNumber.isNotBlank())

        // 3. Condition 1: First check-in must be GRANTED
        val firstResult = repository.verifyAndCheckInTicket(ticket.serialNumber)
        assertTrue("First scan must grant access", firstResult is CheckInResult.Granted)
        val granted = firstResult as CheckInResult.Granted
        assertEquals("Julian Sterling", granted.ticket.attendeeName)
        assertTrue(granted.ticket.checkedIn)
        val originalLogTime = granted.logTime
        assertTrue(originalLogTime.isNotBlank())

        // Verify database state is updated
        val updatedTicketInDb = repository.getTicketBySerial(ticket.serialNumber)
        assertNotNull(updatedTicketInDb)
        assertTrue(updatedTicketInDb!!.checkedIn)
        assertEquals(originalLogTime, updatedTicketInDb.checkedInAt)

        // 4. Condition 2: Second check-in of same ticket MUST be blocked as ALREADY_SCANNED
        val secondResult = repository.verifyAndCheckInTicket(ticket.serialNumber)
        assertTrue("Second scan must be blocked as AlreadyScanned", secondResult is CheckInResult.AlreadyScanned)
        val alreadyScanned = secondResult as CheckInResult.AlreadyScanned
        assertEquals(originalLogTime, alreadyScanned.previousLogTime)

        // 5. Verify database was not overwritten or corrupted
        val ticketAfterSecondScan = repository.getTicketBySerial(ticket.serialNumber)
        assertEquals(originalLogTime, ticketAfterSecondScan?.checkedInAt)
    }

    @Test
    fun testInvalidQrCodeDenied() = runBlocking {
        val result = repository.verifyAndCheckInTicket("NON-EXISTENT-SERIAL-999")
        assertTrue("Unknown QR code must be denied", result is CheckInResult.Denied)
        val denied = result as CheckInResult.Denied
        assertEquals("ACCESS DENIED: Invalid QR Code", denied.message)
    }

    @Test
    fun testQrCodeGenerationAndDecoding() {
        val testSerial = "VSTAMP-2026-ALPHA"
        val bitmap = QrCodeUtil.generateQrBitmap(
            content = testSerial,
            sizePx = 256
        )
        assertNotNull(bitmap)
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)

        val decoded = QrCodeUtil.decodeQrBitmap(bitmap)
        assertEquals(testSerial, decoded)
    }

    @Test
    fun testPdfReceiptCompilation() {
        val receipt = ReceiptEntity(
            receiptNumber = "RCT-2026-0099",
            eventName = "Annual Gala",
            attendeeName = "Marcus Aurelius",
            amount = 1200.0,
            currencySymbol = "$",
            cashChequeNo = "CHQ-8821",
            forDescription = "Presidential Table Admission",
            dateFormatted = "October 11, 2026 - 10:00 AM"
        )
        val profile = EntityProfile(
            companyName = "VeriStamp Systems",
            physicalAddress = "100 Financial Way",
            contactInfo = "info@veristamp.com",
            registrationNumber = "CORP-101",
            currencySymbol = "$"
        )

        val pdfFile: File = PdfReceiptGenerator.generateReceiptPdf(
            context = context,
            receipt = receipt,
            entityProfile = profile,
            signatureBitmap = null
        )

        assertNotNull(pdfFile)
        assertTrue("PDF file must exist", pdfFile.exists())
        assertTrue("PDF file must have content bytes", pdfFile.length() > 100)
        assertTrue("PDF file name must match attendee name pattern", pdfFile.name.contains("Marcus_Aurelius_Receipt.pdf"))
    }

    @Test
    fun testCorporateEntityPdfReflectionAndSignatoryRole() = runBlocking {
        val corpEntity = com.example.data.model.CorporateEntity(
            id = 1,
            name = "Apex Sovereign Logistics",
            physicalAddress = "Suite 500, Apex Harbor Tower",
            contactInfo = "desk@apexlogistics.com",
            registrationNumber = "APX-CORP-774",
            currency = "EUR"
        )

        val receipt = ReceiptEntity(
            receiptNumber = "RCT-2026-7788",
            entityId = 1,
            entityName = corpEntity.name,
            entityAddress = corpEntity.physicalAddress,
            entityContact = corpEntity.contactInfo,
            entityRegistrationNumber = corpEntity.registrationNumber,
            eventName = "Maritime Logistics Summit",
            attendeeName = "Helena Drake",
            amount = 950.0,
            currencySymbol = "EUR",
            cashChequeNo = "CHQ-APX-99",
            forDescription = "Executive Keynote Delegate Access",
            signatoryRole = "Managing Director of Finance",
            dateFormatted = "October 11, 2026 - 11:30 AM CET"
        )

        val pdfFile = PdfReceiptGenerator.generateReceiptPdf(
            context = context,
            receipt = receipt,
            corporateEntity = corpEntity,
            signatureBitmap = null
        )

        assertNotNull(pdfFile)
        assertTrue(pdfFile.exists())
        assertTrue(pdfFile.length() > 100)
        assertTrue(pdfFile.name.contains("Helena_Drake_Receipt.pdf"))
    }

    @Test
    fun testNoDefaultEntitySeededInitially() = runBlocking {
        repository.seedInitialDataIfNeeded(context)
        val entityCount = repository.getCorporateEntityCount()
        assertEquals("No default entity should be seeded automatically", 0, entityCount)
    }
}
