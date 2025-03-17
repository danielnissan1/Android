package com.example.yadshniya

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth


class RegisterActivity : AppCompatActivity() {
    private val auth = Firebase.auth

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

    private fun setUi() {
        val signUpButton = findViewById<Button>(R.id.registerButton)
        val gotoLoginButton = findViewById<Button>(R.id.gotoLogin)
        pickProfileImageButton = findViewById(R.id.profilePicButtonSignUpScreen)

        defineImageSelectionCallBack()
        pickProfileImageButton.setOnClickListener {
            Log.i("buttonClick", "pick profile pick button in signup screen clicked")
            openGallery()
        }

        gotoLoginButton.setOnClickListener {
            Log.i("buttonClick", "login button in register screen clicked")
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    //Authentication
    private fun onSignUp() {
        val usernameInput = findViewById<TextView>(R.id.usernameTextfield)
        val username = usernameInput.text.toString().trim()

        val passwordInput = findViewById<TextView>(R.id.passwordTextfield)
        val password = passwordInput.text.toString().trim()

        auth.createUserWithEmailAndPassword(username, password).addOnSuccessListener {
            val authenticatedUser = it.user!!

//            val profileChange = UserProfileChangeRequest.Builder().setPhotoUri(imageURI)
//                .setDisplayName("$firstNameValue $lastNameValue").build()

//            authenticatedUser.updateProfile(profileChange)

//            UserModel.instance.addUser(
//                User(authenticatedUser.uid, firstNameValue, lastNameValue), imageURI!!
//            ) {
//                Toast.makeText(
//                    this@RegisterActivity, "Register Successful", Toast.LENGTH_SHORT
//                ).show()
//                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
//                startActivity(intent)
//                finish()
//            }
        }.addOnFailureListener {
            Toast.makeText(
                this@RegisterActivity, "Register Failed, " + it.message, Toast.LENGTH_SHORT
            ).show()
        }

    }


    //Functions that related to image profile upload
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
}
