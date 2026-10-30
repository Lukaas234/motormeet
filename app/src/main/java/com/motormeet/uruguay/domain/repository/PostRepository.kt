package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.PostComment
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    fun getPosts(): Flow<List<Post>>
    fun getUserPosts(userId: String): Flow<List<Post>>
    suspend fun getPostById(postId: String): Result<Post>
    suspend fun createPost(post: Post): Result<Unit>
    suspend fun deletePost(postId: String): Result<Unit>
    suspend fun likePost(postId: String, userId: String): Result<Unit>
    fun getPostComments(postId: String): Flow<List<PostComment>>
    suspend fun addPostComment(postId: String, userId: String, userName: String, content: String): Result<Unit>
    suspend fun uploadPostPhoto(postId: String, uri: String): Result<String>
}
