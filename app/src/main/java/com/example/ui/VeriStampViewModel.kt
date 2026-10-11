package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.VeriStampDatabase
import com.example.data.model.CheckInLogEntity
import com.example.data.model.CorporateEntity
import com.example.data.model.EntityProfile
import com.example.data.model.EventEntity
import com.example.data.model.ReceiptEntity
import com.example.data.model.TicketEntity
import com.example.data.repository.CheckInResult
import com.example.data.repository.VeriStampRepository
import com.example.util.SoundFeedbackUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class Screen {
    ONBOARDING,
    EVENTS,
    EVENT_DETAIL,
    TICKET_VIEWER,
    SCANNER,
    RECEIPT_BUILDER,
    RECEIPTS_LIST,
    AUDIT_LOGS,
    ENTITIES_MANAGER,
    SETTINGS
}

class VeriStampViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VeriStampRepository(VeriStampDatabase.getInstance(application))
    private val soundFeedback = SoundFeedbackUtil(application)

    private val _currentScreen = MutableStateFlow(Screen.EVENTS)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedEventId = MutableStateFlow<Long?>(null)
    val selectedEventId: StateFlow<Long?> = _selectedEventId.asStateFlow()

    private val _selectedTicketId = MutableStateFlow<Long?>(null)
    val selectedTicketId: StateFlow<Long?> = _selectedTicketId.asStateFlow()

    private val _selectedTicket = MutableStateFlow<TicketEntity?>(null)
    val selectedTicket: StateFlow<TicketEntity?> = _selectedTicket.asStateFlow()

    private val _lastScanResult = MutableStateFlow<CheckInResult?>(null)
    val lastScanResult: StateFlow<CheckInResult?> = _lastScanResult.asStateFlow()

    private val _activeReceiptTicket = MutableStateFlow<TicketEntity?>(null)
    val activeReceiptTicket: StateFlow<TicketEntity?> = _activeReceiptTicket.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val profileState: StateFlow<EntityProfile?> = repository.profileFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allEntitiesState: StateFlow<List<CorporateEntity>> = repository.allEntitiesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val eventsState: StateFlow<List<EventEntity>> = repository.allEventsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTicketsState: StateFlow<List<TicketEntity>> = repository.allTicketsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val receiptsState: StateFlow<List<ReceiptEntity>> = repository.allReceiptsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logsState: StateFlow<List<CheckInLogEntity>> = repository.checkInLogsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded(application)
            val profile = repository.getProfile()
            if (!profile.isConfigured) {
                _currentScreen.value = Screen.ONBOARDING
            }
        }
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun selectEvent(eventId: Long) {
        _selectedEventId.value = eventId
        _currentScreen.value = Screen.EVENT_DETAIL
    }

    fun viewTicket(ticket: TicketEntity) {
        _selectedTicket.value = ticket
        _selectedTicketId.value = ticket.id
        _currentScreen.value = Screen.TICKET_VIEWER
    }

    fun openReceiptBuilderForTicket(ticket: TicketEntity) {
        _activeReceiptTicket.value = ticket
        _currentScreen.value = Screen.RECEIPT_BUILDER
    }

    fun clearActiveReceiptTicket() {
        _activeReceiptTicket.value = null
    }

    fun saveEntityProfile(
        companyName: String,
        physicalAddress: String,
        contactInfo: String,
        currencySymbol: String,
        logoPath: String?
    ) {
        viewModelScope.launch {
            val existing = profileState.value ?: EntityProfile()
            val updated = existing.copy(
                companyName = companyName.trim().ifEmpty { "VERISTAMP CORPORATE" },
                physicalAddress = physicalAddress.trim().ifEmpty { "Corporate Financial District" },
                contactInfo = contactInfo.trim(),
                currencySymbol = currencySymbol.trim().ifEmpty { "USD" },
                logoPath = logoPath ?: existing.logoPath,
                isConfigured = true
            )
            repository.saveProfile(updated)
            _statusMessage.value = "Entity setup saved"
            _currentScreen.value = Screen.EVENTS
        }
    }

    // Corporate Entity Creation (Max 10)
    fun createCorporateEntity(
        name: String,
        logoPath: String?,
        physicalAddress: String,
        contactInfo: String,
        registrationNumber: String,
        currency: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.createCorporateEntity(
                name = name,
                logoPath = logoPath,
                physicalAddress = physicalAddress,
                contactInfo = contactInfo,
                registrationNumber = registrationNumber,
                currency = currency
            )
            result.onSuccess { entity ->
                _statusMessage.value = "Entity registered: ${entity.name}"
                onSuccess()
            }.onFailure { ex ->
                onError(ex.message ?: "Failed to create entity")
            }
        }
    }

    fun deleteCorporateEntity(entity: CorporateEntity) {
        viewModelScope.launch {
            repository.deleteCorporateEntity(entity)
            _statusMessage.value = "Entity removed: ${entity.name}"
        }
    }

    fun toggleDarkMode(isDark: Boolean) {
        viewModelScope.launch {
            repository.toggleTheme(isDark)
        }
    }

    fun createEvent(
        name: String,
        dateMillis: Long,
        location: String,
        description: String,
        flyerPath: String? = null
    ) {
        viewModelScope.launch {
            val eventId = repository.createEvent(
                name = name.trim(),
                dateMillis = dateMillis,
                location = location.trim(),
                description = description.trim(),
                flyerPath = flyerPath ?: "corporate_flyer"
            )
            _selectedEventId.value = eventId
            _statusMessage.value = "Event created successfully"
            _currentScreen.value = Screen.EVENT_DETAIL
        }
    }

    fun issueTicket(
        eventId: Long,
        attendeeName: String,
        attendeeEmail: String,
        category: String,
        amountPaid: Double,
        currencySymbol: String = "USD"
    ) {
        viewModelScope.launch {
            val ticket = repository.generateTicket(
                eventId = eventId,
                attendeeName = attendeeName,
                attendeeEmail = attendeeEmail,
                category = category,
                amountPaid = amountPaid,
                currencySymbol = currencySymbol.trim().ifEmpty { "USD" }
            )
            _statusMessage.value = "Ticket generated: ${ticket.serialNumber}"
        }
    }

    fun downloadTicketPdf(ticket: TicketEntity, onDone: (File) -> Unit) {
        viewModelScope.launch {
            val file = repository.generateTicketPdf(getApplication(), ticket)
            _statusMessage.value = "Ticket PDF downloaded: ${file.name}"
            onDone(file)
        }
    }

    fun processQrScan(scannedCode: String) {
        viewModelScope.launch {
            val result = repository.verifyAndCheckInTicket(scannedCode)
            _lastScanResult.value = result

            when (result) {
                is CheckInResult.Granted -> soundFeedback.playSuccessFeedback()
                is CheckInResult.ReceiptVerified -> soundFeedback.playSuccessFeedback()
                is CheckInResult.AlreadyScanned -> soundFeedback.playWarningFeedback()
                is CheckInResult.Denied -> soundFeedback.playDeniedFeedback()
            }
        }
    }

    fun clearScanResult() {
        _lastScanResult.value = null
    }

    fun compileReceipt(
        entityId: Long?,
        ticketId: Long?,
        eventId: Long?,
        eventName: String,
        attendeeName: String,
        amount: Double,
        currencySymbol: String,
        cashChequeNo: String,
        forDescription: String,
        signatureBitmap: Bitmap?,
        signatoryRole: String = "Authorized Signatory",
        onSuccess: (File) -> Unit
    ) {
        viewModelScope.launch {
            val receipt = repository.createAndCompileReceipt(
                context = getApplication(),
                entityId = entityId,
                ticketId = ticketId,
                eventId = eventId,
                eventName = eventName,
                attendeeName = attendeeName,
                amount = amount,
                currencySymbol = currencySymbol,
                cashChequeNo = cashChequeNo,
                forDescription = forDescription,
                signatureBitmap = signatureBitmap,
                signatoryRole = signatoryRole
            )
            _statusMessage.value = "Receipt compiled: ${receipt.receiptNumber}"
            if (receipt.pdfPath != null) {
                val file = File(receipt.pdfPath)
                if (file.exists()) {
                    onSuccess(file)
                }
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    override fun onCleared() {
        super.onCleared()
        soundFeedback.release()
    }
}
