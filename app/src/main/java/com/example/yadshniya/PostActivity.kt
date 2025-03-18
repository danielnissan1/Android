package com.example.yadshniya

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PostActivity : AppCompatActivity() {
    private var isEditing = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "creating profile screen")
        setContentView(com.example.yadshniya.R.layout.post)
        setUI()
    }

    private fun setUI() {
        val deleteButton = findViewById<ImageButton>(com.example.yadshniya.R.id.btn_delete_post)
        val editButton = findViewById<ImageButton>(com.example.yadshniya.R.id.btn_edit_post)
        val description = findViewById<TextView>(com.example.yadshniya.R.id.item_description)
        val price = findViewById<TextView>(com.example.yadshniya.R.id.item_price)

        val editDescription = EditText(this)
        val editPrice = EditText(this)

        editDescription.layoutParams = description.layoutParams
        editDescription.setText(description.text)
        editDescription.textSize = description.textSize
        editDescription.visibility = View.GONE

        editPrice.layoutParams = price.layoutParams
        editPrice.setText(price.text)
        editPrice.textSize = price.textSize
        editPrice.visibility = View.GONE

        (description.parent as ViewGroup).addView(editDescription)
        (price.parent as ViewGroup).addView(editPrice)

        //TODO: edit the image as well and send it to firebase
        editButton.setOnClickListener {
            Log.d("DEBUG", "Edit button clicked! isEditing: $isEditing")
            if (isEditing) {
                // Save changes & switch to View Mode
                description.text = editDescription.text.toString()
                price.text = editPrice.text.toString()
                description.visibility = View.VISIBLE
                price.visibility = View.VISIBLE
                editDescription.visibility = View.GONE
                editPrice.visibility = View.GONE
            } else {
                // Switch to Edit Mode
                editDescription.setText(description.text)
                editPrice.setText(price.text)
                description.visibility = View.GONE
                price.visibility = View.GONE
                editDescription.visibility = View.VISIBLE
                editPrice.visibility = View.VISIBLE
                editDescription.requestFocus()
            }
            isEditing = !isEditing // Toggle state
        }
    }
}