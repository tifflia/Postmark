package com.ait.postmark.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ait.postmark.data.Entry
import com.ait.postmark.data.EntryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for the entries list.
 */
class EntriesViewModel(
    private val repo: EntryRepository = EntryRepository()
) : ViewModel() {

    private val allEntries = repo.observeEntries()

    /** Inclusive lower bound as an ISO "yyyy-MM-dd" string, or null for unbounded. */
    private val _startDate = MutableStateFlow<String?>(null)
    val startDate: StateFlow<String?> = _startDate.asStateFlow()

    /** Inclusive upper bound as an ISO "yyyy-MM-dd" string, or null for unbounded. */
    private val _endDate = MutableStateFlow<String?>(null)
    val endDate: StateFlow<String?> = _endDate.asStateFlow()

    /** Entries visible in the UI, narrowed to the active date range (if any). */
    val entries = combine(allEntries, _startDate, _endDate) { list, start, end ->
        // ISO "yyyy-MM-dd" strings sort/compare lexicographically, so plain
        // string comparison is a valid date-range check here.
        list.filter { entry ->
            (start == null || entry.date >= start) &&
                (end == null || entry.date <= end)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** True whenever at least one bound of the date range is set. */
    val isFilterActive = combine(_startDate, _endDate) { start, end ->
        start != null || end != null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    /** Apply a date range. Either bound may be null to leave that side open. */
    fun setDateRange(start: String?, end: String?) {
        _startDate.value = start
        _endDate.value = end
    }

    /** Remove the date filter and show all entries again. */
    fun clearDateRange() {
        _startDate.value = null
        _endDate.value = null
    }

    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
    fun deleteAll() = viewModelScope.launch { repo.deleteAll() }
    fun add(entry: Entry) = viewModelScope.launch { repo.add(entry) }
}
