package com.motormeet.uruguay.presentation.events

import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class EventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<EventUiState>(EventUiState.Loading)
    val uiState: StateFlow<EventUiState> = _uiState.asStateFlow()

    private val _isUserVerified = MutableStateFlow(false)
    val isUserVerified: StateFlow<Boolean> = _isUserVerified.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        scope.launch {
            eventRepository.getUpcomingEvents()
                .onEach { events -> _uiState.value = EventUiState.Success(events) }
                .catch { _uiState.value = EventUiState.Error(it.message ?: "Error al cargar eventos") }
                .launchIn(scope)
        }
    }
}

sealed interface EventUiState {
    object Loading : EventUiState
    data class Success(val events: List<Event>) : EventUiState
    data class Error(val message: String) : EventUiState
}
