package com.example.rentease.data.model

import java.math.BigDecimal
import java.util.Date

data class Property(
    val id: Int,
    val title: String,
    val description: String?,
    val landlordId: Int,
    val price: BigDecimal,
    val address: String,
    val dateAdded: Date = Date(),
    val type: String? = null,
    // Additional fields for the UI
    var landlordName: String? = null,
    var landlordContact: String? = null,
    var images: List<String>? = null
)
