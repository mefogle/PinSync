package com.pinsync.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pinsync.api.PinApi
import com.pinsync.data.NotesRepository
import com.pinsync.util.Resource
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class NotesViewModel (private val notesRepository: NotesRepository) : ViewModel() {

    private val notes = MutableLiveData<Resource<PinApi.Content>>()
    private var timer: Timer? = null

    init {
        fetchNotes()
        startPeriodicTask()
    }

    private fun fetchNotes() {
        viewModelScope.launch {
            notes.postValue(Resource.loading(null))
            notesRepository.getAllNotes()
                .catch { e ->
                    notes.postValue(Resource.error(e.toString(), null))
                }
                .collect {
                    notes.postValue(Resource.success(it))
                }
        }
    }

    fun getNotes(): LiveData<Resource<PinApi.Content>> {
        return notes
    }

    private fun startPeriodicTask() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                viewModelScope.launch {
                    notesRepository.getAllNotes()
                        .catch { e ->
                            notes.postValue(Resource.error(e.toString(), null))
                        }
                        .collect {
                            notes.postValue(Resource.success(it))
                        }
                }
            }
        }, 0, 5000) // Schedule the task to run every 5 seconds
    }
    override fun onCleared() {
        super.onCleared()
        timer?.cancel() // Cancel the timer when the ViewModel is cleared
    }
}