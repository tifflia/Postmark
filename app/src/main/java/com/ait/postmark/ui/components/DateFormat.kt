package com.ait.postmark.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Turns "2026-04-14" into "April 14, 2026". */
fun formatIsoDate(iso: String): String = try {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    formatter.format(parser.parse(iso)!!)
} catch (e: Exception) {
    iso
}

private fun utcIsoFormatter() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

/**
 * Converts a UTC epoch-millis value (as produced by Material's date pickers)
 * into an ISO "yyyy-MM-dd" string.
 */
fun isoFromUtcMillis(millis: Long): String = utcIsoFormatter().format(Date(millis))

/**
 * Converts an ISO "yyyy-MM-dd" string into UTC epoch millis for pre-selecting
 * a date picker, or null if it can't be parsed.
 */
fun utcMillisFromIso(iso: String): Long? = try {
    utcIsoFormatter().parse(iso)?.time
} catch (e: Exception) {
    null
}

/** Human-readable summary of a date range for the filter chip. */
fun formatDateRange(start: String?, end: String?): String = when {
    start != null && end != null -> "${formatIsoDate(start)} – ${formatIsoDate(end)}"
    start != null -> "From ${formatIsoDate(start)}"
    end != null -> "Until ${formatIsoDate(end)}"
    else -> ""
}
