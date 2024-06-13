package com.pinsync.api

import android.util.Log
import android.webkit.CookieManager
import com.pinsync.data.ContentData
import com.pinsync.data.ContentType
import com.pinsync.data.Note
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonClass
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.Dns
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.Inet4Address
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.Date
import java.util.UUID

class APIError(message: String) : Exception(message)

object PinApi {

//    val cacheSize = 10L * 1024L * 1024L // 10 MiB
//    val cache = Cache(File(PinSyncApplication.applicationContext().cacheDir, "http-cache"), cacheSize)

    /// The UserInfo object returned as part of the session info.
    @JsonClass(generateAdapter = true)
    data class UserInfo(
        val id: String,
        val name: String,
        val email: String,
        val givenName: String,
        val familyName: String,
        val idToken: String
    )

    // The SessionInfo object returned as part of a successful session request.  The key attribute is the
    // accessToken.
    @JsonClass(generateAdapter = true)
    data class SessionInfo(val user: UserInfo, val accessToken: String)

    // The NoteData objects, used for all notes stored in .center
    @JsonClass(generateAdapter = true)
    data class NoteData(
        override val uuid: UUID,
        override val location: String?,
        override val latitude: String?,
        override val longitude: String?,
        override val createdAt: Date,
        override val lastModifiedAt: Date,
        override val state: String,
        val note: Note
    ) :
        ContentData(
            uuid,
            location,
            latitude,
            longitude,
            createdAt,
            lastModifiedAt,
            state,
            ContentType.GENERIC_NOTE
        )

    // We need a data class with just the title and text for the create & update DTOs
    data class NoteCreateDTO(
        val title: String,
        val text: String
    )

    // The Object object is used for wrapping other content types.
    @JsonClass(generateAdapter = true)
    data class Object(
        val uuid: UUID,
        val data: ContentData,
        val userLastModified: Date,
        val userCreatedAt: Date,
        val originClientId: String,
        val favorite: Boolean
    )


    // Indicates the sorted/unsorted nature of the contents of a list
    @JsonClass(generateAdapter = true)
    data class Sort(var empty: Boolean, var sorted: Boolean, var unsorted: Boolean)

    // Indicates whether a list is pageable and, if so, where we are in that list.
    @JsonClass(generateAdapter = true)
    data class Pageable(
        var sort: Sort,
        var offset: Int,
        var pageNumber: Int,
        var pageSize: Int,
        var paged: Boolean,
        var unpaged: Boolean
    )

    // The outermost container, containing a list of objects.
    @JsonClass(generateAdapter = true)
    data class Content(
        val content: List<Object>,
        val pageable: Pageable,
        val last: Boolean,
        val totalElements: Int,
        val totalPages: Int,
        val size: Int,
        val number: Int,
        val sort: Sort,
        val first: Boolean,
        val numberOfElements: Int,
        val empty: Boolean
    )

    // Various URLs that we operate against.
    private const val ROOTURL = "https://webapi.prod.humane.cloud/"
    private const val INITIALURL = "https://humane.center/"
    private const val CAPTUREURL = ROOTURL + "capture/"

    // If this cookie is present in the cookie store, it means that we should be authenticated
    // (unless the session has expired, in which case we'll get a 403 and make another session
    // request.
    private const val AUTHCOOKIE = "__Secure-authjs.session-token.0"

    // Just leave this here in case we need it for debugging.

//    private class DummyInterceptor : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val request = chain.request()
//
//            var response = chain.proceed(request)
//
//            return response;
//
//        }
//    }

//    private val loggingInterceptor = HttpLoggingInterceptor().apply {
//        level = HttpLoggingInterceptor.Level.BODY
//    }

//    Initial attempts at creating an offline mode of sorts... seems we're going to have have to
//    implement this at a higher level in order for it to work.  As it currently stands, any attempt to
//    hit the cache was resulting in a 504 error.

//    private val offlineInterceptor = Interceptor { chain ->
//        var request = chain.request()
//        if (!isOnline()) {
//            // Forcing cache:
//            request = request.newBuilder()
//                .header("Cache-Control", "public, only-if-cached, max-stale=" + 2419200)
//                .build()
//            // Alternatively, return a mock response.  We could attempt to come up with a variety of
//            // "special" mock responses here that would instruct the code parsing the responses to
//            // ignore them.
//        }
//        chain.proceed(request)
//    }

    // Cache some responses so we won't necessarily crash if we're offline
//    class CacheInterceptor : Interceptor {
//        override fun intercept(chain: Interceptor.Chain): Response {
//            val response = chain.proceed(chain.request())
//            val cacheControl = CacheControl.Builder()
//                .maxAge(10, TimeUnit.DAYS)
//                .build()
//            return response.newBuilder()
//                .header("Cache-Control", cacheControl.toString())
//                .build()
//        }
//    }
    // This client is used to interact with the content server.  Note that addition of the
    // AuthInterceptor which kicks in whenever there is a 401 or 403 error.
    private val okHttpClient = OkHttpClient.Builder()
//        .addInterceptor(DummyInterceptor())
//        .addInterceptor(loggingInterceptor)
//        .cache(cache)
//        .addInterceptor(offlineInterceptor)
        .dns(Ipv4OnlyDns())
        .addInterceptor(AuthInterceptor())
//        .addNetworkInterceptor(CacheInterceptor()) // Adds Cache-Control header for responses
        .cookieJar(WebViewCookieHandler())
        .build()

    // This client is used only for interacting with the session server.
    private val sessionOkHttpClient = OkHttpClient.Builder()
//        .addInterceptor(loggingInterceptor)
//        .cache(cache)
//        .addInterceptor(offlineInterceptor)
//        .addNetworkInterceptor(CacheInterceptor()) // Adds Cache-Control header for responses
        .dns(Ipv4OnlyDns())
        .cookieJar(WebViewCookieHandler())
        .build()

    // The request that triggers creation of a new session.
    private val sessionRequest = Request.Builder()
        .url(INITIALURL + "api/auth/session")
        .build()

    // The adapter that only processes SessionInfo JSON.
    private val sessionInfoAdapter = Moshi.Builder().build().adapter(SessionInfo::class.java)

    // There's occasionally some malformed JSON coming from the server that we need to deal with,
    // most often with null values. So we'll create a custom adapter to handle this.
    class NullToEmptyStringAdapter {

        @FromJson
        fun fromJson(reader: JsonReader): String {
            if (reader.peek() == JsonReader.Token.NULL) {
                reader.nextNull<Unit>() // Consume the null.
                return ""
            }
            return reader.nextString()
        }

        @ToJson
        fun toJson(writer: JsonWriter, value: String?) {
            writer.value(value)
        }
    }

    // Java UUIDs can't be parsed natively so we need this special adapter.
    private class UUIDAdapter : JsonAdapter<UUID>() {
        @FromJson
        override fun fromJson(reader: JsonReader): UUID? {
            return try {
                UUID.fromString(reader.nextString())
            } catch (e: JsonDataException) {
                // For some reason, after deleting a note from humane.center, other notes started
                // coming through with null UUIDs.  This is an attempt to handle that.
                UUID.randomUUID()
            }
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: UUID?) {
            writer.value(value.toString())
        }
    }

    // This Moshi instance does the real work for handling the JSON serialization/deserialization
    private val moshi = Moshi.Builder()
        .add(UUIDAdapter())
        .add(
            PolymorphicJsonAdapterFactory.of(ContentData::class.java, "type")
                .withSubtype(NoteData::class.java, ContentType.GENERIC_NOTE.name)
        )
        .add(KotlinJsonAdapterFactory())
        .add(Date::class.java, Rfc3339DateJsonAdapter())
        .add(NullToEmptyStringAdapter())
        .build()

    // And retrofit handles the REST communications.
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(CAPTUREURL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .client(okHttpClient)
        .build()

    // The single instance of the API service.
    val pinApiService: PinApiService = retrofit.create(PinApiService::class.java)

    // If this value is non-null, we think we have a valid session, but it can
    // expire at any time.
    private var accessToken: String? = null

    // This handler manages the propagation of cookies between the WebView cookie jar
    // and the cookie jar used by the OKHttp requests.
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

    // Filter out ipv6 addresses.  There seems to be some issue with these, particularly when
    // using Samsung devices.
    class Ipv4OnlyDns : Dns {
        override fun lookup(hostname: String): List<InetAddress> {
            return try {
                // Get all IP addresses for the hostname
                val allAddresses = Dns.SYSTEM.lookup(hostname)
                // Filter out IPv6 addresses
                val ipv4Addresses = allAddresses.filter { it is Inet4Address }
                // If no IPv4 addresses are found, throw an exception
                if (ipv4Addresses.isEmpty()) {
                    throw UnknownHostException("No IPv4 address found for $hostname")
                }
                ipv4Addresses
            } catch (e: Exception) {
                throw UnknownHostException("Failed to resolve $hostname: ${e.message}")
            }
        }
    }

    // This is the interceptor that inserts the Bearer Token into all requests and
    // handles 400 errors, refreshing the session as needed. As a last ditch effort,
    // it will delete all the cookies, forcing the user to login again.
    class AuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {

            // If the user hasn't been authorized, go ahead and refresh the session
            accessToken ?: refreshSession()

            // If, after that, the user still hasn't been authorized, clear the cookies and
            // throw an exception.
            accessToken ?: {
                clearCookies()
                throw APIError("The user is not authorized")
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
                    throw APIError("The user is not authorized")
                }
            }
            return response
        }
    }

    // Clear out the webkit cookies, forcing the user to login again the next
    // time the WebView is displayed.
    private fun clearCookies() {
        // This will trigger a reauthentication the next time we launch
        val webkitCookieManager = CookieManager.getInstance()
        webkitCookieManager.removeAllCookies(null)
    }

    // This checks for the cookie whose existence indicates that the user is
    // logged in.
    fun isAuthenticated(): Boolean {
        val cookie = CookieManager.getInstance().getCookie(INITIALURL)
        if ((cookie != null) && (cookie.contains(AUTHCOOKIE))) {
            Log.d("PinApi", "Already authenticated")
            return true
        }
        return false
    }

    // Determine whether we're online or not, based on network state.
//    fun isOnline() : Boolean {
//        val connectivityManager = PinSyncApp.applicationContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//
//        try {
//            val activeNetwork = connectivityManager.activeNetwork
//            val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
//            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
//        }
//        catch (securityException : SecurityException) {
//            Log.e("PinApi", "SecurityException: " + securityException.message)
//            // We have no real choice but to return true here and let the application crash.
//            // Otherwise, we risk not operating when we're actually online, but the user has chosen
//            // not to let us know.
//            return true
//        }
//    }

    // Sending a request to the session URL will result in the generation of a new SessionInfo
    // object containing a valid Access Token which can, in turn, be used as the Bearer token for
    // requests to the API server.
    private fun refreshSession(): Boolean {
        sessionOkHttpClient.newCall(sessionRequest).execute().use { response ->
            try {
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody =
                    response.body?.string() ?: throw NullPointerException("Response body is null")
                // Parse the string into a JSON object
                val sessionInfo = sessionInfoAdapter.fromJson(responseBody)
                return sessionInfo?.let {
                    accessToken = it.accessToken
                    Log.d("PinApi", "accessToken set")
                    true
                } ?: false
            } catch (e: JsonDataException) {
                // This is most likely because we didn't get a UserSession, meaning that the authentication
                // is out of date, so clear the cookies and return false.
                clearCookies()
                return false
            }
        }
    }
}

