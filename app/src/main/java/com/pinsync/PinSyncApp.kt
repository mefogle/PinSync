package com.pinsync

import android.app.Application
import androidx.room.Room
import com.pinsync.data.PinDatabase

class PinSyncApp : Application() {

    companion object {
        var db: PinDatabase? = null
    }

    override fun onCreate() {
        super.onCreate()
        db = Room.databaseBuilder(applicationContext, PinDatabase::class.java, "pin-sync-database")
            .fallbackToDestructiveMigration() // Handle migrations
            .build()
    }
}