package com.motormeet.uruguay.presentation.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateChannelViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<CreateChannelUiState>(CreateChannelUiState.Idle)
    val uiState: StateFlow<CreateChannelUiState> = _uiState.asStateFlow()

    fun createChannel(name: String, description: String) {
        viewModelScope.launch {
            try {
                _uiState.value = CreateChannelUiState.Loading
                val appwriteUser = account.get()
                val profile = userRepository.getUserProfile(appwriteUser.id).firstOrNull()

                val channel = Channel(
                    name = name,
                    description = description,
                    ownerId = appwriteUser.id,
                    ownerName = profile?.displayName ?: appwriteUser.name,
                    ownerPhotoUrl = profile?.photoUrl,
                    isOwnerVerified = profile?.isVerified ?: true,
                    subscribersCount = 1
                )

                channelRepository.createChannel(channel)
                    .onSuccess { _uiState.value = CreateChannelUiState.Success }
                    .onFailure { _uiState.value = CreateChannelUiState.Error(it.message ?: "Error al crear canal") }
            } catch (e: Exception) {
                _uiState.value = CreateChannelUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface CreateChannelUiState {
    data object Idle : CreateChannelUiState
    data object Loading : CreateChannelUiState
    data object Success : CreateChannelUiState
    data class Error(val message: String) : CreateChannelUiState
}
