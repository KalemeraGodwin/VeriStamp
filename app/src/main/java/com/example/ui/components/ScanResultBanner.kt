package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.CheckInResult
import com.example.ui.theme.AccessDeniedRed
import com.example.ui.theme.AccessGrantedGreen
import com.example.ui.theme.AlreadyScannedAmber
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack

@Composable
fun ScanResultBanner(
    modifier: Modifier = Modifier,
    result: CheckInResult?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = result != null,
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
        modifier = modifier
    ) {
        if (result == null) return@AnimatedVisibility

        when (result) {
            is CheckInResult.Granted -> {
                // Condition 1: Gold & Black prominent banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RichBlack)
                        .border(2.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("banner_access_granted")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Access Granted",
                                tint = RichBlack,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "VERISTAMP ACCESS GRANTED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = result.ticket.attendeeName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tier: ${result.ticket.category} • Log: ${result.logTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD4D4D4),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_banner_btn")) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = GoldPrimary)
                        }
                    }
                }
            }

            is CheckInResult.ReceiptVerified -> {
                // Receipt Verification Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RichBlack)
                        .border(2.dp, GoldPrimary, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("banner_receipt_verified")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(GoldPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Receipt Verified",
                                tint = RichBlack,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OFFICIAL RECEIPT VERIFIED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${result.receipt.attendeeName} • ${result.receipt.currencySymbol} ${String.format(java.util.Locale.US, "%.2f", result.receipt.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Issued by: ${result.receipt.entityName} • Ref: ${result.receipt.receiptNumber}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFD4D4D4),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_banner_btn")) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = GoldPrimary)
                        }
                    }
                }
            }

            is CheckInResult.AlreadyScanned -> {
                // Condition 2: Warning Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF241505))
                        .border(1.5.dp, AlreadyScannedAmber, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("banner_already_scanned")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AlreadyScannedAmber),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Already Scanned",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INVALID: TICKET SCANNED PREVIOUSLY",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AlreadyScannedAmber,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Attendee: ${result.ticket.attendeeName}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Previously Logged: ${result.previousLogTime}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFFFD180),
                                fontSize = 11.sp
                            )
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_banner_btn")) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = AlreadyScannedAmber)
                        }
                    }
                }
            }

            is CheckInResult.Denied -> {
                // Condition 3: Red Alert Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A0808))
                        .border(1.5.dp, AccessDeniedRed, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                        .testTag("banner_access_denied")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccessDeniedRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cancel,
                                contentDescription = "Access Denied",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACCESS DENIED",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = AccessDeniedRed,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Invalid QR Code or Unrecognized Serial",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                            if (result.scannedSerial.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Scanned: ${result.scannedSerial}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color(0xFFFF8A80),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_banner_btn")) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Dismiss", tint = AccessDeniedRed)
                        }
                    }
                }
            }
        }
    }
}
