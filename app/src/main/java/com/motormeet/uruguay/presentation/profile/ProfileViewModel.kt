package com.motormeet.uruguay.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val postRepository: PostRepository,
    private val vehicleRepository: VehicleRepository,
    private val account: Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val _userPosts = MutableStateFlow<List<Post>>(emptyList())
    val userPosts = _userPosts.asStateFlow()

    private val _currentUserId = MutableStateFlow<String>("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    private val _publicVehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val publicVehicles: StateFlow<List<Vehicle>> = _publicVehicles.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (_: Exception) {}
        }
    }

    fun loadProfile(targetUserId: String? = null) {
        viewModelScope.launch {
            try {
                val loggedInId = account.get().id
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
                            val appwriteUser = account.get()
                            val newUser = User(
                                id = loggedInId,
                                email = appwriteUser.email,
                                displayName = appwriteUser.name,
                                isVerified = appwriteUser.emailVerification
                            )
                            userRepository.updateUserProfile(newUser)
                            _uiState.value = ProfileUiState.Success(newUser)
                        }
                    }
                    .catch { _uiState.value = ProfileUiState.Error(it.message ?: "Error al cargar perfil") }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error("Usuario no autenticado")
            }
        }
    }

    fun loadPublicVehicles(userId: String) {
        viewModelScope.launch {
            vehicleRepository.getVehicles(userId)
                .onEach { vehicles ->
                    _publicVehicles.value = vehicles.filter { it.isPublic }
                }
                .launchIn(viewModelScope)
        }
    }

    fun checkIsFollowing(targetUserId: String) {
        viewModelScope.launch {
            try {
                val loggedInId = _currentUserId.value.ifEmpty { account.get().id }
                userRepository.isFollowing(loggedInId, targetUserId)
                    .onSuccess { _isFollowing.value = it }
            } catch (_: Exception) {}
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            try {
                val loggedInId = _currentUserId.value.ifEmpty { account.get().id }
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
            .launchIn(viewModelScope)
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            userRepository.updateUserProfile(user)
        }
    }
}

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Success(val user: User) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}
