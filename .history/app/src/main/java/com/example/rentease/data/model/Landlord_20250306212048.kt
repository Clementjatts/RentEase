package com.example.rentease.data.model

data class Landlord(
    val id: Int,
    val name: String,
    val contact: String,
    val adminId: Int,
    val email: String = "",
    val status: String = "pending"
)
