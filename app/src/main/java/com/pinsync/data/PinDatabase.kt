package com.pinsync.data

import androidx.paging.PagingSource
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.pinsync.api.PinApi
import java.util.UUID

@Database(entities = [Object::class, PinApi.NoteData::class], version = 1)
abstract class PinDatabase: RoomDatabase() {

    interface ObjectDao {
        @Query("SELECT * FROM object")
        fun getAll(): List<PinApi.Object>

        @Query("SELECT * FROM object WHERE uuid IN (:objectIds)")
        fun loadAllByIds(objectIds: IntArray): List<PinApi.Object>

        @Insert
        fun insert(vararg objectFields : PinApi.Object)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertAll(objects: List<PinApi.Object>)

        @Query("SELECT * FROM object INNER JOIN note ON object.uuid = note.uuid WHERE object.uuid = :query")
        fun noteObjectPagingSource (query: String): PagingSource<UUID, PinApi.Object>

        @Query("SELECT * FROM object WHERE uuid LIKE :query")
        fun pagingSource(query: String): PagingSource<UUID, PinApi.Object>

        @Delete
        fun delete(objectRef: PinApi.Object)
    }

    interface NoteDao {
        @Query("SELECT * FROM note")
        fun getAll(): List<PinApi.NoteData>

        @Query("SELECT * FROM note WHERE uuid IN (:noteIds)")
        fun loadAllByIds(noteIds: IntArray): List<PinApi.NoteData>

        @Insert
        fun insert(vararg note : PinApi.NoteData)

        @Delete
        fun delete(note: PinApi.NoteData)
    }
}