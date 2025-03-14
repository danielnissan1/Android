package com.example.yadshniya

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class LoginActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "creating Login screen")
        setContentView(R.layout.login_screen)

        setUI()
    }

    private fun setUI() {
        val signUpButton = findViewById<Button>(R.id.toSignUp)
        val loginButton = findViewById<Button>(R.id.btn_login)

        signUpButton.setOnClickListener {
            Log.i("buttonClick", "signup button in Login screen clicked")
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}