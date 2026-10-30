package com.motormeet.uruguay.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreatePostViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val account: Account,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreatePostUiState>(CreatePostUiState.Idle)
    val uiState: StateFlow<CreatePostUiState> = _uiState.asStateFlow()

    fun createPost(content: String, photoUri: String? = null) {
        viewModelScope.launch {
            try {
                _uiState.value = CreatePostUiState.Loading
                val appwriteUser = account.get()
                val profile = userRepository.getUserProfile(appwriteUser.id).first()
                
                val photoUrl = photoUri?.let { uri ->
                    postRepository.uploadPostPhoto(io.appwrite.ID.unique(), uri).getOrNull()
                }

                val post = Post(
                    userId = appwriteUser.id,
                    userName = profile?.displayName ?: appwriteUser.name,
                    userPhotoUrl = profile?.photoUrl,
                    isUserVerified = profile?.isVerified ?: false,
                    content = content,
                    imageUrl = photoUrl
                )
                
                postRepository.createPost(post)
                    .onSuccess { 
                        profile?.let { p ->
                            userRepository.updateUserProfile(p.copy(postCount = (p.postCount + 1)))
                        }
                        _uiState.value = CreatePostUiState.Success 
                    }
                    .onFailure { _uiState.value = CreatePostUiState.Error(it.message ?: "Error al publicar") }
            } catch (e: Exception) {
                _uiState.value = CreatePostUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface CreatePostUiState {
    data object Idle : CreatePostUiState
    data object Loading : CreatePostUiState
    data object Success : CreatePostUiState
    data class Error(val message: String) : CreatePostUiState
}
