package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import com.example.ui.Screen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RichBlack

@Composable
fun VeriStampBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    // Only show on main parent screens
    val isMainTab = currentScreen in listOf(
        Screen.EVENTS,
        Screen.EVENT_DETAIL,
        Screen.SCANNER,
        Screen.RECEIPTS_LIST,
        Screen.AUDIT_LOGS
    )

    if (!isMainTab) return

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("main_bottom_nav")
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.EVENTS || currentScreen == Screen.EVENT_DETAIL,
            onClick = { onNavigate(Screen.EVENTS) },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RichBlack,
                selectedTextColor = GoldPrimary,
                indicatorColor = GoldPrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_events")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.SCANNER,
            onClick = { onNavigate(Screen.SCANNER) },
            icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = "Scanner") },
            label = { Text("Scan Pass", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RichBlack,
                selectedTextColor = GoldPrimary,
                indicatorColor = GoldPrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_scanner")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.RECEIPTS_LIST,
            onClick = { onNavigate(Screen.RECEIPTS_LIST) },
            icon = { Icon(Icons.Default.ReceiptLong, contentDescription = "Receipts") },
            label = { Text("Receipts", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RichBlack,
                selectedTextColor = GoldPrimary,
                indicatorColor = GoldPrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_receipts")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.AUDIT_LOGS,
            onClick = { onNavigate(Screen.AUDIT_LOGS) },
            icon = { Icon(Icons.Default.History, contentDescription = "Audit Trail") },
            label = { Text("Audit Log", style = MaterialTheme.typography.labelSmall) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = RichBlack,
                selectedTextColor = GoldPrimary,
                indicatorColor = GoldPrimary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_item_audit")
        )
    }
}
