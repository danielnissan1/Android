package com.example.yadshniya.Model


class User(userName: String?, email: String?) {
    var userName: String? = ""
    var email: String? = ""
    var imageUrl: String? = null

    init {
        this.userName = userName
        this.email = email

    }

    fun toJson(): Map<String, Any?> {
        val userJson: MutableMap<String, Any?> = HashMap()

        userJson["email"] = email
        userJson["userName"] = userName
        userJson["imageUrl"] = imageUrl

        return userJson
    }

    companion object {
        const val COLLECTION_NAME: String = "users"

        fun createUser(userJson: Map<String?, Any?>): User {
            val email = userJson["email"] as String?
            val userName = userJson["userName"] as String?
            val imageUrl = userJson["imageUrl"] as String?

            val user = User(userName, email)
            user.imageUrl = imageUrl

            return user
        }
    }
}