package com.example.rentease.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "landlords")
data class Landlord(
    @PrimaryKey val id: Int,
    val name: String,
    val contact: String,
    val adminId: Int
)
