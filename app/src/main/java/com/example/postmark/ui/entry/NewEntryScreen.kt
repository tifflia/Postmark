package com.example.postmark.ui.entry

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Fullscreen
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.postmark.R
import com.example.postmark.ui.theme.InkBlack
import com.example.postmark.ui.theme.MutedStone
import com.example.postmark.ui.theme.PaperWhite
import com.example.postmark.ui.theme.Parchment
import com.example.postmark.ui.theme.StampRed
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun NewEntryScreen(
    onDone: () -> Unit,
    vm: NewEntryViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var locationExpanded by remember { mutableStateOf(true) }
    var photoExpanded by remember { mutableStateOf(state.selectedImageUri != null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(state.selectedImageUri) {
        if (state.selectedImageUri != null) photoExpanded = true
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> vm.onImageSelected(uri) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success -> if (success) vm.onImageSelected(tempImageUri) }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) vm.fetchCurrentLocation(context) }

    LaunchedEffect(Unit) {
        locationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    LaunchedEffect(state.saved) { if (state.saved) onDone() }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Parchment)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 48.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDone) {
                    Text(stringResource(R.string.cancel_btn), color = StampRed, fontWeight = FontWeight.Medium)
                }
                Text(
                    stringResource(R.string.new_entry),
                    style = MaterialTheme.typography.titleMedium,
                    color = InkBlack,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = { vm.save(context.contentResolver) },
                    enabled = !state.saving && state.body.isNotBlank()
                ) {
                    Text(stringResource(R.string.save_btn), color = StampRed, fontWeight = FontWeight.Medium)
                }
            }

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(Modifier.height(16.dp))

                // Date and Location Selector
                SectionLabel(
                    text = stringResource(R.string.location_label),
                    icon = Icons.Outlined.LocationOn,
                    isExpanded = locationExpanded,
                    onToggle = { locationExpanded = !locationExpanded }
                )
                
                if (locationExpanded) {
                    Column {
                        Spacer(Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            ModeButton(
                                label = stringResource(R.string.detect_btn),
                                icon = Icons.Outlined.MyLocation,
                                selected = state.locationMode == LocationMode.DETECT,
                                modifier = Modifier.weight(1f)
                            ) {
                                vm.setLocationMode(LocationMode.DETECT); vm.fetchCurrentLocation(
                                context
                            )
                            }

                            Spacer(Modifier.width(8.dp))

                            ModeButton(
                                label = stringResource(R.string.custom_btn),
                                icon = Icons.Outlined.Edit,
                                selected = state.locationMode == LocationMode.CUSTOM,
                                modifier = Modifier.weight(1f)
                            ) { vm.setLocationMode(LocationMode.CUSTOM) }

                            Spacer(Modifier.width(8.dp))

                            ModeButton(
                                label = stringResource(R.string.map_btn),
                                icon = Icons.Outlined.Map,
                                selected = state.locationMode == LocationMode.MAP,
                                modifier = Modifier.weight(1f)
                            ) { vm.setLocationMode(LocationMode.MAP) }
                        }

                        Spacer(Modifier.height(16.dp))

                        // Location Input / Display
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MutedStone.copy(alpha = 0.05f))
                                .border(
                                    1.dp,
                                    MutedStone.copy(alpha = 0.2f),
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(16.dp)
                        ) {
                            if (state.locationMode == LocationMode.CUSTOM) {
                                BasicTextField(
                                    value = state.location,
                                    onValueChange = vm::onLocationChange,
                                    textStyle = TextStyle(fontSize = 16.sp, color = InkBlack),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (state.location.isEmpty()) {
                                            Text(
                                                stringResource(R.string.enter_location),
                                                color = MutedStone,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = state.location.ifBlank { stringResource(R.string.detecting) },
                                        color = if (state.location.isBlank()) MutedStone else InkBlack,
                                        fontSize = 16.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (state.locationMode == LocationMode.MAP) {
                                        IconButton(
                                            onClick = { vm.setMapExpanded(true) },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Outlined.Fullscreen, null, tint = MutedStone)
                                        }
                                    }
                                }
                            }
                        }

                        if (state.locationMode == LocationMode.CUSTOM && state.history.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                stringResource(R.string.history_label),
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedStone
                            )
                            LazyColumn(modifier = Modifier.heightIn(max = 150.dp)) {
                                items(state.history) { historyLoc ->
                                    HistoryItem(historyLoc) { vm.onLocationChange(historyLoc) }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Photo Picker
                SectionLabel(
                    text = stringResource(R.string.photo_label),
                    icon = Icons.Outlined.Image,
                    isExpanded = photoExpanded,
                    onToggle = { photoExpanded = !photoExpanded }
                )
                
                if (photoExpanded) {
                    Column {
                        Spacer(Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .background(PaperWhite, RoundedCornerShape(8.dp))
                                .clickable {
                                    photoPickerLauncher.launch("image/*")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (state.selectedImageUri != null) {
                                AsyncImage(
                                    model = state.selectedImageUri,
                                    contentDescription = stringResource(R.string.selected_photo_desc),
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                IconButton(
                                    onClick = { vm.onImageSelected(null) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(0.4f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Outlined.Close,
                                        null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Outlined.Image,
                                        null,
                                        tint = MutedStone,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    Row {
                                        TextButton(onClick = { photoPickerLauncher.launch("image/*") }) {
                                            Icon(Icons.Outlined.Collections, null, Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.album_btn), color = Color(0xFF4A86E8))
                                        }
                                        Spacer(Modifier.width(16.dp))
                                        TextButton(onClick = {
                                            val file = File(
                                                context.cacheDir,
                                                "temp_image_${System.currentTimeMillis()}.jpg"
                                            )
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.provider",
                                                file
                                            )
                                            tempImageUri = uri
                                            cameraLauncher.launch(uri)
                                        }) {
                                            Icon(
                                                Icons.Outlined.PhotoCamera,
                                                null,
                                                Modifier.size(18.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(stringResource(R.string.camera_btn), color = Color(0xFF4A86E8))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Body Input
                SectionLabel(stringResource(R.string.entry_label))
                Spacer(Modifier.height(8.dp))
                BasicTextField(
                    value = state.body,
                    onValueChange = vm::onBodyChange,
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = InkBlack,
                        lineHeight = 26.sp,
                        fontStyle = FontStyle.Italic
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    decorationBox = { innerTextField ->
                        if (state.body.isEmpty()) {
                            Text(
                                stringResource(R.string.write_something),
                                style = TextStyle(
                                    fontSize = 18.sp,
                                    color = MutedStone,
                                    fontStyle = FontStyle.Italic
                                )
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        // Full-screen Map Overlay
        if (state.isMapExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(
                        LatLng(state.geo?.latitude ?: 0.0, state.geo?.longitude ?: 0.0),
                        15f
                    )
                }
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { vm.onMapClick(it, context) },
                    uiSettings = MapUiSettings(zoomControlsEnabled = false)
                ) {
                    state.geo?.let {
                        Marker(state = MarkerState(LatLng(it.latitude, it.longitude)))
                    }
                }
                
                // Custom Zoom Controls (Top Right)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 110.dp, end = 16.dp)
                        .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                        .padding(4.dp)
                ) {
                    IconButton(onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } }) {
                        Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.zoom_in_desc), tint = InkBlack)
                    }
                    IconButton(onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } }) {
                        Icon(Icons.Outlined.Remove, contentDescription = stringResource(R.string.zoom_out_desc), tint = InkBlack)
                    }
                    Spacer(Modifier.height(8.dp))
                    IconButton(onClick = { 
                        // Reuse VM logic but also animate camera
                        vm.fetchCurrentLocation(context)
                        state.geo?.let {
                            scope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15f))
                            }
                        }
                    }) {
                        Icon(Icons.Outlined.MyLocation, contentDescription = stringResource(R.string.my_location_desc), tint = InkBlack)
                    }
                }
                
                // Overlay controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { vm.setMapExpanded(false) },
                        modifier = Modifier.background(Color.White, RoundedCornerShape(20.dp))
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.close_desc), tint = InkBlack)
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = state.location.ifBlank { stringResource(R.string.select_location) },
                            style = MaterialTheme.typography.bodyMedium,
                            color = InkBlack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SectionLabel(
    text: String,
    icon: ImageVector? = null,
    isExpanded: Boolean? = null,
    onToggle: (() -> Unit)? = null
) {
    Row(
        modifier = if (onToggle != null) {
            Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() }
                .fillMaxWidth()
        } else Modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, Modifier.size(14.dp), tint = MutedStone)
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MutedStone,
            letterSpacing = 1.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        if (isExpanded != null) {
            Icon(
                if (isExpanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = MutedStone,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ModeButton(label: String, icon: ImageVector, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val contentColor = if (selected) InkBlack else MutedStone
    val borderColor = if (selected) InkBlack else MutedStone.copy(alpha = 0.3f)
    
    Column(
        modifier = modifier
            .height(72.dp)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .background(if (selected) MutedStone.copy(alpha = 0.05f) else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = TextStyle(fontSize = 13.sp, color = contentColor, fontWeight = FontWeight.Medium))
    }
}

@Composable
fun HistoryItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.History, null, Modifier.size(16.dp), tint = MutedStone)
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = InkBlack)
    }
}
