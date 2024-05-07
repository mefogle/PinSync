package com.pinsync.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.UUID

interface PinApiService {

    @GET("notes")
    suspend fun getNotes(@Query("page") page : Int = 0, @Query("size") size : Int = 20): PinApi.Content

    @POST("memory/{uuid}/favorite")
    suspend fun favorite(@Path("uuid") uuid: UUID) : Response<Unit>

    @POST("memory/{uuid}/unfavorite")
    suspend fun unfavorite(@Path("uuid") uuid: UUID) : Response<Unit>

}