package com.example.yadshniya.Model


import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.yadshniya.MyApplication

@Database(entities = [Post::class], version = 3)
//@TypeConverters([Converters::class])
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun PostDao(): PostDao?
}

object AppLocalDb {
    val appDb: AppLocalDbRepository
        get() = Room.databaseBuilder(
            MyApplication.context,
            AppLocalDbRepository::class.java,
            "dbFileName.db"
        )
            .fallbackToDestructiveMigration()
            .build()
}
