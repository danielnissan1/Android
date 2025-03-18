package com.example.yadshniya

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "creating profile screen")
        setContentView(R.layout.activity_profile)
    }
}