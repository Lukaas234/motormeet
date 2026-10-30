package com.motormeet.uruguay.presentation.channels

import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class CreateChannelViewModel(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<CreateChannelUiState>(CreateChannelUiState.Idle)
    val uiState: StateFlow<CreateChannelUiState> = _uiState.asStateFlow()

    fun createChannel(name: String, description: String) {
        scope.launch {
            try {
                _uiState.value = CreateChannelUiState.Loading

                val channel = Channel(
                    id = "",
                    name = name,
                    description = description,
                    ownerId = "current_user",
                    ownerName = "Usuario",
                    ownerPhotoUrl = null,
                    isOwnerVerified = false,
                    imageUrl = null,
                    createdAt = System.currentTimeMillis(),
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
    object Idle : CreateChannelUiState
    object Loading : CreateChannelUiState
    object Success : CreateChannelUiState
    data class Error(val message: String) : CreateChannelUiState
}
