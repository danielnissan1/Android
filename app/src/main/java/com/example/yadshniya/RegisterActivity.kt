package com.example.yadshniya

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
                    Log.d("ImageSelection", "Image URI: $imageUri") // Debugging log

                    if (imageUri != null) {
                        val imageSize = getImageSize(imageUri)
                        val maxCanvasSize = 5 * 1024 * 1024 // 5MB
                        if (imageSize > maxCanvasSize) {
                            Log.e("ImageSelection", "Selected image is too large")
                            Toast.makeText(
                                this@RegisterActivity,
                                "Selected image is too large",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.d("ImageSelection", "Setting image to button")
                            pickProfileImageButton.setImageURI(imageUri)
                            imageURI = imageUri
                            Log.d("ImageSelection", "Image successfully set")
                        }
                    } else {
                        Log.e("ImageSelection", "No image selected")
                        Toast.makeText(this@RegisterActivity, "No Image Selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (e: Exception) {
                    Log.e("ImageSelection", "Error processing image: ${e.message}")
                    Toast.makeText(
                        this@RegisterActivity, "Error processing result", Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }



    private fun register() {
        val name: String = findViewById<EditText>(R.id.nameTextfield).text.toString()
        val email: String = findViewById<EditText>(R.id.usernameTextfield).text.toString()
        val password: String = findViewById<EditText>(R.id.passwordTextfield).text.toString()

        Log.i("register", "registering user with email: $email, name: $name, password: $password")
        if (email != "" && password != "" && name != "") {

            instance().register(email, password)
            { user ->
                if (user != null) {
                    val newUser = User(userName = name, email = email)

                    if (pickProfileImageButton.drawable != null) {
                    val imageView = findViewById<ImageButton>(R.id.profilePicButtonSignUpScreen)
//                    val imageView = binding.profilePicButtonSignUpScreen

                        Log.d("RegisterActivity", "Image view: $imageView")

                    val drawable = imageView.drawable
                    if (drawable is BitmapDrawable) {
                        val imageBitmap = drawable.bitmap
Log.d("RegisterActivity", "Image bitmap: $imageBitmap")
                        // Add to storage account and save url
//                        instance()
//                            .uploadImage(email, imageBitmap) { url ->
//                                if (url != null) {
//                                    newUser.imageUrl = url.toString()
//                                }
                                instance().createUser(
                                    newUser,
                                    img = imageBitmap
                                ) { toMainScreen() }

                    } else {
                        Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
                    }
//                    } else {
//                        // Save without img
//                        instance().createUser(
//                            newUser,
//                            img = ""
//                        ) { toMainScreen() }
                    }
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

    private fun toMainScreen() {

//        val intent = Intent(this@feedFragmentDirections, RegisterActivity::class.java)
//        startActivity(intent)

//        val intent: Intent = Intent(
//            getContext(),
//            MainActivity::class.java
//        )
//        startActivity(intent)
//        getActivity().finish()
    }

}
