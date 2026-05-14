package com.example.postmark.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

/**
 * The dropdown shown when the hamburger icon is tapped on List and Map views.
 * Mirrors the blueprint: switch view, Filter, Delete All.
 */
@Composable
fun PostmarkOverflowMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    isOnListView: Boolean,
    onSwitchView: () -> Unit,
    onDeleteAll: () -> Unit,
    onSignOut: (() -> Unit)? = null
) {
    var confirmDelete by remember { mutableStateOf(false) }

    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            text = { Text(if (isOnListView) "Map View" else "List View") },
            onClick = { onDismiss(); onSwitchView() }
        )
        DropdownMenuItem(
            text = { Text("Delete All", color = MaterialTheme.colorScheme.secondary) },
            onClick = { onDismiss(); confirmDelete = true }
        )
        if (onSignOut != null) {
            DropdownMenuItem(text = { Text("Sign out") }, onClick = { onDismiss(); onSignOut() })
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete all entries?") },
            text = { Text("This cannot be undone. Every entry will be permanently removed.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteAll() }) {
                    Text("Delete all", color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancel") } }
        )
    }
}
