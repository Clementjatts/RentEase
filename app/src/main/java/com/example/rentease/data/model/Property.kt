package com.example.rentease.data.model

import com.squareup.moshi.Json
import java.util.Date

data class Property(
    val id: Int,
    val title: String,
    val description: String?,
    @Json(name = "landlord_id") val landlordId: Int,
    val price: Double,
    val address: String = "",
    @Json(ignore = true) val dateAdded: Date = Date(),
    val type: String? = null,
    @Json(name = "landlord_name") var landlordName: String? = null,
    @Json(name = "landlord_contact") var landlordContact: String? = null,
    @Json(name = "bedroom_count") val bedroomCount: Int = 0,
    @Json(name = "bathroom_count") val bathroomCount: Int = 0,
    @Json(name = "furniture_type") val furnitureType: String = "unfurnished",
    @Json(name = "image_url") var imageUrl: String? = null
)
