package com.pinsync.data

import android.util.Log
import com.pinsync.api.PinApi
import java.util.Date
import java.util.UUID

@Suppress("UNNECESSARY_SAFE_CALL") // Needed for the null dto and data checks below.
fun mapObjectDtoToEntity(dto: PinApi.Object): ContentObject {
    val contentEntity: ContentDataEntity =
        when (dto?.data?.contentType) {
            ContentType.GENERIC_NOTE -> {
                mapNoteDataToEntity(dto.data as PinApi.NoteData, dto.uuid)
            }

            null -> {
                Log.d("mapObjectDtoToEntity", "Encountered null object - returning empty Note")
                NoteData(
                    UUID(0, 0),
                    dto.uuid,
                    "",
                    "",
                    "",
                    Date(),
                    Date(),
                    "",
                    Note(UUID(0, 0), "", "")
                )
            }
        }
    return ContentObject(
        dto.data.contentType,
        dto.uuid,
        dto.userLastModified,
        dto.userCreatedAt,
        dto.originClientId,
        dto.favorite,
        contentEntity
    )
}

fun mapNoteDataToEntity(dto: PinApi.NoteData, parentId: UUID): NoteData {
    return NoteData(
        dto.uuid,
        parentId,
        dto.location,
        dto.latitude,
        dto.longitude,
        dto.createdAt,
        dto.lastModifiedAt,
        dto.state,
        dto.note
    )
}


