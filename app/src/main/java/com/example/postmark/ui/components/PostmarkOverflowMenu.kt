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
import androidx.compose.ui.res.stringResource
import com.example.postmark.R

/**
 * The dropdown shown when the hamburger icon is tapped on List and Map views.
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
            text = { Text(if (isOnListView) stringResource(R.string.map_view) else stringResource(R.string.list_view)) },
            onClick = { onDismiss(); onSwitchView() }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete_all), color = MaterialTheme.colorScheme.secondary) },
            onClick = { onDismiss(); confirmDelete = true }
        )
        if (onSignOut != null) {
            DropdownMenuItem(text = { Text(stringResource(R.string.sign_out)) }, onClick = { onDismiss(); onSignOut() })
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete_all_entries)) },
            text = { Text(stringResource(R.string.delete_all_entries_alert_text)) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDeleteAll() }) {
                    Text(stringResource(R.string.delete_all_btn), color = MaterialTheme.colorScheme.secondary)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel_btn)) } }
        )
    }
}
