package com.example.rentease.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Request model for user login
@JsonClass(generateAdapter = true)
data class LoginRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

// Response model for successful login
@JsonClass(generateAdapter = true)
data class LoginResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String,
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: LoginResponseData
)

// Data payload within login response containing token and user info
@JsonClass(generateAdapter = true)
data class LoginResponseData(
    @Json(name = "token") val token: String,
    @Json(name = "user") val user: User
)

// User model representing a system user (admin or landlord)
@JsonClass(generateAdapter = true)
data class User(
    @Json(name = "id") val id: Int,
    @Json(name = "username") val username: String,
    @Json(name = "email") val email: String?,
    @Json(name = "user_type") val userType: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "phone") val phone: String?,
    @Json(name = "created_at") val createdAt: String?
)

// Request model for user registration
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "email") val email: String,
    @Json(name = "user_type") val userType: String,
    @Json(name = "full_name") val fullName: String,
    @Json(name = "phone") val phone: String
)

// Request model for changing user password
@JsonClass(generateAdapter = true)
data class ChangePasswordRequest(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

// Error model for API error responses
@JsonClass(generateAdapter = true)
data class ApiError(
    @Json(name = "message") val message: String,
    @Json(name = "errors") val errors: Map<String, List<String>>? = null
)
