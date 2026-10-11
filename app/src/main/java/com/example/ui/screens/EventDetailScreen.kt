package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.EventEntity
import com.example.data.model.TicketEntity
import com.example.ui.components.TicketLandscapeCard
import com.example.ui.theme.AccessGrantedGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack

@Composable
fun EventDetailScreen(
    event: EventEntity?,
    tickets: List<TicketEntity>,
    onBack: () -> Unit,
    onViewTicket: (TicketEntity) -> Unit,
    onIssueTicket: (attendeeName: String, attendeeEmail: String, category: String, amountPaid: Double, currency: String) -> Unit,
    onGenerateReceipt: (TicketEntity) -> Unit,
    onTestScan: (String) -> Unit
) {
    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event not found")
        }
        return
    }

    var showIssueTicketDialog by remember { mutableStateOf(false) }
    var filterStatus by remember { mutableStateOf("ALL") } // "ALL", "CHECKED_IN", "PENDING"

    val filteredTickets = when (filterStatus) {
        "CHECKED_IN" -> tickets.filter { it.checkedIn }
        "PENDING" -> tickets.filter { !it.checkedIn }
        else -> tickets
    }

    val totalIssued = tickets.size
    val checkedInCount = tickets.count { it.checkedIn }
    val remainingCount = totalIssued - checkedInCount
    val totalRevenue = tickets.sumOf { it.amountPaid }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Top Bar with Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("event_detail_back_btn")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = GoldPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = event.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
            }
        }

        item {
            // Event Hero Banner Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = RichBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    if (event.flyerPath == "corporate_flyer" || event.flyerPath == null) {
                        Image(
                            painter = painterResource(id = R.drawable.corporate_flyer),
                            contentDescription = "Event Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = event.flyerPath,
                            contentDescription = "Event Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color(0x66000000), Color(0xF20A0A0A))
                                )
                            )
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text(
                            text = event.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = event.dateFormatted, style = MaterialTheme.typography.labelSmall, color = GoldPrimary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = event.location, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                        }
                    }
                }
            }
        }

        item {
            // Four-Tile Executive Metrics Matrix
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(title = "ISSUED", value = totalIssued.toString(), color = GoldPrimary, modifier = Modifier.weight(1f))
                MetricCard(title = "CHECKED-IN", value = checkedInCount.toString(), color = AccessGrantedGreen, modifier = Modifier.weight(1f))
                MetricCard(title = "REMAINING", value = remainingCount.toString(), color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                MetricCard(title = "REVENUE", value = "$${String.format(java.util.Locale.US, "%.0f", totalRevenue)}", color = GoldPrimary, modifier = Modifier.weight(1f))
            }
        }

        item {
            // Primary Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ISSUED TICKETS (${filteredTickets.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )

                Button(
                    onClick = { showIssueTicketDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = RichBlack),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("issue_ticket_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Issue Ticket", fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterStatus == "ALL",
                    onClick = { filterStatus = "ALL" },
                    label = { Text("All ($totalIssued)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = RichBlack
                    )
                )
                FilterChip(
                    selected = filterStatus == "CHECKED_IN",
                    onClick = { filterStatus = "CHECKED_IN" },
                    label = { Text("Checked In ($checkedInCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccessGrantedGreen,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = filterStatus == "PENDING",
                    onClick = { filterStatus = "PENDING" },
                    label = { Text("Pending ($remainingCount)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GoldPrimary,
                        selectedLabelColor = RichBlack
                    )
                )
            }
        }

        if (filteredTickets.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No tickets matching filter",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredTickets, key = { it.id }) { ticket ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    TicketLandscapeCard(
                        ticket = ticket,
                        event = event,
                        onCardClick = { onViewTicket(ticket) }
                    )

                    // Quick Actions for each ticket
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { onTestScan(ticket.serialNumber) },
                            modifier = Modifier.height(32.dp).testTag("quick_scan_${ticket.serialNumber}"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Scan", style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Button(
                            onClick = { onGenerateReceipt(ticket) },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = RichBlack),
                            modifier = Modifier.height(32.dp).testTag("receipt_btn_${ticket.serialNumber}"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cash Receipt", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (showIssueTicketDialog) {
        IssueTicketDialog(
            onDismiss = { showIssueTicketDialog = false },
            onConfirm = { name, email, cat, amount, curr ->
                onIssueTicket(name, email, cat, amount, curr)
                showIssueTicketDialog = false
            }
        )
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun IssueTicketDialog(
    onDismiss: () -> Unit,
    onConfirm: (attendeeName: String, attendeeEmail: String, category: String, amount: Double, currency: String) -> Unit
) {
    var attendeeName by remember { mutableStateOf("") }
    var attendeeEmail by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Executive VIP") }
    var amountText by remember { mutableStateOf("500.00") }
    var currency by remember { mutableStateOf("USD") }

    val categories = listOf("Executive VIP", "Delegate", "Keynote Speaker", "General Admission")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Issue Executive Ticket",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = attendeeName,
                    onValueChange = { attendeeName = it },
                    label = { Text("Attendee Full Name *") },
                    placeholder = { Text("e.g. Jonathan R. Hastings") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_attendee_name"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                OutlinedTextField(
                    value = attendeeEmail,
                    onValueChange = { attendeeEmail = it },
                    label = { Text("Corporate Email (Optional)") },
                    placeholder = { Text("e.g. j.hastings@firm.com") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("dialog_attendee_email"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount Paid *") },
                        placeholder = { Text("0.00") },
                        singleLine = true,
                        modifier = Modifier.weight(1.2f).testTag("dialog_amount_paid"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it },
                        label = { Text("Currency *") },
                        placeholder = { Text("e.g. USD, EUR, UGX") },
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("dialog_ticket_currency"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                Text(
                    text = "Ticket Tier Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.take(2).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.drop(2).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (attendeeName.isNotBlank()) {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        onConfirm(attendeeName, attendeeEmail, selectedCategory, amount, currency)
                    }
                },
                enabled = attendeeName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = RichBlack),
                modifier = Modifier.testTag("confirm_issue_ticket_btn")
            ) {
                Text("Generate Ticket", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
