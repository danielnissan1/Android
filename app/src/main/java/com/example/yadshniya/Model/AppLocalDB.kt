package com.example.yadshniya.Model


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.yadshniya.MyApplication

@Database(entities = [Post::class], version = 4)
abstract class AppLocalDbRepository : RoomDatabase() {
    abstract fun PostDao(): PostDao


    companion object AppLocalDb {
        @Volatile
        private var INSTANCE: AppLocalDbRepository? = null

        fun getDatabase(context: Context): AppLocalDbRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppLocalDbRepository::class.java,
                    "dbFileName.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}