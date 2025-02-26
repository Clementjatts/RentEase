package com.example.rentease.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "favorite_properties",
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["propertyId"])
    ]
)
data class FavoriteProperty(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,  // Using email as userId for simplicity
    val propertyId: Int,
    val createdAt: String
)
