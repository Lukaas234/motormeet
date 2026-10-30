package com.motormeet.uruguay.presentation.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelDetailViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

    private val _channelState = MutableStateFlow<ChannelDetailUiState>(ChannelDetailUiState.Loading)
    val channelState: StateFlow<ChannelDetailUiState> = _channelState.asStateFlow()

    private val _messages = MutableStateFlow<List<ChannelMessage>>(emptyList())
    val messages: StateFlow<List<ChannelMessage>> = _messages.asStateFlow()

    private val _currentUserId = MutableStateFlow("")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (_: Exception) {}
        }
    }

    fun loadChannel(channelId: String) {
        viewModelScope.launch {
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
            .launchIn(viewModelScope)
    }

    fun sendMessage(channelId: String, content: String, photoUri: String? = null) {
        if (content.isBlank() && photoUri == null) return
        viewModelScope.launch {
            try {
                _isSending.value = true
                val appwriteUser = account.get()
                val profile = userRepository.getUserProfile(appwriteUser.id).firstOrNull()
                val senderName = profile?.displayName ?: appwriteUser.name

                var imageUrl: String? = null
                if (photoUri != null) {
                    imageUrl = channelRepository.uploadChannelPhoto(channelId, photoUri).getOrNull()
                }

                channelRepository.sendMessage(channelId, appwriteUser.id, senderName, content, imageUrl)
                    .onSuccess {
                        loadMessages(channelId)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun deleteMessage(channelId: String, messageId: String) {
        viewModelScope.launch {
            try {
                channelRepository.deleteChannelMessage(messageId)
                    .onSuccess {
                        loadMessages(channelId)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed interface ChannelDetailUiState {
    data object Loading : ChannelDetailUiState
    data class Success(val channel: Channel) : ChannelDetailUiState
    data class Error(val message: String) : ChannelDetailUiState
}
