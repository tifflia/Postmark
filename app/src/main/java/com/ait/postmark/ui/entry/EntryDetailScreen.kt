package com.ait.postmark.ui.entry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.ait.postmark.R
import com.ait.postmark.ui.components.formatIsoDate
import com.ait.postmark.ui.list.EntriesViewModel
import com.ait.postmark.ui.theme.InkBlack
import com.ait.postmark.ui.theme.MutedStone
import com.ait.postmark.ui.theme.Parchment
import com.ait.postmark.ui.theme.PaperWhite

@Composable
fun EntryDetailScreen(
    entryId: String,
    onBack: () -> Unit,
    vm: EntriesViewModel = viewModel()
) {
    val entries by vm.entries.collectAsState()
    val entry = entries.firstOrNull { it.id == entryId }
    var confirmDelete by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Parchment)) {
        Column(modifier = Modifier.fillMaxSize()) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .padding(top = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(
                        R.string.back_desc
                    ), tint = InkBlack)
                }
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.delete_desc), tint = InkBlack)
                }
            }

            if (entry == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.entry_not_found),
                        style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                        color = MutedStone
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(formatIsoDate(entry.date), style = MaterialTheme.typography.headlineLarge, color = InkBlack)
                    if (entry.location.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MutedStone, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text(
                                entry.location,
                                style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                                color = MutedStone
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(entry.body, style = MaterialTheme.typography.bodyLarge, color = InkBlack)

                    if (entry.photoUrl != null) {
                        Spacer(Modifier.height(32.dp))
                        AsyncImage(
                            model = entry.photoUrl,
                            contentDescription = stringResource(R.string.entry_photo_desc),
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(4f / 3f)
                                .shadow(4.dp, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .background(PaperWhite),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    if (confirmDelete && entry != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_this_entry)) },
            text = { Text(stringResource(R.string.delete_this_entry_alert_text)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.delete(entry.id)
                    onBack()
                }) {
                    Text(stringResource(R.string.delete_btn), color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel_btn)) } }
        )
    }
}
