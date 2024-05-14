package com.pinsync

import android.app.Application
import androidx.room.Room
import com.pinsync.api.PinApi
import com.pinsync.data.NotesRepository
import com.pinsync.data.NotesRepositoryImpl
import com.pinsync.data.PinDatabase

class PinSyncApplication : Application() {

    companion object {
        lateinit var db: PinDatabase
        private var instance: PinSyncApplication? = null

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