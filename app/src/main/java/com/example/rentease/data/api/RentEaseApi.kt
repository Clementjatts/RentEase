package com.example.rentease.data.api

import com.example.rentease.data.model.ApiResponse
import com.example.rentease.data.model.ChangePasswordRequest
import com.example.rentease.data.model.LoginRequest
import com.example.rentease.data.model.LoginResponse
import com.example.rentease.data.model.Property
import com.example.rentease.data.model.RegisterRequest
import com.example.rentease.data.model.User
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface RentEaseApi {
    // Landlord endpoints
    @GET("landlords")
    suspend fun getLandlords(): Response<ApiResponse>

    @GET("landlords/by-user")
    suspend fun getLandlordByUserId(): Response<ApiResponse>

    @GET("landlords/{id}")
    suspend fun getLandlord(@Path("id") id: Int): Response<ApiResponse>

    @PUT("landlords/{id}")
    suspend fun updateLandlord(@Path("id") id: Int, @Body data: Map<String, String>): Response<ApiResponse>

    @DELETE("landlords/{id}")
    suspend fun deleteLandlord(@Path("id") id: Int): Response<ApiResponse>

    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @GET("auth/me")
    suspend fun getCurrentUser(): Response<User>

    // User endpoints
    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Int, @Body user: User): Response<ApiResponse>

    // Property endpoints
    @POST("properties")
    suspend fun createProperty(@Body property: Property): Response<ApiResponse>

    @GET("properties")
    suspend fun getProperties(): Response<ApiResponse>

    @GET("properties/{id}")
    suspend fun getProperty(@Path("id") id: Int): Response<ApiResponse>

    @PUT("properties/{id}")
    suspend fun updateProperty(@Path("id") id: Int, @Body property: Property): Response<ApiResponse>

    @DELETE("properties/{id}")
    suspend fun deleteProperty(@Path("id") id: Int): Response<ApiResponse>

    @Multipart
    @POST("properties/upload-image")
    suspend fun uploadPropertyImage(
        @Part("property_id") propertyId: okhttp3.RequestBody,
        @Part image: MultipartBody.Part
    ): Response<ApiResponse>

    @GET("properties/{id}/images")
    suspend fun getPropertyImages(@Path("id") propertyId: Int): Response<ApiResponse>

    @DELETE("properties/images/{id}")
    suspend fun deletePropertyImage(@Path("id") imageId: Int): Response<ApiResponse>
}
