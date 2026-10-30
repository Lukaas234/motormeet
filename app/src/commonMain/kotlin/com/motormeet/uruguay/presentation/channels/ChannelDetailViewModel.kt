package com.motormeet.uruguay.presentation.channels

import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import com.motormeet.uruguay.domain.repository.ChannelRepository
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

class ChannelDetailViewModel(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _channelState = MutableStateFlow<ChannelDetailUiState>(ChannelDetailUiState.Loading)
    val channelState: StateFlow<ChannelDetailUiState> = _channelState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChannelMessage>>(emptyList())
    val messages: StateFlow<List<ChannelMessage>> = _messages.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    fun loadChannel(channelId: String) {
        scope.launch {
            try {
                _channelState.value = ChannelDetailUiState.Loading
                val result = channelRepository.getChannelById(channelId)
                result.onSuccess { channel ->
                    _channelState.value = ChannelDetailUiState.Success(channel)
                    loadMessages(channelId)
                }.onFailure {
                    _channelState.value = ChannelDetailUiState.Error(it.message ?: "Error al cargar canal")
                }
            } catch (e: Exception) {
                _channelState.value = ChannelDetailUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    private fun loadMessages(channelId: String) {
        channelRepository.getChannelMessages(channelId)
            .onEach { _messages.value = it }
            .launchIn(scope)
    }

    fun sendMessage(channelId: String, content: String, photoUri: String? = null) {
        if (content.isBlank() && photoUri == null) return
        scope.launch {
            try {
                _isSending.value = true
                val senderName = "Usuario"

                var imageUrl: String? = null
                if (photoUri != null) {
                    // Photo upload would be handled via platform-specific code
                }

                channelRepository.sendMessage(channelId, _currentUserId.value, senderName, content, imageUrl)
                    .onSuccess {
                        loadMessages(channelId)
                    }
            } catch (_: Exception) {
            } finally {
                _isSending.value = false
            }
        }
    }

    fun deleteMessage(channelId: String, messageId: String) {
        scope.launch {
            try {
                channelRepository.deleteChannelMessage(messageId)
                    .onSuccess {
                        loadMessages(channelId)
                    }
            } catch (_: Exception) {
            }
        }
    }
}

sealed interface ChannelDetailUiState {
    object Loading : ChannelDetailUiState
    data class Success(val channel: Channel) : ChannelDetailUiState
    data class Error(val message: String) : ChannelDetailUiState
}
