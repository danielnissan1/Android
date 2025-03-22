package com.example.yadshniya.Fragments

import PostAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.R
import com.example.yadshniya.PostsListViewModel

class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
//    private var postList: MutableList<Post> = mutableListOf()
    private var viewModel: PostsListViewModel? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = ViewModelProvider(this)[PostsListViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.activity_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.feed_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postAdapter = PostAdapter(mutableListOf(), false) // Start with empty list
        recyclerView.adapter = postAdapter

        // Observe the posts and update the adapter
        instance().getAllPosts().observe(viewLifecycleOwner, Observer { posts ->
            Log.d("FeedFragment", "Fetched ${posts.size} posts")
            viewModel?.updatePostList(posts)
            postAdapter.updatePosts(posts)  // ✅ Properly updates the RecyclerView
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
