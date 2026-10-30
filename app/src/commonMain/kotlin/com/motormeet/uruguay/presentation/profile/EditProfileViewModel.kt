package com.motormeet.uruguay.presentation.profile

import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class EditProfileViewModel(
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<EditProfileUiState>(EditProfileUiState.Idle)
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        scope.launch {
            try {
                userRepository.getUserProfile("current_user")
                    .onEach { user ->
                        _user.value = user
                    }
                    .launchIn(scope)
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
        scope.launch {
            try {
                _uiState.value = EditProfileUiState.Loading
                val currentUserId = "current_user"

                val currentProfile = _user.value ?: userRepository.getUserProfile(currentUserId).firstOrNull()
                var photoUrl = currentProfile?.photoUrl

                if (newPhotoUri != null) {
                    // Photo upload would be handled via platform-specific code
                }

                val socialLinks = mutableMapOf<String, String>()
                if (instagram.isNotBlank()) socialLinks["instagram"] = instagram
                if (twitter.isNotBlank()) socialLinks["twitter"] = twitter
                if (web.isNotBlank()) socialLinks["web"] = web

                val updatedUser = User(
                    id = currentUserId,
                    email = currentProfile?.email ?: "",
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
    object Idle : EditProfileUiState
    object Loading : EditProfileUiState
    object Success : EditProfileUiState
    data class Error(val message: String) : EditProfileUiState
}
