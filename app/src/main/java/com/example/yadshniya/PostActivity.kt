package com.example.yadshniya

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class PostActivity : AppCompatActivity() {
    private var isEditing = false // Track edit mode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "Creating post screen")
        setContentView(R.layout.post)
        setUI()
    }

    //TODO: edit image as well and send it to firebase
    private fun setUI() {
        val editButton = findViewById<ImageButton>(R.id.btn_edit_post)
        val description = findViewById<TextView>(R.id.item_description)
        val price = findViewById<TextView>(R.id.item_price)
        val postImage = findViewById<ImageView>(R.id.post_image) // Get the image for constraint

        val editDescription = EditText(this)
        val editPrice = EditText(this)

        // Copy TextView styles to EditText
        editDescription.setText(description.text)
        editDescription.textSize = description.textSize / resources.displayMetrics.scaledDensity
        editDescription.setTextColor(description.currentTextColor)
        editDescription.setTypeface(description.typeface)
        editDescription.visibility = View.GONE
        editDescription.id = View.generateViewId() // Generate unique ID for constraint

        editPrice.setText(price.text)
        editPrice.textSize = price.textSize / resources.displayMetrics.scaledDensity
        editPrice.setTextColor(price.currentTextColor)
        editPrice.setTypeface(price.typeface, Typeface.BOLD)
        editPrice.visibility = View.GONE
        editPrice.id = View.generateViewId()

        // Apply proper ConstraintLayout parameters
        val layoutParamsDesc = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = postImage.id // Make sure it's under the image
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(0, 8, 0, 0)
        }

        val layoutParamsPrice = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topToBottom = editDescription.id // Place below editDescription
            startToStart = ConstraintLayout.LayoutParams.PARENT_ID
            setMargins(0, 8, 0, 0)
        }

        editDescription.layoutParams = layoutParamsDesc
        editPrice.layoutParams = layoutParamsPrice

        val parentLayout = description.parent as ConstraintLayout
        parentLayout.addView(editDescription)
        parentLayout.addView(editPrice)

        editButton.setOnClickListener {
            Log.d("DEBUG", "Edit button clicked! isEditing: $isEditing")

            if (isEditing) {
                description.text = editDescription.text.toString()
                price.text = editPrice.text.toString()
                description.visibility = View.VISIBLE
                price.visibility = View.VISIBLE
                editDescription.visibility = View.GONE
                editPrice.visibility = View.GONE
            } else {
                editDescription.setText(description.text.toString())
                editPrice.setText(price.text.toString())
                description.visibility = View.GONE
                price.visibility = View.GONE
                editDescription.visibility = View.VISIBLE
                editPrice.visibility = View.VISIBLE
                editDescription.requestFocus()
            }

            isEditing = !isEditing
        }
    }
}