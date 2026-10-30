package com.motormeet.uruguay.presentation.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChannelsViewModel @Inject constructor(
    private val channelRepository: ChannelRepository,
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<ChannelsUiState>(ChannelsUiState.Loading)
    val uiState: StateFlow<ChannelsUiState> = _uiState.asStateFlow()

    private val _isUserVerified = MutableStateFlow(false)
    val isUserVerified = _isUserVerified.asStateFlow()

    init {
        loadChannels()
        checkUserVerification()
    }

    fun loadChannels() {
        viewModelScope.launch {
            _uiState.value = ChannelsUiState.Loading
            channelRepository.getChannels()
                .onEach { channels -> _uiState.value = ChannelsUiState.Success(channels) }
                .catch { _uiState.value = ChannelsUiState.Error(it.message ?: "Error al cargar canales") }
                .launchIn(viewModelScope)
        }
    }

    private fun checkUserVerification() {
        viewModelScope.launch {
            try {
                val userId = account.get().id
                userRepository.getUserProfile(userId).collect { user ->
                    _isUserVerified.value = user?.isVerified ?: false
                }
            } catch (_: Exception) {}
        }
    }
}

sealed interface ChannelsUiState {
    data object Loading : ChannelsUiState
    data class Success(val channels: List<Channel>) : ChannelsUiState
    data class Error(val message: String) : ChannelsUiState
}
