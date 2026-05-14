package com.example.postmark.ui.map

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.postmark.R
import com.example.postmark.ui.components.PostmarkOverflowMenu
import com.example.postmark.ui.list.EntriesViewModel
import com.example.postmark.ui.theme.InkBlack
import com.example.postmark.ui.theme.MutedStone
import com.example.postmark.ui.theme.Parchment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@SuppressLint("MissingPermission")
@Composable
fun MapScreen(
    onOpenEntry: (String) -> Unit,
    onNewEntry: () -> Unit,
    onSwitchToList: () -> Unit,
    vm: EntriesViewModel = viewModel()
) {
    val entries by vm.entries.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> /* Result handled by the button click logic usually */ }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(20.0, 0.0), 2f)
    }

    val moveToUserLocation = {
        scope.launch {
            try {
                val client = LocationServices.getFusedLocationProviderClient(context)
                var loc = client.lastLocation.await()
                if (loc == null) {
                    loc = client.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        CancellationTokenSource().token
                    ).await()
                }
                loc?.let {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12f)
                    )
                }
            } catch (e: SecurityException) {
                locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Parchment)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 8.dp, top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(stringResource(R.string.your_map), style = MaterialTheme.typography.labelMedium, color = MutedStone)
                    Spacer(Modifier.height(4.dp))
                    val n = entries.count { it.geo != null }
                    Text(
                        "$n ${if (n == 1) stringResource(R.string.place) else stringResource(R.string.places)}",
                        style = MaterialTheme.typography.headlineMedium,
                        color = InkBlack
                    )
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.Menu, contentDescription = stringResource(R.string.menu_desc), tint = InkBlack)
                    }
                    PostmarkOverflowMenu(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        isOnListView = false,
                        onSwitchView = onSwitchToList,
                        onDeleteAll = { vm.deleteAll() }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .padding(bottom = 16.dp)
            ) {
                GoogleMap(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp)),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    entries.forEach { entry ->
                        val geo = entry.geo ?: return@forEach
                        Marker(
                            state = MarkerState(LatLng(geo.latitude, geo.longitude)),
                            title = entry.location,
                            snippet = entry.date,
                            onClick = {
                                onOpenEntry(entry.id)
                                true
                            }
                        )
                    }
                }

                // Custom Zoom Controls (Top Right of Map)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(4.dp)
                ) {
                    IconButton(onClick = { 
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) }
                    }) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.zoom_in_desc), tint = InkBlack)
                    }
                    IconButton(onClick = { 
                        scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) }
                    }) {
                        Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.zoom_out_desc), tint = InkBlack)
                    }
                }

                // My Location Button
                IconButton(
                    onClick = { moveToUserLocation() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color.White, RoundedCornerShape(20.dp))
                        .size(40.dp)
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = stringResource(R.string.my_location_desc), tint = InkBlack)
                }
            }
        }

        FloatingActionButton(
            onClick = onNewEntry,
            containerColor = InkBlack,
            contentColor = Parchment,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.new_entry_desc))
        }
    }
}
