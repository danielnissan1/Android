package com.example.yadshniya

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class RegisterActivity : AppCompatActivity()  {
    private lateinit var pickProfileImageButton: ImageButton

    //Selecting image callback
    private lateinit var imageSelectionCallBack: ActivityResultLauncher<Intent>
    private var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "creating Login screen")
        setContentView(R.layout.register_screen)
        setUi()
    }

    private fun openGallery() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        imageSelectionCallBack.launch(intent)
    }

    private fun getImageSize(uri: Uri?): Long {
        val inputStream = contentResolver.openInputStream(uri!!)
        return inputStream?.available()?.toLong() ?: 0
    }

    private fun defineImageSelectionCallBack() {
         imageSelectionCallBack =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                try {
                    val imageUri: Uri? = result.data?.data
                    if (imageUri != null) {
                        val imageSize = getImageSize(imageUri)
                        val maxCanvasSize = 5 * 1024 * 1024 // 5MB
                        if (imageSize > maxCanvasSize) {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Selected image is too large",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            pickProfileImageButton.setImageURI(imageUri)
                            imageURI = imageUri
                        }

                    } else {
                        Toast.makeText(this@RegisterActivity, "No Image Selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@RegisterActivity, "Error processing result", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun setUi (){
        var signUpButton = findViewById<Button>(R.id.registerButton)
        var loginButton = findViewById<Button>(R.id.gotoLogin)
        pickProfileImageButton = findViewById(R.id.profilePicButtonSignUpScreen)

        defineImageSelectionCallBack()
        pickProfileImageButton.setOnClickListener {
            Log.i("buttonClick", "pick profile pick button in signup screen clicked")
            openGallery()
        }
    }
}