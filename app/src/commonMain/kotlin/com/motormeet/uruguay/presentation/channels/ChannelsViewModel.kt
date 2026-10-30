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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class ChannelsViewModel(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<ChannelsUiState>(ChannelsUiState.Loading)
    val uiState: StateFlow<ChannelsUiState> = _uiState.asStateFlow()

    private val _isUserVerified = MutableStateFlow(false)
    val isUserVerified: StateFlow<Boolean> = _isUserVerified.asStateFlow()

    init {
        loadChannels()
    }

    fun loadChannels() {
        scope.launch {
            _uiState.value = ChannelsUiState.Loading
            channelRepository.getChannels()
                .onEach { channels -> _uiState.value = ChannelsUiState.Success(channels) }
                .catch { _uiState.value = ChannelsUiState.Error(it.message ?: "Error al cargar canales") }
                .launchIn(scope)
        }
    }
}

sealed interface ChannelsUiState {
    object Loading : ChannelsUiState
    data class Success(val channels: List<Channel>) : ChannelsUiState
    data class Error(val message: String) : ChannelsUiState
}
