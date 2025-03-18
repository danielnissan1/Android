package com.example.yadshniya

import android.R
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "creating profile screen")
        setContentView(com.example.yadshniya.R.layout.activity_profile)
        setUI()
    }

    private fun setUI() {
        val exitButton = findViewById<ImageButton>(com.example.yadshniya.R.id.btn_exit_user)
        val editButton = findViewById<ImageButton>(com.example.yadshniya.R.id.btn_edit_user)
        val username = findViewById<TextView>(com.example.yadshniya.R.id.profile_username)
        val email =  findViewById<TextView>(com.example.yadshniya.R.id.profile_email)

        val editUsername = EditText(this)
        val editEmail = EditText(this)

        editUsername.layoutParams = username.layoutParams
        editUsername.setText(username.text)
        editUsername.textSize = username.textSize
        editUsername.visibility = View.GONE

        editEmail.layoutParams = email.layoutParams
        editEmail.setText(email.text)
        editEmail.textSize = email.textSize
        editEmail.visibility = View.GONE

        (username.parent as ViewGroup).addView(editUsername)
        (email.parent as ViewGroup).addView(editEmail)

        Log.d("hi", editButton.toString())
        editButton.setOnClickListener {
            Log.d("DEBUG", "Edit button clicked!")
            username.visibility = View.GONE
            email.visibility = View.GONE
            editUsername.visibility = View.VISIBLE
            editEmail.visibility = View.VISIBLE
            editUsername.requestFocus() // Focus on username
        }

        exitButton.setOnClickListener {
            username.text = editUsername.text.toString()
            email.text = editEmail.text.toString()
            username.visibility = View.VISIBLE
            email.visibility = View.VISIBLE
            editUsername.visibility = View.GONE
            editEmail.visibility = View.GONE
        }
    }

}