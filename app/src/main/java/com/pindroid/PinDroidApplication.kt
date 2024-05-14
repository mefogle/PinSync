package com.pindroid

import android.app.Application
import androidx.room.Room
import com.pindroid.api.PinApi
import com.pindroid.data.NotesRepository
import com.pindroid.data.NotesRepositoryImpl
import com.pindroid.data.PinDatabase

class PinDroidApplication : Application() {

    companion object {
        lateinit var db: PinDatabase
        private var instance: PinDroidApplication? = null

        private lateinit var _notesRepository: NotesRepository

        fun notesRepository(): NotesRepository = _notesRepository

        //fun applicationContext(): Context = instance!!.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, PinDatabase::class.java, "pin-droid-database")
            .fallbackToDestructiveMigration() // Handle migrations
            .build()
        _notesRepository = NotesRepositoryImpl(PinApi.pinApiService)
        instance = this
    }
}