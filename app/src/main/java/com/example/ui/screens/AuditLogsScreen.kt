package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.CheckInLogEntity
import com.example.ui.theme.AccessDeniedRed
import com.example.ui.theme.AccessGrantedGreen
import com.example.ui.theme.AlreadyScannedAmber
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack
import com.example.util.DateUtil

@Composable
fun AuditLogsScreen(logs: List<CheckInLogEntity>) {
    var selectedCategory by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var dialogTableEvent by remember { mutableStateOf<Pair<String, List<CheckInLogEntity>>?>(null) }

    val systemTimeZoneName = remember { DateUtil.getSystemTimeZoneDisplayName() }
    val systemTimeZoneId = remember { DateUtil.getSystemTimeZoneId() }

    // Distinct event categories extracted from logs
    val distinctEvents = remember(logs) {
        logs.map { it.eventName }.filter { it.isNotBlank() && it != "N/A" }.distinct()
    }

    val filteredLogs = remember(logs, selectedCategory, searchQuery) {
        logs.filter { log ->
            val matchesCategory = when (selectedCategory) {
                "ALL" -> true
                "RECEIPTS" -> log.recordType == "RECEIPT"
                "DENIED" -> log.status == "DENIED"
                else -> log.eventName.equals(selectedCategory, ignoreCase = true)
            }
            val matchesSearch = if (searchQuery.isBlank()) true else {
                log.attendeeName.contains(searchQuery, ignoreCase = true) ||
                    log.serialNumber.contains(searchQuery, ignoreCase = true) ||
                    log.eventName.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    // Group logs by Event for categorized table sections
    val logsGroupedByEvent = remember(filteredLogs, selectedCategory) {
        if (selectedCategory == "ALL") {
            filteredLogs.groupBy { it.eventName.ifBlank { "Uncategorized" } }
        } else {
            mapOf(selectedCategory to filteredLogs)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("audit_logs_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Executive Header Card with phone system timezone indicator
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TableChart, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "SECURITY AUDIT LOG TABLES",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Categorized event telemetry using phone timezone: $systemTimeZoneId ($systemTimeZoneName)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Tap on any table to view the full untrimmed table",
                            style = MaterialTheme.typography.labelSmall,
                            color = GoldPrimary
                        )
                    }

                    Surface(
                        color = GoldPrimary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Text(
                            text = systemTimeZoneName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Search and Event Category Filter Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("audit_logs_search"),
                    placeholder = { Text("Search by attendee, serial code, or event...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = GoldPrimary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                // Category Chips Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().testTag("audit_category_chips")
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == "ALL",
                            onClick = { selectedCategory = "ALL" },
                            label = { Text("All Events (${logs.size})") },
                            leadingIcon = {
                                Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = RichBlack,
                                selectedLeadingIconColor = RichBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == "ALL",
                                borderColor = GoldBorder
                            ),
                            modifier = Modifier.testTag("filter_all")
                        )
                    }

                    items(distinctEvents) { eventName ->
                        val count = logs.count { it.eventName.equals(eventName, ignoreCase = true) }
                        FilterChip(
                            selected = selectedCategory.equals(eventName, ignoreCase = true),
                            onClick = { selectedCategory = eventName },
                            label = { Text("$eventName ($count)") },
                            leadingIcon = {
                                Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = RichBlack,
                                selectedLeadingIconColor = RichBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory.equals(eventName, ignoreCase = true),
                                borderColor = GoldBorder
                            )
                        )
                    }

                    item {
                        val receiptsCount = logs.count { it.recordType == "RECEIPT" }
                        FilterChip(
                            selected = selectedCategory == "RECEIPTS",
                            onClick = { selectedCategory = "RECEIPTS" },
                            label = { Text("Receipts ($receiptsCount)") },
                            leadingIcon = {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GoldPrimary,
                                selectedLabelColor = RichBlack,
                                selectedLeadingIconColor = RichBlack
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == "RECEIPTS",
                                borderColor = GoldBorder
                            ),
                            modifier = Modifier.testTag("filter_receipts")
                        )
                    }

                    item {
                        val deniedCount = logs.count { it.status == "DENIED" }
                        FilterChip(
                            selected = selectedCategory == "DENIED",
                            onClick = { selectedCategory = "DENIED" },
                            label = { Text("Denied ($deniedCount)") },
                            leadingIcon = {
                                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccessDeniedRed,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedCategory == "DENIED",
                                borderColor = AccessDeniedRed
                            ),
                            modifier = Modifier.testTag("filter_denied")
                        )
                    }
                }
            }
        }

        if (filteredLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No Audit Records Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Scan tickets or receipts with the Scanner to record verifiable gate logs.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            // Render Table per Event Category
            logsGroupedByEvent.forEach { (eventName, eventLogs) ->
                item {
                    EventAuditTableCard(
                        categoryName = eventName,
                        logs = eventLogs,
                        timeZoneDisplayName = systemTimeZoneName,
                        onOpenFullTable = {
                            dialogTableEvent = Pair(eventName, eventLogs)
                        }
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    // Modal Full Untrimmed Table Dialog shown on tap
    dialogTableEvent?.let { (eventName, eventLogs) ->
        FullAuditTableDialog(
            categoryName = eventName,
            logs = eventLogs,
            timeZoneDisplayName = systemTimeZoneName,
            onDismiss = { dialogTableEvent = null }
        )
    }
}

@Composable
fun EventAuditTableCard(
    categoryName: String,
    logs: List<CheckInLogEntity>,
    timeZoneDisplayName: String,
    onOpenFullTable: () -> Unit
) {
    var isUntrimmedInline by remember { mutableStateOf(false) }

    val grantedCount = logs.count { it.status == "GRANTED" || it.status == "RECEIPT_VERIFIED" }
    val duplicateCount = logs.count { it.status == "ALREADY_SCANNED" }
    val deniedCount = logs.count { it.status == "DENIED" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_table_${categoryName.hashCode()}")
            .clickable { onOpenFullTable() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Event Table Title & Quick Metric Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = categoryName.uppercase(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.TouchApp,
                            contentDescription = "Tap to view full table",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "Total Entries: ${logs.size} • Granted: $grantedCount • Blocked: $duplicateCount • Denied: $deniedCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }

                // Inline View Mode Toggle & Full Screen Dialog Trigger
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = { isUntrimmedInline = !isUntrimmedInline },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = if (isUntrimmedInline) "Compact" else "Untrimmed",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    IconButton(
                        onClick = onOpenFullTable,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Full Screen Table",
                            tint = GoldPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Tap table or button to see full table without trimming words",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontally Scrollable Table
            val horizontalScrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                    .horizontalScroll(horizontalScrollState)
            ) {
                // Generous width so all columns have plenty of space
                val tableWidth = if (isUntrimmedInline) 850.dp else 700.dp
                Column(modifier = Modifier.width(tableWidth)) {
                    // Table Header Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "TIME ($timeZoneDisplayName)",
                            modifier = Modifier.width(150.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "ATTENDEE / ENTITY",
                            modifier = Modifier.width(if (isUntrimmedInline) 240.dp else 180.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "SERIAL / QR CODE",
                            modifier = Modifier.width(if (isUntrimmedInline) 240.dp else 190.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Text(
                            text = "GATE STATUS",
                            modifier = Modifier.width(if (isUntrimmedInline) 200.dp else 160.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

                    // Table Data Rows
                    logs.forEachIndexed { index, log ->
                        val isEven = index % 2 == 0
                        val rowBg = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

                        val (statusColor, statusLabel, statusIcon) = when (log.status) {
                            "GRANTED" -> Triple(AccessGrantedGreen, "ACCESS GRANTED", Icons.Default.CheckCircle)
                            "RECEIPT_VERIFIED" -> Triple(AccessGrantedGreen, "RECEIPT VALID", Icons.Default.CheckCircle)
                            "ALREADY_SCANNED" -> Triple(AlreadyScannedAmber, "DUPLICATE RE-ENTRY", Icons.Default.Warning)
                            else -> Triple(AccessDeniedRed, "ACCESS DENIED", Icons.Default.Cancel)
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(rowBg)
                                .clickable { onOpenFullTable() }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Timestamp column
                            Text(
                                text = log.timestampFormatted,
                                modifier = Modifier.width(150.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                softWrap = true,
                                maxLines = if (isUntrimmedInline) Int.MAX_VALUE else 1,
                                overflow = if (isUntrimmedInline) TextOverflow.Clip else TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Attendee Name column - no trimming in untrimmed mode
                            Text(
                                text = log.attendeeName,
                                modifier = Modifier.width(if (isUntrimmedInline) 240.dp else 180.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                softWrap = true,
                                maxLines = if (isUntrimmedInline) Int.MAX_VALUE else 1,
                                overflow = if (isUntrimmedInline) TextOverflow.Clip else TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Serial Number column - monospace, no trimming in untrimmed mode
                            Text(
                                text = log.serialNumber,
                                modifier = Modifier.width(if (isUntrimmedInline) 240.dp else 190.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                softWrap = true,
                                maxLines = if (isUntrimmedInline) Int.MAX_VALUE else 1,
                                overflow = if (isUntrimmedInline) TextOverflow.Clip else TextOverflow.Ellipsis,
                                color = GoldPrimary
                            )

                            // Gate Status Column with Badge
                            Surface(
                                color = statusColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp),
                                border = androidx.compose.foundation.BorderStroke(0.5.dp, statusColor),
                                modifier = Modifier.width(if (isUntrimmedInline) 200.dp else 160.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = statusIcon,
                                        contentDescription = null,
                                        tint = statusColor,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = statusLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 9.sp,
                                        softWrap = true,
                                        maxLines = if (isUntrimmedInline) Int.MAX_VALUE else 1
                                    )
                                }
                            }
                        }

                        if (index < logs.size - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FullAuditTableDialog(
    categoryName: String,
    logs: List<CheckInLogEntity>,
    timeZoneDisplayName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .testTag("full_audit_table_dialog"),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Dialog Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "COMPLETE AUDIT TABLE: ${categoryName.uppercase()}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "Full untrimmed text view • Phone Timezone: $timeZoneDisplayName",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_full_table_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = GoldPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = GoldBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Table Container (Both Horizontal & Vertical)
                val horizontalScroll = rememberScrollState()
                val verticalScroll = rememberScrollState()

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                        .verticalScroll(verticalScroll)
                        .horizontalScroll(horizontalScroll)
                ) {
                    Column(modifier = Modifier.width(960.dp)) {
                        // Header Row with No Trimming
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 12.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TIMESTAMP ($timeZoneDisplayName)",
                                modifier = Modifier.width(180.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "EVENT / CATEGORY",
                                modifier = Modifier.width(180.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "ATTENDEE / ENTITY NAME (FULL)",
                                modifier = Modifier.width(220.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "SERIAL / QR CODE NUMBER",
                                modifier = Modifier.width(220.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                            Text(
                                text = "GATE VERIFICATION STATUS",
                                modifier = Modifier.width(160.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }

                        HorizontalDivider(color = GoldPrimary, thickness = 1.dp)

                        // Data Rows with Full Word Wrap & Zero Trimming
                        logs.forEachIndexed { index, log ->
                            val isEven = index % 2 == 0
                            val rowBg = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

                            val (statusColor, statusLabel, statusIcon) = when (log.status) {
                                "GRANTED" -> Triple(AccessGrantedGreen, "ACCESS GRANTED", Icons.Default.CheckCircle)
                                "RECEIPT_VERIFIED" -> Triple(AccessGrantedGreen, "RECEIPT VALID", Icons.Default.CheckCircle)
                                "ALREADY_SCANNED" -> Triple(AlreadyScannedAmber, "DUPLICATE RE-ENTRY", Icons.Default.Warning)
                                else -> Triple(AccessDeniedRed, "ACCESS DENIED", Icons.Default.Cancel)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(rowBg)
                                    .padding(vertical = 12.dp, horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Timestamp - Full word wrap
                                Text(
                                    text = log.timestampFormatted,
                                    modifier = Modifier.width(180.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    softWrap = true,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Event / Category - Full word wrap
                                Text(
                                    text = log.eventName,
                                    modifier = Modifier.width(180.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 12.sp,
                                    softWrap = true,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Attendee Name - Full word wrap (no ellipsis)
                                Text(
                                    text = log.attendeeName,
                                    modifier = Modifier.width(220.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = true,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                // Serial Number - Full monospace
                                Text(
                                    text = log.serialNumber,
                                    modifier = Modifier.width(220.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    softWrap = true,
                                    color = GoldPrimary
                                )

                                // Gate Status Badge
                                Surface(
                                    color = statusColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor),
                                    modifier = Modifier.width(160.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = statusIcon,
                                            contentDescription = null,
                                            tint = statusColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = statusColor,
                                            fontWeight = FontWeight.Bold,
                                            softWrap = true
                                        )
                                    }
                                }
                            }

                            if (index < logs.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), thickness = 0.5.dp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = RichBlack),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Done / Close", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
