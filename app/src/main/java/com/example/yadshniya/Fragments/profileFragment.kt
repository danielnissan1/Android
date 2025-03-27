package com.example.yadshniya.Fragments

import PostAdapter
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
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
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.LoginActivity
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.Post
import com.example.yadshniya.Model.User
import com.example.yadshniya.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private lateinit var root: View
    private var isEditing = false
    private var auth = Firebase.auth

    private lateinit var pickProfileImageButton: ImageButton
    private var imageURI: Uri? = null
    private lateinit var imageSelectionCallBack: ActivityResultLauncher<Intent>

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()
    private val currentId: String?
        get() = auth.currentUser?.uid

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

        // Initialize RecyclerView
        recyclerView = requireView().findViewById(R.id.recycler_view_user_posts)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList, true)
        recyclerView.adapter = postAdapter

        currentId?.let { userId ->
            instance().getCurrentUserPosts(userId).observe(viewLifecycleOwner, Observer { posts ->
                Log.d("profileFragment", "Fetched ${posts.size} posts")

                if (posts.isEmpty()) {
                    Log.e("profileFragment", "No posts fetched. Check Firestore query or database.")
                }

                postList.clear()
                postList.addAll(posts)
                postAdapter.notifyDataSetChanged()
            })
        } ?: Log.e("profileFragment", "User ID is null, cannot fetch user posts")

        instance().EventPostsListLoadingState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("profileFragment", "Loading State: $state")
        })

        reloadData()
        observeData()
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

    @SuppressLint("SuspiciousIndentation")
    private fun updateUserInFirestore(username: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val email = currentUser.email!!
            val userId = currentUser.uid

            val drawable = pickProfileImageButton.drawable
            val user = User(id = userId, email = email, userName = username) // Create a new user object

            if (drawable is BitmapDrawable) {
                val imageBitmap = drawable.bitmap
                instance().updateUser(user, img = imageBitmap) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            } else {
                instance().updateUser(user, img = null) {
                    Toast.makeText(requireContext(), "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun defineImageSelectionCallBack() {
        imageSelectionCallBack =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                try {
                    val imageUri: Uri? = result.data?.data
                    Log.d("ImageSelection", "Image URI: $imageUri")

                    if (imageUri != null) {
                        val bitmap = MediaStore.Images.Media.getBitmap(requireContext().contentResolver, imageUri)
                        val imageSizeInBytes = bitmap.byteCount.toLong() // Size in bytes
                        val maxCanvasSize = 5 * 1024 * 1024 // 5MB

                        if (imageSizeInBytes > maxCanvasSize) {
                            Log.e("ImageSelection", "Selected image is too large")
                            Toast.makeText(requireContext(), "Selected image is too large", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.d("ImageSelection", "Setting image to button")
                            pickProfileImageButton.setImageBitmap(bitmap)
                            imageURI = imageUri  // Store the URI if you need it
                            Log.d("ImageSelection", "Image successfully set")
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

    private fun reloadData() {
        Log.d("profileFragment", "Reloading data...")
        currentId?.let { userId ->
            instance().refreshUsersPosts(userId)
        } ?: Log.e("profileFragment", "User ID is null, cannot reload data")
    }

    private fun observeData() {
        currentId?.let { userId ->
            instance().getCurrentUserPosts(userId).observe(viewLifecycleOwner, Observer { posts ->
                Log.d("profileFragment", "Fetched ${posts.size} posts")

                if (posts.isNotEmpty()) {
                    postList.clear()
                    postList.addAll(posts)
                    postAdapter.notifyDataSetChanged()
                    Log.d("profileFragment", "Adapter notified with new data")
                } else {
                    Log.e("profileFragment", "No posts to display")
                }
            })
        } ?: Log.e("profileFragment", "User ID is null, cannot")
    }
}
