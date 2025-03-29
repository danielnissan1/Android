package com.example.yadshniya.Model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class User(
    @PrimaryKey
    var id: String? = null,
    var userName: String? = null,
    var email: String? = null,
    var imageUrl: String? = null
) {
    fun toJson(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "email" to email,
            "userName" to userName,
            "imageUrl" to imageUrl
        )
    }

    companion object {
        const val COLLECTION_NAME: String = "users"

        fun createUser(userJson: Map<String?, Any?>): User {
            val id = userJson["id"] as String
            val email = userJson["email"] as? String
            val userName = userJson["userName"] as? String
            val imageUrl = userJson["imageUrl"] as? String

            return User(id, userName, email, imageUrl)
        }
    }
}