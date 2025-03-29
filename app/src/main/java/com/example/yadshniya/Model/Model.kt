package com.example.yadshniya.Model

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.yadshniya.EmptyCallback
import com.example.yadshniya.MyApplication
import com.example.yadshniya.PostsCallback
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class Model private constructor() {
    enum class PostsListLoadingState {
        LOADING,
        NOT_LOADING
    }

    private val firebaseModel: FirebaseModel = FirebaseModel()
    private val cloudinaryModel = CloudinaryModel()

    var executor: Executor = Executors.newFixedThreadPool(1)
    var mainThread: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
    var localDb: AppLocalDbRepository = AppLocalDbRepository.getDatabase(MyApplication.context)

    val EventPostsListLoadingState: MutableLiveData<PostsListLoadingState> =
        MutableLiveData(PostsListLoadingState.NOT_LOADING)


    private val allPosts
    : LiveData<List<Post>>? = null


    fun refreshAllPosts() {
        EventPostsListLoadingState.setValue(PostsListLoadingState.LOADING)

        val localLastUpdate: Long? = Post.localLastUpdate

        firebaseModel.getAllPosts() { list ->
            executor.execute {
                var time = localLastUpdate

                localDb.PostDao().deleteAll()

                for (post in list!!) {
                    if (post!!.deleted == true) {
                        localDb.PostDao().delete(post)
                    } else {
                        firebaseModel.getUserById(post.userId!!) { user ->
                            if (user != null) {
                                post.ownerImageUrl = user.imageUrl
                                post.ownerName = user.userName

                            }
                            executor.execute {
                                localDb.PostDao().insertAll(post)
                            }
                        }
                    }

                    if (time!! < post.lastUpdated) {
                        time = post.lastUpdated
                    }
                }

                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                Post.localLastUpdate = time
                EventPostsListLoadingState.postValue(PostsListLoadingState.NOT_LOADING)
            }
        }
    }

    fun refreshUsersPosts(userId: String) {
        EventPostsListLoadingState.setValue(PostsListLoadingState.LOADING)

        val localLastUpdate: Long? = Post.localLastUpdate

        firebaseModel.getCurrentUserPosts(userId) { list ->
            executor.execute {
                var time = localLastUpdate

                for (post in list!!) {
                    if (post!!.deleted == true) {
                        localDb.PostDao().delete(post)
                    } else {
                        firebaseModel.getUserById(post.userId!!) { user ->
                            if (user != null) {
                                post.ownerImageUrl = user.imageUrl
                                post.ownerName = user.userName

                            }
                            executor.execute {
                                localDb.PostDao().insertAll(post)
                            }
                        }
                    }

                    if (time!! < post.lastUpdated) {
                        time = post.lastUpdated
                    }
                }

                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }

                Post.localLastUpdate = time
                EventPostsListLoadingState.postValue(PostsListLoadingState.NOT_LOADING)
            }
        }
    }

    fun getAllPosts(): LiveData<List<Post>> {
        return allPosts?:localDb.PostDao().getAll()
    }

    fun deletePost(post: Post, callback: EmptyCallback) {
        post.deleted = true
        executor.execute {
            localDb.PostDao().delete(post)
            Log.d("Delete", "Deleted post from local DB: ${post.id}")
        }
        val remainingPosts = localDb.PostDao().getAll().value
        Log.d("Delete", "Remaining posts in DB: ${remainingPosts?.size}")

        firebaseModel.deletePost(post.id) { success ->
            refreshAllPosts()

                if (success) {
                    Log.d("Delete", "Post deleted from Firestore")
                } else {
                    Log.d("Delete", "Failed to delete post from Firestore")
                }

        }
    }

    fun updatePost(postId: String, newDescription: String, newPrice: Double) {
        val lastUpdated = System.currentTimeMillis()

        try {
        // Update in local database (Room)
        executor.execute {
            localDb.PostDao().updatePost(postId, newPrice, newDescription, lastUpdated)
            Log.d("Update", "Updated post in local DB: ${postId}")
        }

        // Update in Firestore
        firebaseModel.updatePostInFirestore(postId, newDescription, newPrice) { success ->
            if (success) {
                Log.d("Update", "Post updated successfully in Firestore")
            } else {
                Log.d("Update", "Failed to update post in Firestore")
            }
        }
        } catch (e: Exception) {
        Log.e("Update", "Error updating post: ", e)}
    }



    fun getCurrentUserPosts(user: FirebaseUser, callback: (List<Post>) -> Unit) {
        firebaseModel.getUserByEmail(user.email!!) { user ->
            if (user != null) {
                val userId = user.id.toString()
                refreshUsersPosts(userId)

                executor.execute {
                    val liveDataPosts = localDb.PostDao().getPostsByUser(userId)


                    Handler(Looper.getMainLooper()).post {
                        liveDataPosts.observeForever { posts ->
                            callback(posts)
                        }
                    }
                }
            } else {
                callback(emptyList())
            }
        }
    }



    fun register(email: String?, password: String?, listener: (FirebaseUser?) -> Unit) {
        firebaseModel.register(email, password, listener)
    }

    fun login(email: String?, password: String?, listener: (FirebaseUser?) -> Unit) {
        firebaseModel.login(email, password, listener)
    }

    fun createUser(user: User, img:Bitmap?, callback: EmptyCallback) {
        firebaseModel.createUser(user) {
            callback()
        }
        img?.let {
            cloudinaryModel.uploadBitmap(it) { url ->
                if (!url.isNullOrEmpty()) {
                    user.imageUrl = url
                    firebaseModel.createUser(user, callback)
                }
            }
        } ?: callback()
    }

    fun updateUser(user: User, img: Bitmap?, callback: EmptyCallback) {
        firebaseModel.updateUser(user) {
            img?.let {
                cloudinaryModel.uploadBitmap(it) { url ->
                    if (!url.isNullOrEmpty()) {
                        user.imageUrl = url
                        firebaseModel.updateUser(user, callback)
                    } else {
                        callback()
                    }
                }
            } ?: callback()
        }
    }


    fun createPost(post: Post, img: Bitmap?, callback: EmptyCallback) {
        EventPostsListLoadingState.postValue(PostsListLoadingState.LOADING)

        val onComplete: () -> Unit = {
            mainThread.post {
                EventPostsListLoadingState.postValue(PostsListLoadingState.NOT_LOADING)
                callback()
            }
        }

        if (img != null) {
            cloudinaryModel.uploadBitmap(img) { url ->
                if (!url.isNullOrEmpty()) {
                    post.imageUrl = url
                }

                firebaseModel.createPost(post) {
                    executor.execute {
                        localDb.PostDao().insertAll(post)
                        onComplete()
                    }
                }
            }
        } else {
            firebaseModel.createPost(post) {
                executor.execute {
                    localDb.PostDao().insertAll(post)
                    onComplete()
                }
            }
        }
    }

    fun uploadImage(name: String, bitmap: Bitmap, listener: (String?) -> Unit) {
//            firebaseModel.uploadImage(name, bitmap, listener)
    }


    companion object {

        private val instance = Model()
        fun instance(): Model {
            return instance
        }
    }
}