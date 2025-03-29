package com.example.yadshniya

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        Log.d("dewsa", "MyApplication initialized!")

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

    }
}
