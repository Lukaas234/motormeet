package com.motormeet.uruguay.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.PostComment
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val account: Account,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<PostDetailUiState>(PostDetailUiState.Loading)
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    private val _comments = MutableStateFlow<List<PostComment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (_: Exception) {}
        }
    }

    fun loadPost(postId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = PostDetailUiState.Loading
                val userId = try { account.get().id } catch (_: Exception) { "" }
                _currentUserId.value = userId

                val prefs = context.getSharedPreferences("post_likes_prefs", Context.MODE_PRIVATE)
                val postResult = postRepository.getPostById(postId)
                postResult.onSuccess { post ->
                    _uiState.value = PostDetailUiState.Success(post)
                    _isLiked.value = prefs.getBoolean("liked_${userId}_$postId", false)
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
            .launchIn(viewModelScope)
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: account.get().id
                val currentlyLiked = _isLiked.value
                val newLikedState = !currentlyLiked
                _isLiked.value = newLikedState

                postRepository.likePost(postId, userId)
                    .onSuccess { loadPost(postId) }
                    .onFailure { _isLiked.value = currentlyLiked }
            } catch (e: Exception) {}
        }
    }

    fun addComment(postId: String, content: String) {
        viewModelScope.launch {
            try {
                val userId = account.get().id
                val profile = userRepository.getUserProfile(userId).first()
                postRepository.addPostComment(postId, userId, profile?.displayName ?: "Usuario", content)
                    .onSuccess { loadComments(postId) }
            } catch (_: Exception) {}
        }
    }
}

sealed interface PostDetailUiState {
    data object Loading : PostDetailUiState
    data class Success(val post: Post) : PostDetailUiState
    data class Error(val message: String) : PostDetailUiState
}
