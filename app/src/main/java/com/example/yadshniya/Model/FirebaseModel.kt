package com.example.yadshniya.Model

import android.graphics.Bitmap
import android.widget.Toast
import com.example.yadshniya.MyApplication
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.Timestamp
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.util.LinkedList

class FirebaseModel internal constructor() {
    var db: FirebaseFirestore = FirebaseFirestore.getInstance()
    var storage: FirebaseStorage
    var auth: FirebaseAuth

    init {
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        db.firestoreSettings = settings
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
    }

    fun uploadImage(name: String, bitmap: Bitmap, listener: Model.Listener<String?>) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images/$name.jpg")
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val uploadTask = imagesRef.putBytes(data)
        uploadTask.addOnFailureListener { listener.onComplete(null) }.addOnSuccessListener {
            imagesRef.downloadUrl.addOnSuccessListener { uri ->
                listener.onComplete(
                    uri.toString()
                )
            }
        }
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

    fun login(email: String?, password: String?, listener: Model.Listener<FirebaseUser?>) {
        auth.signInWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    listener.onComplete(auth.currentUser)
                } else {
                    Toast.makeText(
                        MyApplication.getContext(), task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    listener.onComplete(null)
                }
            }
    }

    fun signOut() {
        auth.signOut()
    }

    fun register(email: String?, password: String?, listener: Model.Listener<FirebaseUser?>) {
        auth.createUserWithEmailAndPassword(email!!, password!!)
            .addOnCompleteListener { task: Task<AuthResult?> ->
                if (task.isSuccessful) {
                    listener.onComplete(auth.currentUser)
                } else {
                    Toast.makeText(
                        MyApplication.getContext(), task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                    listener.onComplete(null)
                }
            }
    }

    fun addUser(user: User, listener: Model.Listener<User?>) {
        val userJson = user.toJson()
        db.collection(User.COLLECTION_NAME)
            .document(user.email!!)
            .set(userJson)
            .addOnSuccessListener { unused: Void? -> listener.onComplete(user) }
            .addOnFailureListener { e: Exception? -> listener.onComplete(user) }
    }

    fun getUserById(email: String?, listener: Model.Listener<User?>) {
        db.collection(User.COLLECTION_NAME)
            .document(email!!)
            .get()
            .addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                var user: User? = null
                if (task.isSuccessful and (task.result != null)) {
                    user = task.result!!.data?.let { User.createUser(it) }
                }
                listener.onComplete(user)
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
