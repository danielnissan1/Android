package com.example.yadshniya

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.ColorSpace.Model
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.User
import com.example.yadshniya.databinding.ActivityRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.security.AccessController.getContext


class RegisterActivity : AppCompatActivity() {
    private val auth = Firebase.auth
//    private var binding = ActivityRegisterBinding.inflate(layoutInflater)
private lateinit var binding: ActivityRegisterBinding

    private lateinit var pickProfileImageButton: ImageButton
    private lateinit var imageSelectionCallBack: ActivityResultLauncher<Intent>
    private var imageURI: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("creation", "creating Login screen")
        binding = ActivityRegisterBinding.inflate(layoutInflater) // Initialize here!

        setContentView(R.layout.activity_register)
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
        signUpButton.setOnClickListener() {
            Log.i("buttonClick", "sign up button in register screen clicked")
            register()
        }
    }

    //Authentication
//    private fun onSignUp() {
//        val usernameInput = findViewById<TextView>(R.id.usernameTextfield)
//        val username = usernameInput.text.toString().trim()
//
//        val passwordInput = findViewById<TextView>(R.id.passwordTextfield)
//        val password = passwordInput.text.toString().trim()
//
//        auth.createUserWithEmailAndPassword(username, password).addOnSuccessListener {
//            val authenticatedUser = it.user!!
//
////            val profileChange = UserProfileChangeRequest.Builder().setPhotoUri(imageURI)
////                .setDisplayName("$firstNameValue $lastNameValue").build()
//
//
////            authenticatedUser.updateProfile(profileChange)
//
////            UserModel.instance.addUser(
////                User(authenticatedUser.uid, firstNameValue, lastNameValue), imageURI!!
////            ) {
////                Toast.makeText(
////                    this@RegisterActivity, "Register Successful", Toast.LENGTH_SHORT
////                ).show()
////                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
////                startActivity(intent)
////                finish()
////            }
//        }.addOnFailureListener {
//            Toast.makeText(
//                this@RegisterActivity, "Register Failed, " + it.message, Toast.LENGTH_SHORT
//            ).show()
//        }
//
//    }


    //Functions that related to image profile upload
    @SuppressLint("InlinedApi")
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

    private fun register() {
        val name: String = binding.nameTextfield.text.toString()
        val email: String = binding.usernameTextfield.text.toString()
        val password: String = binding.passwordTextfield.text.toString()

        Log.i("register", "registering user with email: $email, name: $name, password: $password")
        if (email != "" && password != "" && name != "") {

            instance().register(email, password)
            { user ->
                if (user != null) {
                    val newUser: User = User(name, email, "")

//                    if (isAvatarSelected) {
                    val imageView = binding.profilePicButtonSignUpScreen

                    val drawable = imageView.drawable
                    if (drawable is BitmapDrawable) {
                        val imageBitmap = drawable.bitmap

                        // Add to storage account and save url
                        instance()
                            .uploadImage(email, imageBitmap) { url ->
                                if (url != null) {
                                    newUser.imageUrl = url.toString()
                                }
                                instance().createUser(newUser) { user ->
//                                    TODO: toMainScreen()
//                                    toMainScreen()
                                }
                            }
                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
//                    } else {
//                        // Save without img
//                        Model.instance().addUser(newUser) { usr ->
//                            binding.registerProgressbar.setVisibility(View.GONE)
//                            toMainScreen()
//                        }
//                    }
                } else {
                    Toast.makeText(
                        this, "Register failed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } else {
            Toast.makeText(
                this, "All the fields are required",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
