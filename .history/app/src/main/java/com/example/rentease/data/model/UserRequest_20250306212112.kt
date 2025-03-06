package com.example.rentease.data.model

data class UserRequest(
    val id: Int,
    val userId: String,
    val propertyId: Int,
    val message: String?,
    val createdAt: String
)
