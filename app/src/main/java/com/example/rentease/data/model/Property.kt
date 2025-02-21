package com.example.rentease.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "properties",
    foreignKeys = [
        ForeignKey(
            entity = Landlord::class,
            parentColumns = ["id"],
            childColumns = ["landlordId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Property(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String?,
    val landlordId: Int,
    // Additional fields for the UI
    var landlordName: String? = null,
    var landlordContact: String? = null
)
