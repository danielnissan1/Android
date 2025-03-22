package com.example.yadshniya.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.yadshniya.LoginActivity
import com.example.yadshniya.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {
    private lateinit var root: View
    private var isEditing = false
    private var auth = Firebase.auth

    private lateinit var pickProfileImageButton: ImageButton
    private lateinit var imageSelectionCallBack: ActivityResultLauncher<Intent>
    private var imageURI: Uri? = null
    private var selectedBitmap: Bitmap? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.activity_profile, container, false)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("ProfileFragment", "Profile screen loaded")
        setUserDetails()
        setUI()
    }

        private fun setUserDetails() {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val db = FirebaseFirestore.getInstance()
                val userRef = db.collection("users").document(currentUser.email!!)

                userRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userName = task.result?.getString("userName")
                        val email = currentUser.email
                        val profileImageUrl = task.result?.getString("imageUrl")

                        val profileUsername = root.findViewById<TextView>(R.id.profile_username)
                        val profileEmail = root.findViewById<TextView>(R.id.profile_email)

                        if (profileImageUrl != null) {
                            val profileImageView = root.findViewById<ImageView>(R.id.profileImage)
                            Picasso.get()
                                .load(profileImageUrl)
                                .placeholder(R.drawable.profile)
                                .into(profileImageView)
                        }

                        profileUsername.text = userName ?: "No Name"
                        profileEmail.text = email ?: "No Email"
                    } else {
                        Toast.makeText(requireContext(), "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "No user is logged in", Toast.LENGTH_SHORT).show()
            }
        }


        private fun setUI() {
        val exitButton = root.findViewById<ImageButton>(R.id.btn_exit_user)
        val editButton = root.findViewById<ImageButton>(R.id.btn_edit_user)
        val username = root.findViewById<TextView>(R.id.profile_username)
        pickProfileImageButton = root.findViewById<ImageButton>(R.id.profileImage)

        defineImageSelectionCallBack()
        pickProfileImageButton.setOnClickListener {
            Log.i("buttonClick", "pick profile pick button in profile screen clicked")
            openGallery()
        }
        pickProfileImageButton.isEnabled = false

        val editUsername = EditText(requireContext())

        editUsername.layoutParams = username.layoutParams
        editUsername.setText(username.text)
        editUsername.textSize = username.textSize
        editUsername.visibility = View.GONE

        (username.parent as ViewGroup).addView(editUsername)

        // Edit profile logic
        editButton.setOnClickListener {
            Log.d("ProfileFragment", "Edit button clicked! isEditing: $isEditing")
            if (isEditing) {
                editButton.setImageResource(R.drawable.baseline_edit_24)

                // Save changes & switch to View Mode
                username.text = editUsername.text.toString()
                username.visibility = View.VISIBLE
                editUsername.visibility = View.GONE

                updateUserInFirestore(editUsername.text.toString())
                pickProfileImageButton.isEnabled = false
            } else {
                editButton.setImageResource(R.drawable.baseline_done_24)

                editUsername.setText(username.text)
                username.visibility = View.GONE
                editUsername.visibility = View.VISIBLE

                editUsername.textSize = 14f
                editUsername.requestFocus()

                pickProfileImageButton.isEnabled = true
                pickProfileImageButton.alpha = 1.0f
            }
            isEditing = !isEditing // Toggle state
        }

        // Exit Button Click Listener
        exitButton.setOnClickListener {
            Log.d("ProfileFragment", "Exit button clicked! Exiting edit mode.")
            auth.signOut()
            Toast.makeText(
                requireContext(), "Logged out", Toast.LENGTH_SHORT
            ).show()

            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            activity?.finish()
        }
    }

    private fun updateUserInFirestore(username: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUser.email!!)

            Log.d("profile", "selected image: $imageURI")
            if (selectedBitmap != null) {
                uploadImageToFirebaseStorage(selectedBitmap!!) { imageUrl ->
                    val updatedData: Map<String, Any> = hashMapOf(
                        "userName" to username,
                        "imageUrl" to imageUrl // Save image URL in Firestore
                    )
                    userRef.update(updatedData)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                            Log.d("ProfileFragment", "User details updated successfully in Firestore.")
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                            Log.e("ProfileFragment", "Error updating user details in Firestore", e)
                        }
                }
            } else {
                // Update only username if no new image is selected
                val updatedData: Map<String, Any> = hashMapOf("userName" to username)
                userRef.update(updatedData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                        Log.d("ProfileFragment", "User details updated successfully in Firestore.")
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                        Log.e("ProfileFragment", "Error updating user details in Firestore", e)
                    }
            }
        } else {
            Toast.makeText(requireContext(), "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebaseStorage(bitmap: Bitmap, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val userId = FirebaseAuth.getInstance().currentUser?.uid

        if (userId == null) {
            Log.e("FirebaseStorage", "User not authenticated")
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_LONG).show()
            return
        }

        val fileRef = storageRef.child("profile_images/$userId.jpg")

        // Convert Bitmap to ByteArray
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos) // Compress to reduce size
        val imageData = baos.toByteArray()

        // Upload to Firebase Storage using putBytes()
        fileRef.putBytes(imageData)
            .addOnSuccessListener {
                fileRef.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("FirebaseStorage", "Image uploaded successfully: $uri")
                    onSuccess(uri.toString()) // Return download URL
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseStorage", "Image upload failed: ${e.message}")
                Toast.makeText(requireContext(), "Image upload failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun defineImageSelectionCallBack() {
        imageSelectionCallBack =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                try {
                    val imageUri: Uri? = result.data?.data
                    Log.d("ImageSelection", "Image URI: $imageUri")

                    if (imageUri != null) {
                        // Convert Uri to Bitmap
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)

                        // Check file size
                        val imageSize = getBitmapSize(bitmap)
                        val maxCanvasSize = 5 * 1024 * 1024 // 5MB
                        if (imageSize > maxCanvasSize) {
                            Log.e("ImageSelection", "Selected image is too large")
                            Toast.makeText(requireContext(), "Selected image is too large", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("ImageSelection", "Setting image to button")
                            pickProfileImageButton.setImageBitmap(bitmap)
                            selectedBitmap = bitmap // Store the bitmap for later upload
                        }
                    } else {
                        Log.e("ImageSelection", "No image selected")
                        Toast.makeText(requireContext(), "No Image Selected", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("ImageSelection", "Error processing image: ${e.message}")
                    Toast.makeText(requireContext(), "Error processing result", Toast.LENGTH_SHORT).show()
                }
            }
    }


    private fun openGallery() {
        val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
        imageSelectionCallBack.launch(intent)
    }

    private fun getBitmapSize(bitmap: Bitmap): Int {
        return bitmap.byteCount
    }

}
