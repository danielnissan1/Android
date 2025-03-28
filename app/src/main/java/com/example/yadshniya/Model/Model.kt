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

    interface Listener<T> {
        fun onComplete(data: T)
    }

    private var userPostsList: LiveData<List<Post?>>? = null

    private var myPostsList: LiveData<List<Post>>? = null

    private val allPosts
    : LiveData<List<Post>>? = null

    private val userPosts : LiveData<List<Post>>? = null


    val MyPost: LiveData<List<Post>>?
        get() {
            if (myPostsList == null) {
                myPostsList = localDb.PostDao()?.getPostsByUser(firebaseModel.userEmail)
//                refreshAllMaslulim()
            }
            return myPostsList
        }

    fun refreshAllPosts() {
        EventPostsListLoadingState.setValue(PostsListLoadingState.LOADING)

        val localLastUpdate: Long? = Post.localLastUpdate

        firebaseModel.getAllPosts() { list ->
            executor.execute {
                var time = localLastUpdate

//                localDb.PostDao().deleteAll()

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

    fun deletePost(post: Post) {
        executor.execute {
            val posts = localDb.PostDao().getAll().value
            Log.d("Delete", "All posts in DB: ${posts?.size}")

            localDb.PostDao().delete(post)
            Log.d("Delete", "Deleted post from local DB: ${post.id}")
        }
        val remainingPosts = localDb.PostDao().getAll().value
        Log.d("Delete", "Remaining posts in DB: ${remainingPosts?.size}")

        firebaseModel.deletePost(post.id) { success ->
            if (success) {
                Log.d("Delete", "Post deleted from Firestore")
            } else {
                Log.d("Delete", "Failed to delete post from Firestore")
            }
        }
    }


    fun getCurrentUserPosts(userId: String): LiveData<List<Post>> {
        return allPosts?:localDb.PostDao().getPostsByUser(userId)
    }

    fun register(email: String?, password: String?, listener: (FirebaseUser?) -> Unit) {
        firebaseModel.register(email, password, listener)
    }

    fun login(email: String?, password: String?, listener: (FirebaseUser?) -> Unit) {
        firebaseModel.login(email, password, listener)
    }

//    val isSignedIn: Boolean
//        get() = firebaseModel.isSignedIn()

//    fun signOut() {
//        EventPostsListLoadingState.postValue(null)
//        firebaseModel.signOut()
//    }

//    fun createUser(user: User, listener: (User?) -> Unit) {
//        firebaseModel.createUser(user, listener)
//    }

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
        firebaseModel.updateUser(user) { // Update username first
            img?.let {
                cloudinaryModel.uploadBitmap(it) { url ->
                    if (!url.isNullOrEmpty()) {
                        user.imageUrl = url
                        firebaseModel.updateUser(user, callback) // Update Firestore again with the new image URL
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





//    fun getAllPosts(callback: PostsCallback) {
//        firebaseModel.getAllPosts(callback)
//    }



//    fun getUserById(email: String?, listener: (FirebaseUser?) -> Unit) {
//            firebaseModel.getUserById(email, listener)
//    }

//    val userEmail: String
//        get() = firebaseModel.getUserEmail()

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