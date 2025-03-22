package com.example.yadshniya

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity: AppCompatActivity() {
    private var auth = Firebase.auth

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

        loginButton.setOnClickListener {
            Log.i("buttonClick", "signin button in signin screen clicked")
            checkLoginUser()
        }
    }

    private fun checkLoginUser() {
        val email = findViewById<EditText>(R.id.email_login)
        val emailValue: String = email.text.toString().trim()

        val password = findViewById<EditText>(R.id.password_login)
        val passwordValue: String = password.text.toString().trim()

        val checkUserValidation = loginUserValidation(emailValue, passwordValue)

        if (checkUserValidation) {
            Log.i("buttonClick", "signIn button in login screen clicked")
            Log.i("login", "email input is:" + emailValue)
            Log.i("login", "password Input is:" + passwordValue)

            auth.signInWithEmailAndPassword(emailValue, passwordValue).addOnSuccessListener {
                loggedInHandler()
            }.addOnFailureListener {
                Toast.makeText(
                    this@LoginActivity, "Your Email or Password is incorrect!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loginUserValidation(
        email: String, password: String
    ): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun loggedInHandler() {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").document(auth.currentUser?.email!!)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userName = task.result?.getString("userName")
                Toast.makeText(
                    this@LoginActivity, "Welcome ${userName}!", Toast.LENGTH_SHORT
                ).show()
                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}