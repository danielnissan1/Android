package com.example.yadshniya.Fragments

import PostAdapter
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yadshniya.Model.Model
import com.example.yadshniya.Model.Model.Companion.instance
import com.example.yadshniya.Model.Model.PostsListLoadingState
import com.example.yadshniya.R
import com.example.yadshniya.PostsListViewModel
import com.example.yadshniya.databinding.ActivityFeedBinding
class FeedFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var progressBar: View
    private var viewModel: PostsListViewModel? = null
    private lateinit var binding: ActivityFeedBinding
    val EventPostsListLoadingState: MutableLiveData<PostsListLoadingState> =
        MutableLiveData(PostsListLoadingState.NOT_LOADING)



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
        binding = ActivityFeedBinding.inflate(layoutInflater)

        recyclerView = view.findViewById(R.id.feed_recycler_view)
        progressBar = view.findViewById(R.id.progressBar)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        postAdapter = PostAdapter(mutableListOf(), false)
        recyclerView.adapter = postAdapter

        observePosts()
        reloadData()
    }

    private fun observePosts() {
        val hangerImage = view?.findViewById<ImageView>(R.id.image_view)

        instance().EventPostsListLoadingState.observe(viewLifecycleOwner, Observer { state ->
            if (state == Model.PostsListLoadingState.LOADING) {
                progressBar.visibility = View.VISIBLE
                hangerImage?.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                hangerImage?.visibility = View.VISIBLE
            }
        })

        instance().getAllPosts().observe(viewLifecycleOwner, Observer { posts ->
            if (posts.isNotEmpty()) {
                Log.d("FeedFragment", "Fetched ${posts.size} posts")
                viewModel?.updatePostList(posts)
                postAdapter.updatePosts(posts)
            } else {
                Log.d("FeedFragment", "No posts available")
            }

        })
    }

    override fun onResume() {
        super.onResume()
        reloadData()
    }

    private fun reloadData() {
        progressBar.visibility = View.VISIBLE
        instance().refreshAllPosts()
    }
}
