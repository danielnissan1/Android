package com.example.yadshniya.Model

import android.graphics.Bitmap
import android.util.Log
import android.widget.Toast
import com.example.yadshniya.EmptyCallback
import com.example.yadshniya.MyApplication
import com.example.yadshniya.PostsCallback
import com.google.android.gms.tasks.Task
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class FirebaseModel internal constructor() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var storage: FirebaseStorage
    var auth: FirebaseAuth

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .build()  // No need to set persistence explicitly
        db.firestoreSettings = settings
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }

//    fun uploadImage(name: String, bitmap: Bitmap, listener: (String?) -> Unit) {
//        val storageRef = storage.reference
//        val imagesRef = storageRef.child("images/$name.jpg")
//        val baos = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
//        val data = baos.toByteArray()
//
//        val uploadTask = imagesRef.putBytes(data)
//        uploadTask.addOnFailureListener { listener(null) }
//            .addOnSuccessListener {
//                imagesRef.downloadUrl.addOnSuccessListener { uri ->
//                    listener(uri.toString())
////                    val user = FirebaseAuth.getInstance().currentUser
////
////                    if (user != null) {
////                        val profileUpdates = UserProfileChangeRequest.Builder()
////                            .setPhotoUri(uri)
////                            .build()
////
////                        user.updateProfile(profileUpdates)
////                            .addOnCompleteListener { task ->
////                                if (task.isSuccessful) {
////                                    listener(user) // Return updated FirebaseUser
////                                } else {
////                                    listener(null)
////                                }
////                            }
////                    } else {
////                        listener(null)
////                    }
//                }
//            }
//
//    }

    val isSignedIn: Boolean
        get() {
            val currentUser = auth.currentUser
            return (currentUser != null)
        }

    val userEmail: String?
        get() {
            val currentUser = auth.currentUser
            return currentUser!!.email
        }

    fun login(email: String?, password: String?, listener: (FirebaseUser?) -> Unit) {
        auth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    listener(auth.currentUser)
                } else {
                    Toast.makeText(
                        MyApplication.context, task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    listener(null)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun register(email: String?, password: String?, listener: (FirebaseUser?) -> Unit) {
        auth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    listener(auth.currentUser)
                } else {
                    Toast.makeText(
                        MyApplication.context, task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    listener(null)
                }
            }
    }

    fun createUser(user: User, callback: EmptyCallback) {
        val userRef = db.collection("posts").document()
        user.id = userRef.id

        val userJson = user.toJson()
        db.collection(User.COLLECTION_NAME).document(user.email!!)
            .set(userJson)
            .addOnCompleteListener {
                callback()
            }
//        val userJson = user.toJson()
//        db.collection(User.COLLECTION_NAME)
//            .document(user.email!!)
//            .set(userJson)
//            .addOnSuccessListener { unused: Void? -> listener(user) }
//            .addOnFailureListener { e: Exception? -> listener(user) }
//    }
    }

    fun createPost(post: Post, callback: EmptyCallback) {
        val postRef = db.collection("posts").document()
        val postId = postRef.id  // Generated ID
        post.id = postId
        val postJson = post.toJson()
        db.collection(Post.COLLECTION_NAME).document(postId)
            .set(postJson)
            .addOnCompleteListener{
                callback()
            }
    }

    fun getAllPosts(callback: PostsCallback) {
        db.collection(Post.COLLECTION_NAME).get()
            .addOnCompleteListener {
                when (it.isSuccessful) {
                    true -> {
                        val students: MutableList<Post> = mutableListOf()
                        for (json in it.result) {
                            students.add(Post.fromJSON(json.data))
                        }
                        callback(students)
                    }
                    false -> callback(listOf())
                }
            }

    }

        fun getUserById(email: String?, listener: (FirebaseUser?) -> Unit) {
            db.collection(User.COLLECTION_NAME)
                .document(email!!)
                .get()
                .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    var user: User? = null
                    if (task.isSuccessful and (task.result != null)) {
                        user = task.result!!.data?.let { User.createUser(it) }
                    }
                    listener(user as FirebaseUser)
                }
        }


//    fun getAllMaslulimSince(since: Long?, callback: Model.Listener<List<Maslul>?>) {
//        db.collection(Maslul.COLLECTION_NAME)
//            .whereGreaterThanOrEqualTo(Maslul.LAST_UPDATED, Timestamp(since!!, 0))
//            .get()
//            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> { task ->
//                val list: MutableList<Maslul> = LinkedList<Maslul>()
//                if (task.isSuccessful) {
//                    val jsonsList = task.result
//                    for (json in jsonsList) {
//                        val maslul: Maslul = Maslul.createMaslul(json.data, json.id)
//                        list.add(maslul)
//                    }
//                }
//                callback.onComplete(list)
//            })
//    }
//
//    fun saveMaslul(maslul: Maslul, listener: Model.Listener<Void?>) {
//        // Add maslul case - get new free id
//        var maslulId: String = maslul.getId()
//        if (maslulId == "") {
//            val rootRef = FirebaseFirestore.getInstance()
//            val maslulsRef = rootRef.collection(Maslul.COLLECTION_NAME)
//            maslulId = maslulsRef.document().id
//        }
//
//        db.collection(Maslul.COLLECTION_NAME)
//            .document(maslulId)
//            .set(maslul.toJson())
//            .addOnSuccessListener { unused: Void? -> listener.onComplete(null) }
//            .addOnFailureListener { e: Exception? -> listener.onComplete(null) }
//    }
    }
