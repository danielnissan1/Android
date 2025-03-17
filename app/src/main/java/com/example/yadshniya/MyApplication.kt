package com.example.yadshniya

import android.app.Application
import android.content.Context


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context

//        fun getContext(): Context {
//            return context
//        }
    }
}
