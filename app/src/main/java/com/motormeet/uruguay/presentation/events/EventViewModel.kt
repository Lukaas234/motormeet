package com.motormeet.uruguay.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Loading)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val _isUserVerified = MutableStateFlow(false)
    val isUserVerified = _isUserVerified.asStateFlow()

    init {
        loadEvents()
        checkUserVerification()
    }

    fun loadEvents() {
        eventRepository.getUpcomingEvents()
            .onEach { events -> _uiState.value = EventUiState.Success(events) }
            .catch { _uiState.value = EventUiState.Error(it.message ?: "Error al cargar eventos") }
            .launchIn(viewModelScope)
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

sealed interface EventUiState {
    data object Loading : EventUiState
    data class Success(val events: List<com.motormeet.uruguay.domain.model.Event>) : EventUiState
    data class Error(val message: String) : EventUiState
}
