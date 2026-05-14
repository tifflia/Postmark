package com.example.postmark.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.postmark.data.Entry
import com.example.postmark.data.EntryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single source of truth for the entries list.
 */
class EntriesViewModel(
    private val repo: EntryRepository = EntryRepository()
) : ViewModel() {

    val entries = repo.observeEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
    fun deleteAll() = viewModelScope.launch { repo.deleteAll() }
    fun add(entry: Entry) = viewModelScope.launch { repo.add(entry) }
}
