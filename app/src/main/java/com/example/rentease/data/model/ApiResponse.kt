package com.example.rentease.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// Standard API response wrapper for all backend responses
@JsonClass(generateAdapter = true)
data class ApiResponse(
    @Json(name = "status") val status: String,
    @Json(name = "message") val message: String?,
    @Json(name = "data") val data: Any? = null,
    @Json(name = "error") val error: String? = null
) {
    val success: Boolean
        get() = status == "success"
}
