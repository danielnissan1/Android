package com.example.yadshniya

import androidx.lifecycle.ViewModel
import com.example.yadshniya.Model.Post

class PostsListViewModel : ViewModel() {
    private var _postList: List<Post>? = null
    val postList: List<Post>?
        get() = _postList

    fun updatePostList(posts: List<Post>) {
        _postList = posts
    }

    fun addPost(newPost: Post) {
        val currentList = _postList ?: emptyList()
        _postList = listOf(newPost) + currentList
    }

    fun removePost(post: Post) {
        val currentList = _postList ?: emptyList()
        _postList = currentList.filter { it != post }
    }
}
