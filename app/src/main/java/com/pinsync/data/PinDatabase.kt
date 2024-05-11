package com.pinsync.data

import androidx.lifecycle.LiveData
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date
import java.util.UUID

enum class ContentType {
    GENERIC_NOTE
}
@Entity(tableName = "object")
data class Object(
    val contentType: ContentType,
    @PrimaryKey val uuid: UUID,
    val userLastModified: Date,
    val userCreatedAt: Date,
    val originClientId: String,
    val favorite: Boolean,
    @Ignore val contentData: ContentData? = null
) {
    constructor(contentType: ContentType, uuid: UUID, userLastModified: Date, userCreatedAt: Date, originClientId: String, favorite: Boolean):
            this(contentType, uuid, userLastModified, userCreatedAt, originClientId, favorite, null)
}

// Base class for Note, Photo & Video... There should be another abstract class above this
// for the other data types to extend from. This class is shared by the database objects as well as
// the DTOs.
abstract class ContentData(
    open val uuid: UUID,
    open val location: String?,
    open val latitude: String?,
    open val longitude: String?,
    open val createdAt: Date,
    open val lastModifiedAt: Date,
    open val state: String,
    open var contentType: ContentType
)

abstract class ContentDataEntity (
    override val uuid: UUID,
    override val location: String?,
    override val latitude: String?,
    override val longitude: String?,
    override val createdAt: Date,
    override val lastModifiedAt: Date,
    override val state: String,
    @ColumnInfo(name = "parent_id") open val parentId: UUID?,
    override var contentType: ContentType
) : ContentData (uuid, location, latitude, longitude, createdAt, lastModifiedAt, state, contentType)

// The NoteData and Notes objects, used for DB storage
@Entity(tableName = "note",
    foreignKeys = [ForeignKey(entity = Object::class,
        parentColumns = arrayOf("uuid"),
        childColumns = arrayOf("parent_id"),
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["parent_id"])])
data class NoteData(
    @ColumnInfo(name = "note_uuid")
    @PrimaryKey override val uuid: UUID,
    @ColumnInfo(name = "parent_id") override val parentId: UUID,
    override val location: String?,
    override val latitude: String?,
    override val longitude: String?,
    override val createdAt: Date,
    override val lastModifiedAt: Date,
    override val state: String,
    @Embedded val note: Note
) :
    ContentDataEntity (uuid, location, latitude, longitude, createdAt, lastModifiedAt, state, parentId, ContentType.GENERIC_NOTE )



// This class works for both the REST API and the DB.
data class Note(
    val uuid: UUID,
    var title: String,
    var text: String)

// Relations for the various content types
abstract class ObjectRelationship (
    open val container : Object
)

// Type converters
class UUIDConverter {
    @TypeConverter
    fun fromUUID(uuid: UUID?): String? {
        return uuid?.toString()
    }

    @TypeConverter
    fun toUUID(uuid: String?): UUID? {
        return uuid?.let { UUID.fromString(it) }
    }
}

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}

data class ObjectWithNote(
    @Embedded override val container: Object,
    @Relation(
        parentColumn = "uuid",
        entityColumn = "parent_id")
    val note: NoteData
) : ObjectRelationship (container)

@Database(entities = [Object::class, NoteData::class], version = 1, exportSchema = false)
@TypeConverters(UUIDConverter::class, DateConverter::class)
abstract class PinDatabase: RoomDatabase() {

    abstract fun objectDao (): ObjectDao

    @Dao
    interface ObjectDao {

        @Query("SELECT * FROM object")
        fun getAll(): List<Object>

        @Query("SELECT * FROM object WHERE uuid IN (:objectIds)")
        fun loadAllByIds(objectIds: IntArray): List<Object>

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertAll(objects: List<Object>)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        fun insertNotes(notes: List<NoteData>)

        @Delete
        fun delete(objectRef: Object)

        @Transaction
        @Query("SELECT * FROM Object ORDER BY favorite DESC, userCreatedAt DESC")
        fun getObjectsWithNotes (): LiveData<List<ObjectWithNote>>

        @Transaction
        @Query("DELETE FROM object WHERE uuid NOT IN (:idsToKeep)")
        suspend fun removeDeletedObjects(idsToKeep: List<UUID>)

        @Transaction
        @Query("DELETE FROM object")
        suspend fun removeAll()

        @Transaction
        fun insertAllWithNotes(objects: List<Object>) {
            insertAll(objects)
            val noteObjects = objects.filter { it.contentType == ContentType.GENERIC_NOTE }
            insertNotes(noteObjects.map { noteObject -> noteObject.contentData as NoteData })
        }

        @Transaction
        fun insertWithNote(objectFields : Object) {
            if (objectFields.contentType == ContentType.GENERIC_NOTE) {
                insertAll(listOf(objectFields))
                insertNotes(listOf(objectFields.contentData as NoteData))
            }
        }
    }
}