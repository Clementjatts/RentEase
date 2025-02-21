package com.example.rentease.data.api

import com.example.rentease.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface RentEaseApi {
    // Landlord endpoints
    @POST("landlord/create.php")
    suspend fun createLandlord(@Body landlord: Landlord): Response<ApiResponse>

    @GET("landlord/read.php")
    suspend fun getLandlords(): Response<ApiResponse>

    @GET("landlord/read.php")
    suspend fun getLandlord(@Query("id") id: Int): Response<ApiResponse>

    @PUT("landlord/update.php")
    suspend fun updateLandlord(@Body landlord: Landlord): Response<ApiResponse>

    @DELETE("landlord/delete.php")
    suspend fun deleteLandlord(@Query("id") id: Int): Response<ApiResponse>

    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @GET("auth/user")
    suspend fun getCurrentUser(): Response<User>

    // Property endpoints
    @POST("property/create.php")
    suspend fun createProperty(@Body property: Property): Response<ApiResponse>

    @GET("property/read.php")
    suspend fun getProperties(): Response<ApiResponse>

    @GET("property/read.php")
    suspend fun getProperty(@Query("id") id: Int): Response<ApiResponse>

    @PUT("property/update.php")
    suspend fun updateProperty(@Body property: Property): Response<ApiResponse>

    @DELETE("property/delete.php")
    suspend fun deleteProperty(@Query("id") id: Int): Response<ApiResponse>

    @GET("properties")
    suspend fun getPropertiesList(): Response<List<Property>>

    @GET("properties/{id}")
    suspend fun getPropertyPath(@Path("id") id: Int): Response<Property>

    @POST("properties")
    suspend fun createPropertyPath(@Body property: Property): Response<Property>

    @PUT("properties/{id}")
    suspend fun updatePropertyPath(
        @Path("id") id: Int,
        @Body property: Property
    ): Response<Property>

    @DELETE("properties/{id}")
    suspend fun deletePropertyPath(@Path("id") id: Int): Response<Unit>

    @Multipart
    @POST("properties/{id}/images")
    suspend fun uploadPropertyImage(
        @Path("id") propertyId: Int,
        @Part("image") image: okhttp3.MultipartBody.Part
    ): Response<String>

    // User request endpoints
    @POST("request/create.php")
    suspend fun createRequest(@Body request: UserRequest): Response<ApiResponse>

    @GET("request/read.php")
    suspend fun getRequests(): Response<ApiResponse>

    @GET("request/read.php")
    suspend fun getUserRequests(@Query("user_id") userId: String): Response<ApiResponse>
}

data class ApiResponse(
    val status: String,
    val message: String?,
    val data: Any?
)
