package com.ait.postmark.ui.entry

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ait.postmark.data.Entry
import com.ait.postmark.data.EntryRepository
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

enum class LocationMode { DETECT, CUSTOM, MAP }

data class NewEntryUiState(
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    val location: String = "",
    val geo: GeoPoint? = null,
    val body: String = "",
    val locationMode: LocationMode = LocationMode.DETECT,
    val history: List<String> = emptyList(),
    val selectedImageUri: Uri? = null,
    val existingPhotoUrl: String? = null,
    val editingId: String? = null,
    val isMapExpanded: Boolean = false,
    val saving: Boolean = false,
    val saved: Boolean = false
) {
    val isEditing: Boolean get() = editingId != null
}

class NewEntryViewModel(
    private val repo: EntryRepository = EntryRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(NewEntryUiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                repo.observeEntries().collect { entries ->
                    val recentLocations = entries
                        .map { it.location }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(10)
                    _state.update { it.copy(history = recentLocations) }
                }
            } catch (e: Exception) {
                // Not signed in or other error, history stays default or empty
            }
        }
    }

    private var loadedForEdit = false

    /**
     * Loads an existing entry into the form so it can be edited. Populates the
     * fields once from the first snapshot; later snapshots are ignored so they
     * don't clobber edits in progress.
     */
    fun loadForEdit(entryId: String) {
        if (loadedForEdit || _state.value.editingId == entryId) return
        loadedForEdit = true
        viewModelScope.launch {
            try {
                repo.observeEntries().collect { entries ->
                    // Populate only from the first matching snapshot; once the form
                    // holds the entry, ignore later updates so edits aren't clobbered.
                    if (_state.value.editingId != null) return@collect
                    val e = entries.firstOrNull { it.id == entryId } ?: return@collect
                    _state.update {
                        it.copy(
                            editingId = e.id,
                            date = e.date,
                            location = e.location,
                            geo = e.geo,
                            body = e.body,
                            existingPhotoUrl = e.photoUrl,
                            // Preserve the original location intent: an entry with
                            // coordinates keeps its pin; otherwise treat it as custom.
                            locationMode = if (e.geo != null) LocationMode.MAP else LocationMode.CUSTOM
                        )
                    }
                }
            } catch (_: Exception) {
                // Not signed in or entry unavailable — leave the form empty.
            }
        }
    }

    fun onLocationChange(v: String) = _state.update { it.copy(location = v) }
    fun onBodyChange(v: String) = _state.update { it.copy(body = v) }
    fun onImageSelected(uri: Uri?) = _state.update { it.copy(selectedImageUri = uri) }

    /** Clears both a freshly picked photo and any existing one on an edited entry. */
    fun onImageRemoved() = _state.update { it.copy(selectedImageUri = null, existingPhotoUrl = null) }

    fun setLocationMode(mode: LocationMode) {
        _state.update { it.copy(locationMode = mode) }
    }

    fun setMapExpanded(expanded: Boolean) {
        _state.update { it.copy(isMapExpanded = expanded) }
    }

    fun onMapClick(latLng: com.google.android.gms.maps.model.LatLng, context: Context) {
        val geoPoint = GeoPoint(latLng.latitude, latLng.longitude)
        _state.update { it.copy(geo = geoPoint) }
        viewModelScope.launch {
            reverseGeocode(context, latLng.latitude, latLng.longitude)
        }
    }

    /**
     * Pulls the device's current location using FusedLocationProvider.
     * Caller must request ACCESS_FINE_LOCATION permission first.
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

                reverseGeocode(context, loc.latitude, loc.longitude)
            } catch (_: SecurityException) {
                // Permission not granted — silently skip
            }
        }
    }

    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double) {
        // Reverse geocode to get a human-readable string (City, Country)
        val addressLine = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                try {
                    Geocoder(context, Locale.getDefault())
                        .getFromLocation(latitude, longitude, 1) { addresses ->
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
                        .getFromLocation(latitude, longitude, 1)
                    formatAddress(addresses?.firstOrNull())
                } catch (e: Exception) {
                    null
                }
            }
        }

        if (!addressLine.isNullOrBlank()) {
            _state.update { it.copy(location = addressLine) }
        }
    }

    private fun formatAddress(addr: Address?): String? {
        if (addr == null) return null
        val city = addr.locality ?: addr.subAdminArea ?: ""
        val country = addr.countryName ?: ""
        return if (city.isNotBlank() && country.isNotBlank()) "$city, $country"
        else city.ifBlank { country }.takeIf { it.isNotBlank() }
    }

    fun save(contentResolver: android.content.ContentResolver) {
        val s = _state.value
        if (s.body.isBlank()) return
        viewModelScope.launch {
            _state.update { it.copy(saving = true) }

            try {
                // Upload a newly picked photo; otherwise keep whatever the entry
                // already had (null once the user removes it).
                val photoUrl = s.selectedImageUri?.let { uri ->
                    repo.uploadPhoto(uri, contentResolver)
                } ?: s.existingPhotoUrl

                // In CUSTOM mode, we intentionally drop the GeoPoint
                val finalGeo = if (s.locationMode == LocationMode.CUSTOM) null else s.geo

                val entry = Entry(
                    id = s.editingId.orEmpty(),
                    date = s.date,
                    location = s.location.trim(),
                    geo = finalGeo,
                    body = s.body.trim(),
                    photoUrl = photoUrl
                )

                if (s.isEditing) repo.update(entry) else repo.add(entry)
                _state.update { it.copy(saving = false, saved = true) }
            } catch (e: Exception) {
                // Handle upload or save error
                _state.update { it.copy(saving = false) }
            }
        }
    }
}
