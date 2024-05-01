package com.pinsync

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Timer
import java.util.TimerTask

class ContentViewModel : ViewModel() {
    private val _data = MutableLiveData<PinAPI.Content>()
    val data : LiveData<PinAPI.Content> = _data
    private var timer: Timer? = null

    init {
        startPeriodicTask()
    }

    private fun startPeriodicTask() {
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                _data.postValue(PinAPI.notes())
            }
        }, 0, 5000) // Schedule the task to run every 5 seconds
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel() // Cancel the timer when the ViewModel is cleared
    }
}