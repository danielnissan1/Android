package com.example.yadshniya

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.adapters.PostAdapter
import com.example.yadshniya.classes.Post

class FeedActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feed)

        recyclerView = findViewById(R.id.feed_recycler_view)

        // Create adapter with a list of sample posts
        adapter = PostAdapter(getSamplePosts())

        // Set the adapter and layout manager for the RecyclerView
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getSamplePosts(): List<Post> {
        return listOf(
            Post("Post 1", "Description of Post 1", R.drawable.sample_post),
            Post("Post 2", "Description of Post 2", R.drawable.sample_post),
            Post("Post 3", "Description of Post 3", R.drawable.sample_post)
        )
    }
}
