package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DateUtil {

    fun getSystemTimeZone(): TimeZone = TimeZone.getDefault()

    fun getSystemTimeZoneId(): String = TimeZone.getDefault().id

    fun getSystemTimeZoneDisplayName(): String =
        TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT, Locale.getDefault())

    private fun createFormatter(pattern: String): SimpleDateFormat {
        return SimpleDateFormat(pattern, Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }
    }

    fun getCurrentTicketTimestamp(): String {
        val now = Date()
        val formatter = createFormatter("yyyy-MM-dd HH:mm:ss z")
        return "Generated: " + formatter.format(now)
    }

    fun getCurrentReceiptDateTime(): String {
        val formatter = createFormatter("MMMM dd, yyyy - hh:mm:ss a z")
        return formatter.format(Date())
    }

    fun formatCurrentLogTime(): String {
        val formatter = createFormatter("yyyy-MM-dd HH:mm:ss z")
        return formatter.format(Date())
    }

    fun formatEventDate(millis: Long): String {
        val formatter = createFormatter("MMM dd, yyyy")
        return formatter.format(Date(millis))
    }

    fun formatLogTime(millis: Long): String {
        val formatter = createFormatter("HH:mm:ss z")
        return formatter.format(Date(millis))
    }

    fun generateTicketSerial(): String {
        val formatter = createFormatter("yyyy")
        val year = formatter.format(Date())
        val chars = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ"
        val randomPart = (1..5).map { chars.random() }.joinToString("")
        return "VSTAMP-$year-$randomPart"
    }

    fun generateReceiptNumber(): String {
        val formatter = createFormatter("yyyy")
        val year = formatter.format(Date())
        val randomNum = (1000..9999).random()
        return "RCT-$year-$randomNum"
    }
}
