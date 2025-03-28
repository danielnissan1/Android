package com.example.yadshniya.Model

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query


@Dao
interface PostDao {
    @Query("select * from Post order by lastUpdated desc")
    fun getAll(): LiveData<List<Post>>

    @Query("select * from Post where userId = :userId order by lastUpdated desc")
    fun getPostsByUser(userId: String?): LiveData<List<Post>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg posts: Post?)

    @Delete
    fun delete(post: Post)

    @Query("DELETE FROM Post")
    fun deleteAll()
}