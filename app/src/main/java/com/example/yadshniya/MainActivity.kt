package com.example.yadshniya

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate() called")

//        enableEdgeToEdge()
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.bottom_navigation)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
        setContentView(R.layout.main_screen)
        setUI()
    }

    fun setUI() {
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_navigation)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        NavigationUI.setupWithNavController(bottomNavigationView, navController)

    //        val navHostFragment =
//            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
//        val navController = navHostFragment.navController
//
//        NavigationUI.setupWithNavController(bottomNavigationView, navController)
    }
}