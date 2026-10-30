package com.motormeet.uruguay.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            try {
                val currentUserId = account.get().id
                userRepository.getUserProfile(currentUserId)
                    .onEach { user ->
                        _user.value = user
                    }
                    .launchIn(viewModelScope)
            } catch (_: Exception) {}
        }
    }

    fun updateProfile(
        displayName: String,
        bio: String,
        city: String,
        instagram: String,
        twitter: String,
        web: String,
        newPhotoUri: String? = null
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = EditProfileUiState.Loading
                val appwriteUser = account.get()
                val currentUserId = appwriteUser.id
                val email = appwriteUser.email
                
                val currentProfile = _user.value ?: userRepository.getUserProfile(currentUserId).firstOrNull()
                var photoUrl = currentProfile?.photoUrl

                if (newPhotoUri != null) {
                    userRepository.uploadProfilePhoto(currentUserId, newPhotoUri)
                        .onSuccess { photoUrl = it }
                        .onFailure { 
                            _uiState.value = EditProfileUiState.Error("Error al subir foto: ${it.message}")
                            return@launch
                        }
                }

                val socialLinks = mutableMapOf<String, String>()
                if (instagram.isNotBlank()) socialLinks["instagram"] = instagram
                if (twitter.isNotBlank()) socialLinks["twitter"] = twitter
                if (web.isNotBlank()) socialLinks["web"] = web

                val updatedUser = User(
                    id = currentUserId,
                    email = email,
                    displayName = displayName,
                    bio = bio,
                    city = city,
                    socialLinks = socialLinks,
                    photoUrl = photoUrl,
                    followerCount = currentProfile?.followerCount ?: 0,
                    followingCount = currentProfile?.followingCount ?: 0,
                    postCount = currentProfile?.postCount ?: 0,
                    vehicleCount = currentProfile?.vehicleCount ?: 0,
                    createdEventsCount = currentProfile?.createdEventsCount ?: 0,
                    attendedEventsCount = currentProfile?.attendedEventsCount ?: 0,
                    isVerified = currentProfile?.isVerified ?: false
                )
                
                userRepository.updateUserProfile(updatedUser)
                    .onSuccess { _uiState.value = EditProfileUiState.Success }
                    .onFailure { _uiState.value = EditProfileUiState.Error(it.message ?: "Error al actualizar") }
            } catch (e: Exception) {
                _uiState.value = EditProfileUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface EditProfileUiState {
    data object Idle : EditProfileUiState
    data object Loading : EditProfileUiState
    data object Success : EditProfileUiState
    data class Error(val message: String) : EditProfileUiState
}
