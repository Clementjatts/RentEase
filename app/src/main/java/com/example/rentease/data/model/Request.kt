package com.example.rentease.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

data class Request(
    val id: Int,
    @Json(name = "property_id") val propertyId: Int,
    @Json(name = "landlord_id") val landlordId: Int,
    @Json(name = "requester_name") val requesterName: String,
    @Json(name = "requester_email") val requesterEmail: String,
    @Json(name = "requester_phone") val requesterPhone: String?,
    val message: String,
    @Json(name = "is_read") val isRead: Boolean,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "property_title") val propertyTitle: String,
    @Json(name = "property_address") val propertyAddress: String
)

@JsonClass(generateAdapter = true)
data class CreateRequestData(
    @Json(name = "property_id") val propertyId: Int,
    @Json(name = "landlord_id") val landlordId: Int,
    @Json(name = "requester_name") val requesterName: String,
    @Json(name = "requester_email") val requesterEmail: String,
    @Json(name = "requester_phone") val requesterPhone: String?,
    val message: String
)
