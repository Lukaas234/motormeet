package com.motormeet.uruguay.presentation.home

import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

class CreatePostViewModel(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun createPost(content: String, photoUri: String? = null) {
        scope.launch {
            try {
                _uiState.value = CreatePostUiState.Loading

                val photoUrl = photoUri?.let { uri ->
                    // Note: In a real KMP app, photo upload would be handled via platform-specific code
                    null
                }

                // We need user info - this would come from auth state
                // For now, create a post without user details (to be filled by repository/UI)
                val post = Post(
                    userId = "",
                    userName = "",
                    userPhotoUrl = null,
                    isUserVerified = false,
                    content = content,
                    imageUrl = photoUrl
                )

                postRepository.createPost(post)
                    .onSuccess {
                        _uiState.value = CreatePostUiState.Success
                    }
                    .onFailure {
                        _uiState.value = CreatePostUiState.Error(it.message ?: "Error al publicar")
                    }
            } catch (e: Exception) {
                _uiState.value = CreatePostUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface CreatePostUiState {
    object Idle : CreatePostUiState
    object Loading : CreatePostUiState
    object Success : CreatePostUiState
    data class Error(val message: String) : CreatePostUiState
}
