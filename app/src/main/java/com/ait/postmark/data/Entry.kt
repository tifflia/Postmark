package com.ait.postmark.data

import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Entry(
    var id: String = "",
    var date: String = "",            // ISO date i.e. "2026-04-14"
    var location: String = "",        // "City, Country"
    var geo: GeoPoint? = null,        // For the map view
    var body: String = "",
    var photoUrl: String? = null,     // Public Supabase URL for display
    var photoPath: String? = null,    // Storage path ("uid/name.jpg") for deletion; null on legacy entries
    @ServerTimestamp var createdAt: Date? = null
)
