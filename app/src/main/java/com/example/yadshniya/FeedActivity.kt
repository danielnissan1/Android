package com.example.yadshniya

import PostAdapter
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.Post

class FeedActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_feed)

        recyclerView = findViewById(R.id.feed_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)
        postAdapter = PostAdapter(postList)
        recyclerView.adapter = postAdapter

        loadPosts()
    }

    private fun loadPosts() {
        instance().getAllPosts({ posts ->
            if (posts != null) {
                Log.i("FeedActivity", "Retrieved ${posts.size} posts")
            }

            postList.clear()

            posts?.filterNotNull()?.let { nonNullPosts ->
                postList.addAll(nonNullPosts)
            }

            postAdapter.notifyDataSetChanged()
        },)
    }

}
