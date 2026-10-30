package com.motormeet.uruguay.presentation.profile

import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val vehicleRepository: VehicleRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts: StateFlow<List<Post>> = _userPosts.asStateFlow()

    private val _currentUserId = MutableStateFlow<String>("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _publicVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val publicVehicles: StateFlow<List<Vehicle>> = _publicVehicles.asStateFlow()

    fun loadProfile(targetUserId: String? = null) {
        scope.launch {
            try {
                val loggedInId = _currentUserId.value.ifEmpty { "current_user" }
                _currentUserId.value = loggedInId
                val userIdToLoad = targetUserId?.takeIf { it.isNotBlank() } ?: loggedInId

                if (userIdToLoad != loggedInId) {
                    checkIsFollowing(userIdToLoad)
                    loadPublicVehicles(userIdToLoad)
                }

                userRepository.getUserProfile(userIdToLoad)
                    .onEach { user ->
                        if (user != null) {
                            _uiState.value = ProfileUiState.Success(user)
                            loadUserPosts(userIdToLoad)
                        } else {
                            val newUser = User(
                                id = loggedInId,
                                email = "",
                                displayName = "Usuario",
                                isVerified = false
                            )
                            _uiState.value = ProfileUiState.Success(newUser)
                        }
                    }
                    .catch { _uiState.value = ProfileUiState.Error(it.message ?: "Error al cargar perfil") }
                    .launchIn(scope)
            } catch (_: Exception) {
                _uiState.value = ProfileUiState.Error("Usuario no autenticado")
            }
        }
    }

    fun loadPublicVehicles(userId: String) {
        scope.launch {
            vehicleRepository.getVehicles(userId)
                .onEach { vehicles ->
                    _publicVehicles.value = vehicles.filter { it.isPublic }
                }
                .launchIn(scope)
        }
    }

    fun checkIsFollowing(targetUserId: String) {
        scope.launch {
            try {
                val loggedInId = _currentUserId.value.ifEmpty { "current_user" }
                userRepository.isFollowing(loggedInId, targetUserId)
                    .onSuccess { _isFollowing.value = it }
            } catch (_: Exception) {}
        }
    }

    fun toggleFollow(targetUserId: String) {
        scope.launch {
            try {
                val loggedInId = _currentUserId.value.ifEmpty { "current_user" }
                val currentlyFollowing = _isFollowing.value
                val newFollowingState = !currentlyFollowing
                _isFollowing.value = newFollowingState

                if (newFollowingState) {
                    userRepository.followUser(loggedInId, targetUserId)
                        .onSuccess {
                            loadProfile(targetUserId)
                        }
                        .onFailure {
                            _isFollowing.value = currentlyFollowing
                        }
                } else {
                    userRepository.unfollowUser(loggedInId, targetUserId)
                        .onSuccess {
                            loadProfile(targetUserId)
                        }
                        .onFailure {
                            _isFollowing.value = currentlyFollowing
                        }
                }
            } catch (_: Exception) {}
        }
    }

    private fun loadUserPosts(userId: String) {
        postRepository.getUserPosts(userId)
            .onEach { _userPosts.value = it }
            .launchIn(scope)
    }

    fun updateProfile(user: User) {
        scope.launch {
            userRepository.updateUserProfile(user)
        }
    }
}

sealed interface ProfileUiState {
    object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
