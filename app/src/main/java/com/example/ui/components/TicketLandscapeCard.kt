package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.EventEntity
import com.example.data.model.TicketEntity
import com.example.ui.theme.AccessGrantedGreen
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack
import com.example.util.QrCodeUtil

@Composable
fun TicketLandscapeCard(
    modifier: Modifier = Modifier,
    ticket: TicketEntity,
    event: EventEntity?,
    onCardClick: (() -> Unit)? = null
) {
    // Generate QR code bitmap for stub
    val qrBitmap: Bitmap = remember(ticket.serialNumber) {
        QrCodeUtil.generateQrBitmap(
            content = ticket.serialNumber,
            sizePx = 320,
            foregroundColor = android.graphics.Color.BLACK,
            backgroundColor = android.graphics.Color.WHITE
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ticket_card_${ticket.serialNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = RichBlack),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = { onCardClick?.invoke() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // LEFT SIDE: Flyer Banner & Attendee Details (Takes 65% width)
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    // Background Flyer Image
                    if (event?.flyerPath == "corporate_flyer" || event?.flyerPath == null) {
                        Image(
                            painter = painterResource(id = R.drawable.corporate_flyer),
                            contentDescription = "Event Flyer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = event.flyerPath,
                            contentDescription = "Event Flyer",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Luxury dark gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xE60A0A0A),
                                        Color(0xCC0A0A0A),
                                        Color(0xF00A0A0A)
                                    )
                                )
                            )
                    )

                    // Text Content over Banner
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top row: Event Name & Category Tier
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = GoldPrimary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = ticket.category.uppercase(),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = RichBlack,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${ticket.currencySymbol}${String.format(java.util.Locale.US, "%.0f", ticket.amountPaid)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = event?.name ?: "Corporate Leadership Event",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Middle: Attendee Full Name
                        Column {
                            Text(
                                text = "ATTENDEE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFAAAAAA),
                                fontSize = 10.sp
                            )
                            Text(
                                text = ticket.attendeeName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Bottom: Ticket ID & Real-Time System Timestamp
                        Column {
                            Text(
                                text = "SERIAL: ${ticket.serialNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldPrimary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = ticket.generatedTimestamp,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCCCCCC),
                                fontSize = 9.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // PERFORATION SEPARATOR: Vertical dashed gold line with notch notches
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxHeight().width(1.dp)) {
                        drawLine(
                            color = GoldPrimary,
                            start = Offset(0f, 15f),
                            end = Offset(0f, size.height - 15f),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    // Top notch cutout
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.TopCenter)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, GoldBorder, CircleShape)
                    )

                    // Bottom notch cutout
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomCenter)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .border(1.dp, GoldBorder, CircleShape)
                    )
                }

                // RIGHT SIDE: Verification Stub with QR Code
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight()
                        .background(Color(0xFF141414))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "VERIFICATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = GoldPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )

                    // QR Code Image
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Ticket QR Verification Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Verification State Status Badge
                    if (ticket.checkedIn) {
                        Surface(
                            color = AccessGrantedGreen.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AccessGrantedGreen)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = AccessGrantedGreen,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "CHECKED-IN",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AccessGrantedGreen,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = GoldPrimary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary)
                        ) {
                            Text(
                                text = "READY / VALID",
                                style = MaterialTheme.typography.labelSmall,
                                color = GoldPrimary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
