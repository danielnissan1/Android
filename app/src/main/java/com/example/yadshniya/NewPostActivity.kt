package com.example.yadshniya

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_new_post)

        val descriptionEditText = findViewById<EditText>(R.id.ed1)
        val pickPriceButton = findViewById<Button>(R.id.pickPrice)

        // Initially, set the Pick Price button visibility to GONE
        pickPriceButton.visibility = View.GONE

        // Add TextWatcher to the description EditText
        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Check if description is not empty
                if (s != null && s.isNotEmpty()) {
                    pickPriceButton.visibility = View.VISIBLE // Show the button
                } else {
                    pickPriceButton.visibility = View.GONE // Hide the button
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
