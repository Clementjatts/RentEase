package com.example.rentease.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.util.Date

@Entity(
    tableName = "properties",
    foreignKeys = [
        ForeignKey(
            entity = Landlord::class,
            parentColumns = ["id"],
            childColumns = ["landlordId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["landlordId"])
    ]
)
data class Property(
    @PrimaryKey val id: Int,
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
