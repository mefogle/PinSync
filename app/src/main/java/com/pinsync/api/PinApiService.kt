package com.pinsync.api

import retrofit2.http.GET

interface PinApiService {

    @GET("notes")
    suspend fun getNotes(): PinApi.Content

}