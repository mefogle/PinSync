package com.pinsync.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.pinsync.data.NotesRepository

class ViewModelFactory(private val notesRepository: NotesRepository) : ViewModelProvider.Factory {
    @SuppressWarnings("unchecked")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotesViewModel::class.java))
            return NotesViewModel(notesRepository) as T
        throw IllegalArgumentException("Unknown Class Name")
    }

}