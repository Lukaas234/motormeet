package com.motormeet.uruguay.presentation.home

import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.User
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
import kotlinx.coroutines.flow.first

class HomeViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadPosts()
    }

    fun loadPosts() {
        scope.launch {
            postRepository.getPosts()
                .onEach { posts -> _uiState.value = HomeUiState.Success(posts) }
                .catch { _uiState.value = HomeUiState.Error(it.message ?: "Error al cargar publicaciones") }
                .launchIn(scope)
        }
    }

    fun isPostLiked(postId: String): Boolean {
        // Simplified: check via repository or settings
        return false
    }

    fun likePost(postId: String) {
        scope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                postRepository.likePost(postId, userId)
                    .onSuccess { loadPosts() }
            } catch (_: Exception) {}
        }
    }

    fun deletePost(postId: String) {
        scope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                val deleteResult = postRepository.deletePost(postId)
                if (deleteResult.isSuccess) {
                    loadPosts()
                }
            } catch (_: Exception) {}
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
        scope.launch {
            _isSearching.value = true
            userRepository.searchUsers(query)
                .onSuccess { users -> _searchResults.value = users }
                .onFailure { /* Ignore */ }
            _isSearching.value = false
        }
    }
}

sealed interface HomeUiState {
    object Loading : HomeUiState
    data class Success(val posts: List<Post>) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
