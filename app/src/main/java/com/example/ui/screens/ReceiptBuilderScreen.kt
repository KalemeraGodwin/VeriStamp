package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CorporateEntity
import com.example.data.model.EntityProfile
import com.example.data.model.EventEntity
import com.example.data.model.TicketEntity
import com.example.ui.components.SignatureCanvas
import com.example.ui.components.StrokeLine
import com.example.ui.components.exportStrokesToBitmap
import com.example.ui.theme.AccessGrantedGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack
import com.example.util.DateUtil
import com.example.util.QrCodeUtil
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiptBuilderScreen(
    profile: EntityProfile?,
    entities: List<CorporateEntity>,
    ticket: TicketEntity?,
    event: EventEntity?,
    onBack: () -> Unit,
    onNavigateToEntityManager: () -> Unit,
    onCompileReceipt: (
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
        signatoryRole: String,
        onSuccess: (File) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current

    // Entity Selection: Must have at least 1 entity to issue receipt
    var selectedEntityId by remember(entities) {
        mutableStateOf(entities.firstOrNull()?.id)
    }
    val selectedEntity = entities.find { it.id == selectedEntityId } ?: entities.firstOrNull()

    var attendeeName by remember { mutableStateOf(ticket?.attendeeName ?: "Jonathan Vance") }
    var amountText by remember { mutableStateOf(ticket?.amountPaid?.toString() ?: "500.00") }
    var currency by remember(selectedEntity) { mutableStateOf(selectedEntity?.currency ?: "USD") }
    var cashChequeNo by remember { mutableStateOf("CASH-REF-0082") }
    var signatoryRole by remember { mutableStateOf("Authorized Signatory") }
    var forDescription by remember {
        mutableStateOf(
            if (ticket != null) "${event?.name ?: "Corporate Gala"} - ${ticket.category} Registration Pass"
            else "Corporate Event Admission & Executive Delegate Access"
        )
    }

    val liveTimestamp = remember { DateUtil.getCurrentReceiptDateTime() }
    val receiptSerial = remember { DateUtil.generateReceiptNumber() }

    val qrBitmap = remember(receiptSerial) {
        QrCodeUtil.generateQrBitmap(
            content = receiptSerial,
            sizePx = 200,
            foregroundColor = android.graphics.Color.BLACK,
            backgroundColor = android.graphics.Color.WHITE
        )
    }

    val strokes = remember { mutableStateListOf<StrokeLine>() }
    var strokesVersion by remember { mutableStateOf(0) }

    var compiledPdfFile by remember { mutableStateOf<File?>(null) }
    var isCompiling by remember { mutableStateOf(false) }

    var entityDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Navigation Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("receipt_back_btn")) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = GoldPrimary)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    text = "DIGITAL CASH RECEIPT",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GoldPrimary,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Authenticated Payment Proof with Entity Logo & QR Seal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Entity Requirement Guard: "actually one should 1st create an entity to create a receipt where they upload the logo and name of the entity."
        if (entities.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("no_entity_warning_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Business,
                        contentDescription = null,
                        tint = GoldPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Entity Registration Required",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "To issue an official receipt, you must first create an entity where you upload your company logo and name.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToEntityManager,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = RichBlack),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("create_entity_first_btn")
                    ) {
                        Text("Register Corporate Entity First", fontWeight = FontWeight.Bold)
                    }
                }
            }
            return
        }

        // Entity Selector Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ISSUING ENTITY:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = GoldPrimary
                    )
                    Text(
                        text = "Manage Entities (${entities.size}/10)",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        modifier = Modifier.clickable { onNavigateToEntityManager() }
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = entityDropdownExpanded,
                    onExpandedChange = { entityDropdownExpanded = !entityDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedEntity?.name ?: "Select Entity",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = entityDropdownExpanded) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = entityDropdownExpanded,
                        onDismissRequest = { entityDropdownExpanded = false }
                    ) {
                        entities.forEach { entity ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(entity.name, fontWeight = FontWeight.Bold)
                                        Text("Currency: ${entity.currency}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedEntityId = entity.id
                                    currency = entity.currency
                                    entityDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Digital Card Layout matching Cash Receipt Sample Blueprint Exactly
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("receipt_blueprint_card"),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header stating "Cash Receipt", displaying VeriStamp branding layout alongside Entity Logo & Name & Address
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        // Display the selected entity's uploaded logo!
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(RichBlack)
                                .border(1.5.dp, GoldPrimary, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedEntity?.logoPath != null && File(selectedEntity.logoPath).exists()) {
                                AsyncImage(
                                    model = File(selectedEntity.logoPath),
                                    contentDescription = selectedEntity.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.veristamp_icon),
                                    contentDescription = "Entity Logo",
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = (selectedEntity?.name ?: "VERISTAMP CORPORATE").uppercase(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = selectedEntity?.physicalAddress?.ifEmpty { "Corporate Financial District" } ?: "Corporate Financial District",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                            Text(
                                text = "Reg: ${selectedEntity?.registrationNumber?.ifEmpty { "VSTAMP-CORP-01" } ?: "VSTAMP-CORP-01"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp
                            )
                        }
                    }

                    // Top Banner Tag: "Cash Receipt"
                    Surface(
                        color = RichBlack,
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                    ) {
                        Text(
                            text = "CASH RECEIPT",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                HorizontalDivider(color = GoldBorder, thickness = 1.dp)

                // Real-Time System Timestamp & Unique Number Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "RECEIPT NO: $receiptSerial",
                            style = MaterialTheme.typography.labelSmall,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                    }
                    Text(
                        text = liveTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Text Data Field 1: Received From: [Attendee Name]
                Column {
                    Text(
                        text = "Received From:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = attendeeName,
                        onValueChange = { attendeeName = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("receipt_input_attendee"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Text Data Field 2: Amount & Currency (User Defined) & Cash/Cheque No
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1.1f)) {
                        Text(
                            text = "Amount Paid:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("receipt_input_amount"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(0.9f)) {
                        Text(
                            text = "Currency:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = currency,
                            onValueChange = { currency = it },
                            placeholder = { Text("e.g. USD") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("receipt_input_currency"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text(
                            text = "Cash / Ref No:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = cashChequeNo,
                            onValueChange = { cashChequeNo = it },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("receipt_input_cheque_no"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = GoldPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    }
                }

                // Text Data Field 3: "For:" wide narrative container
                Column {
                    Text(
                        text = "For (Event / Purpose Description):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = forDescription,
                        onValueChange = { forDescription = it },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("receipt_input_for"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Signatory Role Input: "let the signatory enter their role"
                Column {
                    Text(
                        text = "Signatory Role / Designation:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = signatoryRole,
                        onValueChange = { signatoryRole = it },
                        placeholder = { Text("e.g. Authorized Signatory, Finance Director, Cashier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("receipt_input_signatory_role"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }

                // Bottom Section:
                // Signature Canvas (Finger signing + Rubber eraser)
                // Stamp square with embedded verification QR code!
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Bottom Left: Touchscreen Signature Canvas with Pen and Rubber
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Signatory Signature:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SignatureCanvas(
                            strokes = strokes,
                            onStrokesChanged = { strokesVersion++ }
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Signature line representation with role fitting on top
                        androidx.compose.material3.HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = signatoryRole.ifEmpty { "Authorized Signatory" },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 10.sp
                        )
                    }

                    // Bottom Right: Stamp Block with Verification QR Code
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(105.dp)
                    ) {
                        Text(
                            text = "Stamp & QR",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = GoldPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(95.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .border(1.5.dp, GoldPrimary, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                                .testTag("receipt_stamp_box"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Receipt Verification QR",
                                    modifier = Modifier.size(68.dp)
                                )
                                Text(
                                    text = receiptSerial,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 7.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RichBlack
                                )
                            }
                        }
                    }
                }
            }
        }

        // Native PDF Compilation Action
        Button(
            onClick = {
                isCompiling = true
                val amount = amountText.toDoubleOrNull() ?: 0.0
                val sigBitmap = if (strokes.isNotEmpty()) exportStrokesToBitmap(strokes) else null

                onCompileReceipt(
                    selectedEntity?.id,
                    ticket?.id,
                    ticket?.eventId ?: event?.id,
                    event?.name ?: "Corporate Gala",
                    attendeeName,
                    amount,
                    currency.trim().ifEmpty { "USD" },
                    cashChequeNo,
                    forDescription,
                    sigBitmap,
                    signatoryRole.trim().ifEmpty { "Authorized Signatory" }
                ) { file ->
                    compiledPdfFile = file
                    isCompiling = false
                    Toast.makeText(context, "Saved to ${file.name}", Toast.LENGTH_LONG).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("download_receipt_pdf_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary, contentColor = RichBlack),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCompiling) "Compiling PDF..." else "Download Receipt PDF",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Compiled PDF Confirmation Card
        if (compiledPdfFile != null) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("compiled_pdf_card"),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = RichBlack),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccessGrantedGreen)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = AccessGrantedGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PDF Generated: ${compiledPdfFile?.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val uri: Uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    compiledPdfFile!!
                                )
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "application/pdf")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Open Receipt PDF"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AccessGrantedGreen, contentColor = Color.White),
                            modifier = Modifier.weight(1f).testTag("open_pdf_btn")
                        ) {
                            Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open PDF", style = MaterialTheme.typography.labelSmall)
                        }

                        OutlinedButton(
                            onClick = {
                                val uri: Uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    compiledPdfFile!!
                                )
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Receipt PDF"))
                            },
                            modifier = Modifier.weight(1f).testTag("share_pdf_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldPrimary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Share PDF", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
