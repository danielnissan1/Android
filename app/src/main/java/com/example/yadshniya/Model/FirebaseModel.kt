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
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream

class FirebaseModel internal constructor() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var storage: FirebaseStorage
    var auth: FirebaseAuth

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .build()
        db.firestoreSettings = settings
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }

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
        val userRef = db.collection(User.COLLECTION_NAME).document()
        user.id = userRef.id

        val userJson = user.toJson()
        db.collection(User.COLLECTION_NAME).document(user.email!!)
            .set(userJson)
            .addOnCompleteListener {
                callback()
            }
    }


    fun createPost(post: Post, callback: EmptyCallback) {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d("TAG", "User ID: ${currentUser.uid}")
            Log.d("TAG", "User Email: ${currentUser.email}")
            Log.d("TAG", "User Display Name: ${currentUser.displayName}")

            getUserByEmail(currentUser.email!!) { user ->
                if (user != null) {
                    post.userId = user.id
                    Log.d("TAG", "Fetched User ID: ${user.id}")

                    val postRef = db.collection(Post.COLLECTION_NAME).document()
                    val postId = postRef.id
                    post.id = postId

                    val postJson = post.toJson()

                    postRef.set(postJson)
                        .addOnCompleteListener {
                            callback()
                        }
                } else {
                    Log.d("TAG", "User with email ${currentUser.email} not found")
                }
            }
        } else {
            Log.d("TAG", "No user is logged in")
        }
    }

    fun getUserByEmail(email: String, callback: (User?) -> Unit) {
        db.collection(User.COLLECTION_NAME)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming there's only one user with the given email
                    val user = querySnapshot.documents[0].toObject(User::class.java)
                    callback(user)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.d("TAG", "Error fetching user: ", exception)
                callback(null)
            }
    }



    fun getAllPosts(callback: PostsCallback) {
        db.collection(Post.COLLECTION_NAME).get(Source.SERVER)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val posts: MutableList<Post> = mutableListOf()
                    val postDocuments = task.result!!.documents

                    if (postDocuments.isNotEmpty()) {
                        val postFetchCount = postDocuments.size
                        var completedPosts = 0

                        for (doc in postDocuments) {
                            val post = Post.fromJSON(doc.data!!)

                            db.collection(User.COLLECTION_NAME)
                                .whereEqualTo("id", post.userId)
                                .get()
                                .addOnSuccessListener { userQuerySnapshot ->
                                    if (!userQuerySnapshot.isEmpty) {
                                        val userDoc = userQuerySnapshot.documents[0]
                                        post.ownerName = userDoc.getString("userName") ?: "Unknown"
                                        post.ownerImageUrl = userDoc.getString("imageUrl") ?: ""
                                    }

                                    posts.add(post)
                                    completedPosts++

                                    if (completedPosts == postFetchCount) {
                                        callback(posts)
                                    }
                                }
                                .addOnFailureListener {
                                    completedPosts++
                                    if (completedPosts == postFetchCount) {
                                        callback(posts)
                                    }
                                }
                        }
                    } else {
                        callback(posts)
                    }
                } else {
                    callback(listOf())
                }
            }
    }


    fun getUserById(userId: String, callback: (User?) -> Unit) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val user = document.toObject(User::class.java)
                    callback(user)
                } else {
                    Log.e("FirebaseModel", "User with ID $userId not found")
                    callback(null)  // Return null instead of crashing
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseModel", "Error fetching user", e)
                callback(null)
            }
    }
}
