package com.example.yadshniya.Fragments

import PostAdapter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Model
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.Post
import com.example.yadshniya.R

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private var postList: MutableList<Post> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("FeedFragment", "Fragment Loaded Successfully")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.feed_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList, false)
        recyclerView.adapter = postAdapter

        instance().getAllPosts().observe(viewLifecycleOwner, Observer { posts ->
            Log.d("FeedFragment", "Fetched ${posts.size} posts")

            postList.clear()
            postList.addAll(posts)
            postAdapter.notifyDataSetChanged()
        })

        instance().EventPostsListLoadingState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("FeedFragment", "Loading State: $state")
        })

        reloadData()
    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    private fun reloadData() {
        instance().refreshAllPosts()
    }
}
