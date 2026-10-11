package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.Screen
import com.example.ui.VeriStampViewModel
import com.example.ui.components.VeriStampBottomBar
import com.example.ui.components.VeriStampTopBar
import com.example.ui.screens.AuditLogsScreen
import com.example.ui.screens.CorporateEntityManagerScreen
import com.example.ui.screens.EventDetailScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.OnboardingEntityScreen
import com.example.ui.screens.ReceiptBuilderScreen
import com.example.ui.screens.ReceiptsListScreen
import com.example.ui.screens.ScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TicketViewScreen
import com.example.ui.theme.VeriStampTheme

class MainActivity : ComponentActivity() {

    private val viewModel: VeriStampViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val profile by viewModel.profileState.collectAsStateWithLifecycle()
            val isDarkMode = profile?.isDarkMode ?: true

            VeriStampTheme(darkTheme = isDarkMode) {
                VeriStampApp(viewModel = viewModel, isDarkMode = isDarkMode)
            }
        }
    }
}

@Composable
fun VeriStampApp(
    viewModel: VeriStampViewModel,
    isDarkMode: Boolean
) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.profileState.collectAsStateWithLifecycle()
    val allEntities by viewModel.allEntitiesState.collectAsStateWithLifecycle()
    val events by viewModel.eventsState.collectAsStateWithLifecycle()
    val allTickets by viewModel.allTicketsState.collectAsStateWithLifecycle()
    val receipts by viewModel.receiptsState.collectAsStateWithLifecycle()
    val logs by viewModel.logsState.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val selectedTicket by viewModel.selectedTicket.collectAsStateWithLifecycle()
    val lastScanResult by viewModel.lastScanResult.collectAsStateWithLifecycle()
    val activeReceiptTicket by viewModel.activeReceiptTicket.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusMessage) {
        statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    // Hardware and gesture BackHandlers for sub-screens
    BackHandler(enabled = currentScreen != Screen.EVENTS && currentScreen != Screen.ONBOARDING) {
        when (currentScreen) {
            Screen.EVENT_DETAIL -> viewModel.navigateTo(Screen.EVENTS)
            Screen.TICKET_VIEWER -> viewModel.navigateTo(Screen.EVENT_DETAIL)
            Screen.RECEIPT_BUILDER -> {
                viewModel.clearActiveReceiptTicket()
                viewModel.navigateTo(Screen.RECEIPTS_LIST)
            }
            Screen.SETTINGS -> viewModel.navigateTo(Screen.EVENTS)
            Screen.ENTITIES_MANAGER -> viewModel.navigateTo(Screen.SETTINGS)
            Screen.SCANNER -> viewModel.navigateTo(Screen.EVENTS)
            Screen.RECEIPTS_LIST -> viewModel.navigateTo(Screen.EVENTS)
            Screen.AUDIT_LOGS -> viewModel.navigateTo(Screen.EVENTS)
            else -> viewModel.navigateTo(Screen.EVENTS)
        }
    }

    Scaffold(
        topBar = {
            if (currentScreen != Screen.ONBOARDING) {
                VeriStampTopBar(
                    entityProfile = profile,
                    currentScreen = currentScreen,
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                    onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) },
                    onOpenScanner = { viewModel.navigateTo(Screen.SCANNER) }
                )
            }
        },
        bottomBar = {
            VeriStampBottomBar(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.ONBOARDING -> {
                    OnboardingEntityScreen(
                        currentProfile = profile,
                        onSaveProfile = { comp, addr, cont, curr, logo ->
                            viewModel.saveEntityProfile(comp, addr, cont, curr, logo)
                        }
                    )
                }

                Screen.EVENTS -> {
                    EventsScreen(
                        events = events,
                        allTickets = allTickets,
                        onSelectEvent = { eventId ->
                            viewModel.selectEvent(eventId)
                        },
                        onCreateEvent = { name, dateMillis, loc, desc, flyer ->
                            viewModel.createEvent(name, dateMillis, loc, desc, flyer)
                        }
                    )
                }

                Screen.EVENT_DETAIL -> {
                    val event = events.find { it.id == selectedEventId }
                    val eventTickets = allTickets.filter { it.eventId == selectedEventId }

                    EventDetailScreen(
                        event = event,
                        tickets = eventTickets,
                        onBack = { viewModel.navigateTo(Screen.EVENTS) },
                        onViewTicket = { ticket ->
                            viewModel.viewTicket(ticket)
                        },
                        onIssueTicket = { name, email, cat, amount, curr ->
                            if (selectedEventId != null) {
                                viewModel.issueTicket(selectedEventId!!, name, email, cat, amount, curr)
                            }
                        },
                        onGenerateReceipt = { ticket ->
                            viewModel.openReceiptBuilderForTicket(ticket)
                        },
                        onTestScan = { serial ->
                            viewModel.processQrScan(serial)
                            viewModel.navigateTo(Screen.SCANNER)
                        }
                    )
                }

                Screen.TICKET_VIEWER -> {
                    val event = events.find { it.id == selectedTicket?.eventId }
                    TicketViewScreen(
                        ticket = selectedTicket,
                        event = event,
                        onBack = { viewModel.navigateTo(Screen.EVENT_DETAIL) },
                        onTestScan = { serial ->
                            viewModel.processQrScan(serial)
                            viewModel.navigateTo(Screen.SCANNER)
                        },
                        onGenerateReceipt = { ticket ->
                            viewModel.openReceiptBuilderForTicket(ticket)
                        },
                        onDownloadPdf = { ticket, callback ->
                            viewModel.downloadTicketPdf(ticket, callback)
                        }
                    )
                }

                Screen.SCANNER -> {
                    ScannerScreen(
                        tickets = allTickets,
                        logs = logs,
                        lastResult = lastScanResult,
                        onScanSerial = { serial ->
                            viewModel.processQrScan(serial)
                        },
                        onDismissResult = {
                            viewModel.clearScanResult()
                        }
                    )
                }

                Screen.RECEIPT_BUILDER -> {
                    val ticket = activeReceiptTicket
                    val event = events.find { it.id == ticket?.eventId }

                    ReceiptBuilderScreen(
                        profile = profile,
                        entities = allEntities,
                        ticket = ticket,
                        event = event,
                        onBack = {
                            viewModel.clearActiveReceiptTicket()
                            viewModel.navigateTo(Screen.RECEIPTS_LIST)
                        },
                        onNavigateToEntityManager = {
                            viewModel.navigateTo(Screen.ENTITIES_MANAGER)
                        },
                        onCompileReceipt = { entityId, ticketId, eventId, eventName, attendee, amt, curr, chq, desc, sig, role, onDone ->
                            viewModel.compileReceipt(
                                entityId = entityId,
                                ticketId = ticketId,
                                eventId = eventId,
                                eventName = eventName,
                                attendeeName = attendee,
                                amount = amt,
                                currencySymbol = curr,
                                cashChequeNo = chq,
                                forDescription = desc,
                                signatureBitmap = sig,
                                signatoryRole = role,
                                onSuccess = onDone
                            )
                        }
                    )
                }

                Screen.RECEIPTS_LIST -> {
                    ReceiptsListScreen(
                        receipts = receipts,
                        onCreateNewReceipt = {
                            viewModel.clearActiveReceiptTicket()
                            viewModel.navigateTo(Screen.RECEIPT_BUILDER)
                        }
                    )
                }

                Screen.AUDIT_LOGS -> {
                    AuditLogsScreen(logs = logs)
                }

                Screen.ENTITIES_MANAGER -> {
                    CorporateEntityManagerScreen(
                        entities = allEntities,
                        onBack = { viewModel.navigateTo(Screen.SETTINGS) },
                        onCreateEntity = { name, logoPath, addr, cont, reg, curr ->
                            viewModel.createCorporateEntity(
                                name = name,
                                logoPath = logoPath,
                                physicalAddress = addr,
                                contactInfo = cont,
                                registrationNumber = reg,
                                currency = curr,
                                onSuccess = {},
                                onError = {}
                            )
                        },
                        onDeleteEntity = { entity ->
                            viewModel.deleteCorporateEntity(entity)
                        }
                    )
                }

                Screen.SETTINGS -> {
                    SettingsScreen(
                        profile = profile,
                        isDarkMode = isDarkMode,
                        onBack = { viewModel.navigateTo(Screen.EVENTS) },
                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                        onNavigateToEntityManager = { viewModel.navigateTo(Screen.ENTITIES_MANAGER) },
                        onSaveProfile = { comp, addr, cont, curr, logo ->
                            viewModel.saveEntityProfile(comp, addr, cont, curr, logo)
                        }
                    )
                }
            }
        }
    }
}
