package com.example.yadshniya.Model

class User(
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

//    fun getImageUrl(): String? = imageUrl
//
//    fun setImageUrl(imageUrl: String?) {
//        this.imageUrl = imageUrl
//    }
}

//
//class User(userName: String?, email: String?, imageUrl: String) {
//    var userName: String? = ""
//    var email: String? = ""
//    var imageUrl: String? = ""
//
//    init {
//        this.userName = userName
//        this.email = email
//        this.imageUrl = imageUrl
//
//    }
//
//    fun toJson(): Map<String, Any?> {
//        val userJson: MutableMap<String, Any?> = HashMap()
//
//        userJson["email"] = email
//        userJson["userName"] = userName
//        userJson["imageUrl"] = imageUrl
//
//        return userJson
//    }
//
//    companion object {
//        const val COLLECTION_NAME: String = "users"
//
//        fun createUser(userJson: Map<String?, Any?>): User {
//            val email = userJson["email"] as String?
//            val userName = userJson["userName"] as String?
//            val imageUrl = userJson["imageUrl"] as String?
//
//            val user = User(userName, email, imageUrl!!)
////            user.imageUrl = imageUrl
//
//            return user
//        }
//    }
//
//    fun getImageUrl(): String {
//        return imageUrl!!
//    }
//
//    fun setImageUrl(imageUrl: String?) {
//        this.imageUrl = imageUrl
//    }
//}