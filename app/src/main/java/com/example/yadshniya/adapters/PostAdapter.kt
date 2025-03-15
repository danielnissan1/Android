package com.example.yadshniya.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.R
import com.example.yadshniya.classes.Post

class PostAdapter(private val posts: List<Post>) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    // Create a new ViewHolder to display each post
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post, parent, false)
        return PostViewHolder(view)
    }

    // Bind the data to the ViewHolder (e.g., title, description, image)
    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.bind(post)
    }

    // Return the total number of items in the list
    override fun getItemCount(): Int {
        return posts.size
    }

    // ViewHolder class to hold the views of each post
    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val postImage: ImageView = itemView.findViewById(R.id.post_image)
        private val postDescription: TextView = itemView.findViewById(R.id.post_description)

        // Bind the post data to the view elements
        fun bind(post: Post) {
            postImage.setImageResource(post.imageResId)
            postDescription.text = post.description
        }
    }
}
