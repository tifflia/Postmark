package com.ait.postmark.ui.components

import java.text.SimpleDateFormat
import java.util.Locale

/** Turns "2026-04-14" into "April 14, 2026". */
fun formatIsoDate(iso: String): String = try {
    val parser = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    val formatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
    formatter.format(parser.parse(iso)!!)
} catch (e: Exception) {
    iso
}
