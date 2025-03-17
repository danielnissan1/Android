package com.example.yadshniya.Model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface MaslulDao {
    @get:Query("select * from Post")
    val all: LiveData<List<Any?>?>?

    @Query("select * from Post where userId = :userId")
    fun getMyMaslulim(userId: String?): LiveData<List<Post?>?>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg maslulim: Post?)

    @Delete
    fun delete(maslul: Post?)
}
