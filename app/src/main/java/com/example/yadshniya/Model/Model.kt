package com.example.yadshniya.Model

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.os.HandlerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import java.util.concurrent.Executor
import java.util.concurrent.Executors

class Model private constructor() {
    enum class MaslulimListLoadingState {
        LOADING,
        NOT_LOADING
    }

    private val firebaseModel: FirebaseModel = FirebaseModel()
    var executor: Executor = Executors.newFixedThreadPool(1)
    var mainThread: Handler = HandlerCompat.createAsync(Looper.getMainLooper())
    var localDb: AppLocalDbRepository = AppLocalDb.getAppDb()

    val EventMaslulimListLoadingState: MutableLiveData<MaslulimListLoadingState?> =
        MutableLiveData(MaslulimListLoadingState.NOT_LOADING)

    interface Listener<T> {
        fun onComplete(data: T)
    }

    private var allMaslulimList: LiveData<List<Maslul?>>? = null

    private var myMaslulimList: LiveData<List<Maslul>>? = null

    val allMaslulim: LiveData<List<Any?>>
        get() {
            if (allMaslulimList == null) {
                allMaslulimList = localDb.maslulDao().getAll()
                refreshAllMaslulim()
            }
            return allMaslulimList
        }

    val myMaslulim: LiveData<List<Any>>
        get() {
            if (myMaslulimList == null) {
                myMaslulimList = localDb.maslulDao().getMyMaslulim(userEmail)
                refreshAllMaslulim()
            }
            return myMaslulimList
        }

    fun refreshAllMaslulim() {
        EventMaslulimListLoadingState.value = MaslulimListLoadingState.LOADING

        // get local last update
        val localLastUpdate: Long = Maslul.getLocalLastUpdate()

        // get all updated records from firebase since local last update
        firebaseModel.getAllMaslulimSince(localLastUpdate) { list ->
            executor.execute {
                Log.d("TAG", " firebase return : " + list.size())
                var time = localLastUpdate
                for (maslul in list) {
                    // insert new records into ROOM
                    if (maslul.getDeleted()) {
                        localDb.maslulDao().delete(maslul)
                    } else {
                        localDb.maslulDao().insertAll(maslul)
                    }

                    if (time < maslul.getLastUpdated()) {
                        time = maslul.getLastUpdated()
                    }
                }
                try {
                    Thread.sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                // update local last update
                Maslul.setLocalLastUpdate(time)
                EventMaslulimListLoadingState.postValue(MaslulimListLoadingState.NOT_LOADING)
            }
        }
    }

    fun addMaslul(maslul: Maslul, listener: Listener<Void?>) {
        maslul.setUserId(userEmail)
        firebaseModel.saveMaslul(maslul) { Void ->
            refreshAllMaslulim()
            listener.onComplete(null)
        }
    }

    fun register(email: String?, password: String?, listener: Listener<FirebaseUser?>?) {
        firebaseModel.register(email, password, listener)
    }

    fun login(email: String?, password: String?, listener: Listener<FirebaseUser?>?) {
        firebaseModel.login(email, password, listener)
    }

    val isSignedIn: Boolean
        get() = firebaseModel.isSignedIn()

    fun signOut() {
        EventMaslulimListLoadingState.postValue(null)
        firebaseModel.signOut()
    }

    fun addUser(user: User?, listener: Listener<User?>?) {
        firebaseModel.addUser(user, listener)
    }

    fun getUserById(email: String?, listener: Listener<User?>?) {
        firebaseModel.getUserById(email, listener)
    }

    val userEmail: String
        get() = firebaseModel.getUserEmail()

    fun uploadImage(name: String?, bitmap: Bitmap?, listener: Listener<String?>?) {
        firebaseModel.uploadImage(name, bitmap, listener)
    }

    fun getMaslulById(maslulId: String?, listener: Listener<Maslul?>) {
        val maslulimList: List<Maslul?> = allMaslulimList!!.value!!
        val maslul: Maslul? = maslulimList.stream().filter { ms: Maslul? ->
            ms.getId().equals
            (maslulId)
        }.findFirst().orElse(null)
        listener.onComplete(maslul)
    }

    companion object {
        val areas: Array<String> = arrayOf(
            "Where am I form",
            "North",
            "Center",
            "South"
        )

        private val instance = Model()
        fun instance(): Model {
            return instance
        }
    }
}
