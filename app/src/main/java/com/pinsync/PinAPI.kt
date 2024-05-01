package com.pinsync

import android.util.Log
import android.webkit.CookieManager
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.Date

class APIError(message: String) : Exception(message)

object PinAPI {

    @JsonClass(generateAdapter = true)
    data class UserInfo (val id : String, val name : String, val email : String, val givenName : String, val familyName : String, val idToken : String)

    @JsonClass(generateAdapter = true)
    data class SessionInfo (val user : UserInfo, val accessToken : String)

    @JsonClass(generateAdapter = true)
    data class Note (val uuid : String, var title : String, var text : String)

    @JsonClass(generateAdapter = true)
    data class Data (val uuid : String, val type : String, val location : String?, val latitude : String?, val longitude : String?, val createdAt : Date, val lastModifiedAt : Date, val state : String, val note : Note)

    @JsonClass(generateAdapter = true)
    data class Object (val uuid : String, val data : Data, val userLastModified : Date, val userCreatedAt : Date, val originClientId : String, val favorite : Boolean )

    @JsonClass(generateAdapter = true)
    data class Sort (var empty : Boolean, var sorted : Boolean, var unsorted : Boolean)

    @JsonClass(generateAdapter = true)
    data class Pageable (var sort : Sort, var offset : Integer, var pageNumber : Integer, var pageSize : Integer, var paged : Boolean, var unpaged : Boolean)

    @JsonClass(generateAdapter = true)
    data class Content (var content : List<Object>, val pageable : Pageable, val last : Boolean, val totalElements : Integer, val totalPages: Integer, val size : Integer, val number : Integer, val sort : Sort, val first : Boolean, val numberOfElements : Integer, val empty : Boolean)

    private const val ROOTURL = "https://webapi.prod.humane.cloud/"
    private const val INITIALURL = "https://humane.center/"
    private const val CAPTUREURL = ROOTURL + "capture/"
    private const val AUTHCOOKIE = "__Secure-next-auth.session-token.0"

    private val okHttpClient = OkHttpClient.Builder()
        .cookieJar(WebViewCookieHandler())
        .addInterceptor(AuthInterceptor())
        .build()

    private val sessionOkHttpClient = OkHttpClient.Builder()
        .cookieJar(WebViewCookieHandler())
        .build()

    private val sessionRequest = Request.Builder()
        .url(INITIALURL + "api/auth/session")
        .build()

    private val sessionInfoAdapter = Moshi.Builder().build().adapter(SessionInfo::class.java)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).add(Date::class.java, Rfc3339DateJsonAdapter()).build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CAPTUREURL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    interface HumaneApiService {
        @GET("notes")
        fun getNotes(): Call<Content>
    }

    private val humaneApiService: HumaneApiService = retrofit.create(HumaneApiService::class.java)

    private var accessToken : String? = null

    class WebViewCookieHandler : CookieJar {
        private val webkitCookieManager = CookieManager.getInstance()

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            // Save cookies to the WebView CookieManager
            val urlString = url.toString()
            cookies.forEach { cookie ->
                webkitCookieManager.setCookie(urlString, cookie.toString())
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            // Load cookies from the WebView CookieManager
            val urlString = url.toString()
            val cookiesString = webkitCookieManager.getCookie(urlString)
            return cookiesString?.split("; ")?.mapNotNull { Cookie.parse(url, it) } ?: emptyList()
        }
    }

    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            // If the user hasn't been authorized, go ahead and refresh the session
            accessToken ?: refreshSession()

            // If, after that, the user still hasn't been authorized, clear the cookies and
            // throw an exception.
            accessToken ?: {
                clearCookies()
                throw APIError ("The user is not authorized")
            }

            val request = chain.request()

            // Add the necessary headers
            val newRequest = request.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .addHeader("Accept", "application/json")
                .build()

            var response = chain.proceed(newRequest)

            if (response.code == 401 || response.code == 403) {
                // Access token has expired or user does not have necessary permissions, so first
                // attempt to refresh the token.
                refreshSession()
                val newerRequest = newRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken").build()
                response.close()
                response = chain.proceed(newerRequest)

                // If it's still failing, we need to take more drastic measures - delete all the cookies
                // because it's time for the user to login again.  We really want to do something here to
                // retrigger the AuthDialog, but what?

                if (response.code == 401 || response.code == 403) {
                    clearCookies()
                    throw APIError ("The user is not authorized")
                }
            }
            return response
        }
    }

    private fun clearCookies () {
        // This will trigger a reauthentication the next time we launch
        val webkitCookieManager = CookieManager.getInstance()
        webkitCookieManager.removeAllCookies(null)
    }

    fun isAuthenticated() : Boolean {
        val cookie = CookieManager.getInstance().getCookie(INITIALURL)
        if ((cookie != null) && (cookie.contains(AUTHCOOKIE))) {
            Log.d("PinAPI", "Already authenticated")
            return true
        }
        return false
    }

    fun notes () : Content? {
        val response = humaneApiService.getNotes().execute()
        if (!response.isSuccessful) throw IOException("Unexpected code $response")
        return response.body()
    }

    private fun refreshSession() : Boolean {
        sessionOkHttpClient.newCall(sessionRequest).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val responseBody = response.body?.string() ?: throw NullPointerException("Response body is null")

            // Parse the string into a JSON object
            val sessionInfo = sessionInfoAdapter.fromJson(responseBody)
            return sessionInfo?.let {
                accessToken = it.accessToken
                Log.d("PinAPI", "accessToken set")
                true
            } ?: false
        }
    }
}

