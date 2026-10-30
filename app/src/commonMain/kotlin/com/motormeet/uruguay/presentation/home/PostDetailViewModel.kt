package com.motormeet.uruguay.presentation.home

import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.PostComment
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class PostDetailViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _comments = MutableStateFlow<List<PostComment>>(emptyList())
    val comments: StateFlow<List<PostComment>> = _comments.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    fun loadPost(postId: String) {
        scope.launch {
            try {
                _uiState.value = PostDetailUiState.Loading

                val postResult = postRepository.getPostById(postId)
                postResult.onSuccess { post ->
                    _uiState.value = PostDetailUiState.Success(post)
                    loadComments(postId)
                }.onFailure {
                    _uiState.value = PostDetailUiState.Error(it.message ?: "Error al cargar post")
                }
            } catch (e: Exception) {
                _uiState.value = PostDetailUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    private fun loadComments(postId: String) {
        postRepository.getPostComments(postId)
            .onEach { _comments.value = it }
            .launchIn(scope)
    }

    fun likePost(postId: String) {
        scope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val currentlyLiked = _isLiked.value
                val newLikedState = !currentlyLiked
                _isLiked.value = newLikedState

                postRepository.likePost(postId, userId)
                    .onSuccess { loadPost(postId) }
                    .onFailure { _isLiked.value = currentlyLiked }
            } catch (_: Exception) {}
        }
    }

    fun addComment(postId: String, content: String) {
        scope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                postRepository.addPostComment(postId, userId, "Usuario", content)
                    .onSuccess { loadComments(postId) }
            } catch (_: Exception) {}
        }
    }
}

sealed interface PostDetailUiState {
    object Loading : PostDetailUiState
    data class Success(val post: Post) : PostDetailUiState
    data class Error(val message: String) : PostDetailUiState
}
