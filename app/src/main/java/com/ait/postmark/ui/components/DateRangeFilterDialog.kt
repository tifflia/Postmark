package com.ait.postmark.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A dialog wrapping a Material [DateRangePicker] used to filter the entry list
 * by date. Either bound may be left empty: picking only a start date filters to
 * entries "from" that day onward.
 *
 * @param initialStartMillis pre-selected start bound as UTC epoch millis, or null
 * @param initialEndMillis pre-selected end bound as UTC epoch millis, or null
 * @param onConfirm called with the chosen bounds as ISO "yyyy-MM-dd" strings (null = open)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangeFilterDialog(
    initialStartMillis: Long?,
    initialEndMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (startIso: String?, endIso: String?) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = initialStartMillis,
        initialSelectedEndDateMillis = initialEndMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(
                        state.selectedStartDateMillis?.let { isoFromUtcMillis(it) },
                        state.selectedEndDateMillis?.let { isoFromUtcMillis(it) }
                    )
                },
                enabled = state.selectedStartDateMillis != null
            ) { Text("Apply") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DateRangePicker(
            state = state,
            modifier = Modifier.padding(top = 8.dp),
            title = {
                Text(
                    "Filter by date",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp)
                )
            }
        )
    }
}
