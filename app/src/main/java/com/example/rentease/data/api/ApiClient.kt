package com.example.rentease.data.api

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object ApiClient {
    // Backend URL - uses localhost with ADB reverse port forwarding
    private const val BASE_URL = "http://localhost:8000/"

    // Single client with conditional auth interceptor
    private fun createClient(context: Context): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val authManager = com.example.rentease.auth.AuthManager.getInstance(context)

                // Add auth header if user is logged in and has token
                if (authManager.isLoggedIn && !authManager.authToken.isNullOrEmpty()) {
                    val newRequest = request.newBuilder()
                        .header("Authorization", "Bearer ${authManager.authToken}")
                        .build()
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(request)
                }
            }
            .build()
    }

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val moshiConverterFactory = MoshiConverterFactory.create(moshi)

    // Single API instance that handles both authenticated and non-authenticated requests
    fun getApi(context: Context): RentEaseApi {
        val client = createClient(context)
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(moshiConverterFactory)
            .build()
        return retrofit.create(RentEaseApi::class.java)
    }
}
