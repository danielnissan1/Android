package com.example.yadshniya.Model


import android.content.Context
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yadshniya.MyApplication
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.GeoPoint

@Entity
class Post(
    @PrimaryKey
            var id: String,
            var description: String,
            var location: String,
            var userId: String? = null,
            var imageUrl: String? = null,
            var deleted: Boolean? = false,
            var lastUpdated: Long = 0) {


//    constructor(
//        id: String,
//        description: String,
//        location: String?,
//        userId: String?,
//
//    ) {
//        this.id = id
//        this.description = description
//        this.location = location
//        this.userId = userId
//        this.deleted = false
//        this.lastUpdated = 0
//
//    }
//
//    constructor() {
//        this.id = ""
//        this.description = ""
//        this.location = ""
//        this.userId = ""
//        this.deleted = false
//    }

    enum class Difficulty {
        Easy,
        Medium,
        Hard
    }

    fun toJson(): Map<String, Any?> {
        val json: MutableMap<String, Any?> = HashMap()
        json["id"] = id
        json["description"] = description
        json["location"] = location
        json["userId"] = userId
        json["imageUrl"] = imageUrl
        json["isDeleted"] = deleted

        json[LAST_UPDATED] = FieldValue.serverTimestamp()
        return json
    }

    companion object {
        const val COLLECTION_NAME: String = "post"
        const val LAST_UPDATED: String = "lastUpdated"
        const val LOCAL_LAST_UPDATED: String = "post_local_last_update"

        var localLastUpdate: Long?
            get() {
                val sharedPref =
                    MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                return sharedPref.getLong(LOCAL_LAST_UPDATED, 0)
            }
            set(time) {
                val sharedPref =
                    MyApplication.context.getSharedPreferences("TAG", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putLong(LOCAL_LAST_UPDATED, time!!)
                editor.commit()
            }

        fun createPost(postJson: Map<String?, Any?>, docId: String): Post {
            val description = postJson["description"] as String
            val location = postJson["location"] as String
            val userId = postJson["userId"] as String?
            val imageUrl = postJson["imageUrl"] as String?
            val isDeleted = postJson["isDeleted"] as Boolean?

            val timestamp = postJson[LAST_UPDATED] as Timestamp?
            val lastUpdated = timestamp!!.seconds

            val postItem = Post(
                docId,
                description,
                location,
                userId,
                imageUrl,
                isDeleted,
            )

            postItem.lastUpdated = lastUpdated
            postItem.imageUrl = imageUrl
            postItem.id = docId
            postItem.deleted = isDeleted
            return postItem
        }
    }
}
