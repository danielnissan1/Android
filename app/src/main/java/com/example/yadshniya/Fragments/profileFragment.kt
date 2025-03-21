package com.example.yadshniya.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.yadshniya.LoginActivity
import com.example.yadshniya.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

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
        setUI()
    }

    private fun setUI() {
        val exitButton = root.findViewById<ImageButton>(R.id.btn_exit_user)
        val editButton = root.findViewById<ImageButton>(R.id.btn_edit_user)
        val username = root.findViewById<TextView>(R.id.profile_username)
        val email = root.findViewById<TextView>(R.id.profile_email)

        val editUsername = EditText(requireContext())
        val editEmail = EditText(requireContext())

        editUsername.layoutParams = username.layoutParams
        editUsername.setText(username.text)
        editUsername.textSize = username.textSize
        editUsername.visibility = View.GONE

        editEmail.layoutParams = email.layoutParams
        editEmail.setText(email.text)
        editEmail.textSize = email.textSize
        editEmail.visibility = View.GONE

        (username.parent as ViewGroup).addView(editUsername)
        (email.parent as ViewGroup).addView(editEmail)

        // Edit profile logic
        editButton.setOnClickListener {
            Log.d("ProfileFragment", "Edit button clicked! isEditing: $isEditing")
            if (isEditing) {
                // Save changes & switch to View Mode
                username.text = editUsername.text.toString()
                email.text = editEmail.text.toString()
                username.visibility = View.VISIBLE
                email.visibility = View.VISIBLE
                editUsername.visibility = View.GONE
                editEmail.visibility = View.GONE
            } else {
                // Switch to Edit Mode
                editUsername.setText(username.text)
                editEmail.setText(email.text)
                username.visibility = View.GONE
                email.visibility = View.GONE
                editUsername.visibility = View.VISIBLE
                editEmail.visibility = View.VISIBLE
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
}
