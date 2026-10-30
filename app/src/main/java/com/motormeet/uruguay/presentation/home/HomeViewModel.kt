package com.motormeet.uruguay.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val account: io.appwrite.services.Account,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (_: Exception) {}
        }
        loadPosts()
    }

    fun loadPosts() {
        postRepository.getPosts()
            .onEach { posts -> _uiState.value = HomeUiState.Success(posts) }
            .catch { _uiState.value = HomeUiState.Error(it.message ?: "Error al cargar publicaciones") }
            .launchIn(viewModelScope)
    }

    fun isPostLiked(postId: String): Boolean {
        val userId = _currentUserId.value ?: return false
        val prefs = context.getSharedPreferences("post_likes_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("liked_${userId}_$postId", false)
    }

    fun likePost(postId: String) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: account.get().id
                postRepository.likePost(postId, userId)
                    .onSuccess { loadPosts() }
            } catch (_: Exception) {}
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val deleteResult = postRepository.deletePost(postId)
                if (deleteResult.isSuccess) {
                    try {
                        val profile = userRepository.getUserProfile(userId).first()
                        profile?.let { p ->
                            userRepository.updateUserProfile(p.copy(postCount = (p.postCount - 1).coerceAtLeast(0)))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    loadPosts()
                }
            } catch (e: Exception) {}
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        if (query.isNotBlank()) {
            searchUsers(query)
        } else {
            _searchResults.value = emptyList()
            _isSearching.value = false
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            userRepository.searchUsers(query)
                .onSuccess { users -> _searchResults.value = users }
                .onFailure { /* Manejar error */ }
            _isSearching.value = false
        }
    }
}

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val posts: List<Post>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
