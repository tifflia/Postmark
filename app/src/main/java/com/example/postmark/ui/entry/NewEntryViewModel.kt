package com.example.postmark.ui.entry

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postmark.data.Entry
import com.example.postmark.data.EntryRepository
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.resume

data class NewEntryUiState(
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val location: String = "",
    val geo: GeoPoint? = null,
    val body: String = "",
    val saving: Boolean = false,
    val saved: Boolean = false
)

class NewEntryViewModel(
    private val repo: EntryRepository = EntryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NewEntryUiState())
    val state = _state.asStateFlow()

    fun onLocationChange(v: String) = _state.update { it.copy(location = v) }
    fun onBodyChange(v: String) = _state.update { it.copy(body = v) }

    /**
     * Pulls the device's current location using FusedLocationProvider.
     * Caller must request ACCESS_FINE_LOCATION permission first.
     *
     * In production, also do reverse geocoding (Geocoder) to fill the
     * location field with "City, Country" instead of leaving it blank.
     */
    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(context: Context) {
        viewModelScope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                
                // 1. Try last location first
                var loc = client.lastLocation.await()
                
                // 2. If null (common on emulators), request current location
                if (loc == null) {
                    loc = client.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()
                }

                if (loc == null) return@launch
                
                // Update coordinates for Firestore
                val geoPoint = GeoPoint(loc.latitude, loc.longitude)
                _state.update { it.copy(geo = geoPoint) }

                // Reverse geocode to get a human-readable string (City, Country)
                val addressLine = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    suspendCancellableCoroutine { continuation ->
                        try {
                            Geocoder(context, Locale.getDefault())
                                .getFromLocation(loc.latitude, loc.longitude, 1) { addresses ->
                                    continuation.resume(formatAddress(addresses.firstOrNull()))
                                }
                        } catch (e: Exception) {
                            continuation.resume(null)
                        }
                    }
                } else {
                    withContext(Dispatchers.IO) {
                        try {
                            @Suppress("DEPRECATION")
                            val addresses = Geocoder(context, Locale.getDefault())
                                .getFromLocation(loc.latitude, loc.longitude, 1)
                            formatAddress(addresses?.firstOrNull())
                        } catch (e: Exception) {
                            null
                        }
                    }
                }

                if (!addressLine.isNullOrBlank()) {
                    _state.update { it.copy(location = addressLine) }
                }
            } catch (_: SecurityException) {
                // Permission not granted — silently skip
            }
        }
    }

    private fun formatAddress(addr: Address?): String? {
        if (addr == null) return null
        val city = addr.locality ?: addr.subAdminArea ?: ""
        val country = addr.countryName ?: ""
        return if (city.isNotBlank() && country.isNotBlank()) "$city, $country"
        else city.ifBlank { country }.takeIf { it.isNotBlank() }
    }

    fun save() {
        val s = _state.value
        if (s.body.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }
            repo.add(
                Entry(
                    date = s.date,
                    location = s.location.trim(),
                    geo = s.geo,
                    body = s.body.trim()
                )
            )
            _state.update { it.copy(saving = false, saved = true) }
        }
    }
}
