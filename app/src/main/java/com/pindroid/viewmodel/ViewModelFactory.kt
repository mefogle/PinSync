package com.pindroid.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pindroid.data.NotesRepository

class ViewModelFactory(private val notesRepository: NotesRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java))
            return NotesViewModel(notesRepository) as T
        throw IllegalArgumentException("Unknown Class Name")
    }

}