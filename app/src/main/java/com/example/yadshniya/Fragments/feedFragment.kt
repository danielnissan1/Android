package com.example.yadshniya.Fragments

import PostAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.Post
import com.example.yadshniya.R

class FeedFragment : Fragment(R.layout.activity_feed) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FeedFragment", "Fragment Loaded Successfully")

        recyclerView = view.findViewById(R.id.feed_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList, false)
        recyclerView.adapter = postAdapter

        loadPosts()
    }

    private fun loadPosts() {
        instance().getAllPosts({ posts ->
            if (posts != null) {
                Log.i("FeedFragment", "Retrieved ${posts.size} posts")
            }

            postList.clear()

            posts?.filterNotNull()?.let { nonNullPosts ->
                postList.addAll(nonNullPosts)
            }

            postAdapter.notifyDataSetChanged()
        })
    }
}
