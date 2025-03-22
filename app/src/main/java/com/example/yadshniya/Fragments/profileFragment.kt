package com.example.yadshniya.Fragments

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yadshniya.LoginActivity
import com.example.yadshniya.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.io.File

class ProfileFragment : Fragment() {
    private lateinit var root: View
    private var isEditing = false
    private var auth = Firebase.auth

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
            } else {
                editButton.setImageResource(R.drawable.baseline_done_24)

                editUsername.setText(username.text)
                username.visibility = View.GONE
                editUsername.visibility = View.VISIBLE

                editUsername.textSize = 14f

                editUsername.requestFocus()
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
        //TODO: updates the details in the firebase but not the login
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(currentUser.email!!)

            // Create a map with the expected types
            val updatedData: Map<String, Any> = hashMapOf(
                "userName" to username,
            )

            // Update Firestore document
            userRef.update(updatedData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                    Log.d("ProfileFragment", "User details updated successfully in Firestore.")
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                    Log.e("ProfileFragment", "Error updating user details in Firestore", e)
                }
        } else {
            Toast.makeText(requireContext(), "No user is logged in", Toast.LENGTH_SHORT).show()
        }

    }
}
