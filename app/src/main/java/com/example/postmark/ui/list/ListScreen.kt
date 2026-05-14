package com.example.postmark.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Edit
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.postmark.data.Entry
import com.example.postmark.ui.components.PostmarkOverflowMenu
import com.example.postmark.ui.components.formatIsoDate
import com.example.postmark.ui.theme.InkBlack
import com.example.postmark.ui.theme.MutedStone
import com.example.postmark.ui.theme.Parchment
import com.example.postmark.ui.theme.PaperWhite

@Composable
fun ListScreen(
    onOpenEntry: (String) -> Unit,
    onNewEntry: () -> Unit,
    onSwitchToMap: () -> Unit,
    onSignOut: () -> Unit,
    vm: EntriesViewModel = viewModel()
) {
    val entries by vm.entries.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(Parchment)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp, top = 48.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("WELCOME BACK", style = MaterialTheme.typography.labelMedium, color = MutedStone)
                    Spacer(Modifier.height(4.dp))
                    Text("John", style = MaterialTheme.typography.headlineLarge, color = InkBlack)
                }
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Outlined.Menu, contentDescription = "Menu", tint = InkBlack)
                    }
                    PostmarkOverflowMenu(
                        expanded = menuOpen,
                        onDismiss = { menuOpen = false },
                        isOnListView = true,
                        onSwitchView = onSwitchToMap,
                        onDeleteAll = { vm.deleteAll() },
                        onSignOut = onSignOut
                    )
                }
            }

            // Section header
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("LATEST ENTRIES", style = MaterialTheme.typography.labelMedium, color = MutedStone)
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MutedStone))
            }

            Spacer(Modifier.height(12.dp))

            if (entries.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 80.dp), contentAlignment = Alignment.TopCenter) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "No entries yet.",
                            style = MaterialTheme.typography.bodyLarge.copy(fontStyle = FontStyle.Italic),
                            color = MutedStone
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Tap the pencil to write your first.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = MutedStone
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(entries, key = { it.id }) { entry ->
                        EntryCard(entry = entry, onClick = { onOpenEntry(entry.id) })
                    }
                    items(listOf(Unit)) { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // Floating "new entry" pencil
        FloatingActionButton(
            onClick = onNewEntry,
            containerColor = InkBlack,
            contentColor = Parchment,
            shape = CircleShape,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp).size(56.dp)
        ) {
            Icon(Icons.Outlined.Edit, contentDescription = "New entry")
        }
    }
}

@Composable
private fun EntryCard(entry: Entry, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp))
            .background(PaperWhite, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatIsoDate(entry.date), style = MaterialTheme.typography.titleLarge, color = InkBlack)
                if (entry.location.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = MutedStone, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.size(4.dp))
                        Text(entry.location, style = MaterialTheme.typography.bodyMedium, color = MutedStone, fontSize = 13.sp)
                    }
                }
            }
            
            if (entry.photoUrl != null) {
                Spacer(Modifier.size(12.dp))
                AsyncImage(
                    model = entry.photoUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Parchment),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("→", color = MutedStone, fontSize = 14.sp)
            }
        }
    }
}
