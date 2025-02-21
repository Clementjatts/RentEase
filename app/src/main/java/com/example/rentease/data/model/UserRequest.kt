package com.example.rentease.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_requests",
    foreignKeys = [
        ForeignKey(
            entity = Property::class,
            parentColumns = ["id"],
            childColumns = ["propertyId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserRequest(
    @PrimaryKey val id: Int,
    val userId: String,
    val propertyId: Int,
    val message: String?,
    val createdAt: String
)
