package com.pinsync.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.pinsync.api.PinApi
import com.pinsync.api.PinApiService

class NotesPagingSource(
    private val pinApiService: PinApiService
) : PagingSource<Int, PinApi.Object>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PinApi.Object> {
        val pageNumber = params.key ?: 0
        return try {
            val response = pinApiService.getNotes(page = pageNumber)
            LoadResult.Page(
                data = response.content,
                prevKey = if (pageNumber == 0) null else pageNumber,
                nextKey = if (response.last) null else pageNumber + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }


    override fun getRefreshKey(state: PagingState<Int, PinApi.Object>): Int {
        return ((state.anchorPosition ?: 0) - state.config.initialLoadSize / 2).coerceAtLeast(0)
    }
}