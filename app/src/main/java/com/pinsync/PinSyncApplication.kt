package com.pinsync

import android.app.Application
import android.content.Context
import androidx.room.Room
import com.pinsync.data.PinDatabase

class PinSyncApplication : Application() {

    companion object {
        lateinit var db: PinDatabase
        private var instance: PinSyncApplication? = null

        //fun applicationContext(): Context = instance!!.applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, PinDatabase::class.java, "pin-sync-database")
            .fallbackToDestructiveMigration() // Handle migrations
            .build()
        instance = this
    }
}