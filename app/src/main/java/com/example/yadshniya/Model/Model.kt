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

    private var allPostsList: LiveData<List<Post?>>? = null

    private var myPostsList: LiveData<List<Post>>? = null

    private val allPosts
    : LiveData<List<Post>>? = null


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

        // get local last update
        val localLastUpdate: Long? = Post.localLastUpdate

        // get all updated records from firebase since local last update
        firebaseModel.getAllPosts() { list ->
            executor.execute {
//                Log.d("TAG", " firebase return : " + list.size())
                var time = localLastUpdate
                    for (post in list!!) {
                        // insert new records into ROOM
                            if (post!!.deleted == true) {
                                localDb.PostDao().delete(post)
                            } else {
                                localDb.PostDao().insertAll(post)
                            }


                                if (time!! < (post.lastUpdated)) {
                                    time = post.lastUpdated
                                }


                }
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                // update local last update
                Post.localLastUpdate = time
                EventPostsListLoadingState.postValue(PostsListLoadingState.NOT_LOADING)
            }
        }
    }

//    fun addMaslul(maslul: Maslul, listener: Listener<Void?>) {
//        maslul.setUserId(userEmail)
//        firebaseModel.saveMaslul(maslul) { Void ->
//            refreshAllMaslulim()
//            listener.onComplete(null)
//        }
//    }

    fun getAllPosts(): LiveData<List<Post>> {
        return allPosts?:localDb.PostDao().getAll()
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

    fun createPost(post: Post, img:Bitmap?, callback: EmptyCallback) {
        img?.let {
            cloudinaryModel.uploadBitmap(it) { url ->
                if (!url.isNullOrEmpty()) {
                    post.imageUrl = url
                    firebaseModel.createPost(post, callback)
                }
            }
        } ?: callback()
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