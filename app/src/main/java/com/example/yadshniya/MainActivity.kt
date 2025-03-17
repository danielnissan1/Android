package com.example.yadshniya

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        setUI()
        setContentView(R.layout.login_screen)
        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
            "name" to "John Doe",
            "email" to "johndoe@example.com",
            "age" to 25
        )

        db.collection("users").document("user1")
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "User added successfully!")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding user", e)
            }
    }

    fun setUI() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
//        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}