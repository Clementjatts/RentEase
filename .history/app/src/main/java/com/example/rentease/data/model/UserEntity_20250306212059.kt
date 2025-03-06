package com.example.rentease.data.model

data class UserEntity(
    val id: Int,
    val username: String,
    val email: String?,
    val userType: String,
    val fullName: String?,
    val phone: String?,
    val createdAt: String
) {
    companion object {
        fun fromUser(user: User): UserEntity {
            return UserEntity(
                id = user.id,
                username = user.username,
                email = user.email,
                userType = user.userType,
                fullName = user.fullName,
                phone = user.phone,
                createdAt = user.createdAt
            )
        }
        
        fun toUser(entity: UserEntity): User {
            return User(
                id = entity.id,
                username = entity.username,
                email = entity.email,
                userType = entity.userType,
                fullName = entity.fullName,
                phone = entity.phone,
                createdAt = entity.createdAt
            )
        }
    }
}
