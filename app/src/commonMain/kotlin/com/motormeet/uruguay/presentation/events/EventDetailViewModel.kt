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

class EventDetailViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    private val _isJoining = MutableStateFlow(false)
    val isJoining: StateFlow<Boolean> = _isJoining.asStateFlow()

    private val _hasLiked = MutableStateFlow(false)
    val hasLiked: StateFlow<Boolean> = _hasLiked.asStateFlow()

    private val _hasAttended = MutableStateFlow(false)
    val hasAttended: StateFlow<Boolean> = _hasAttended.asStateFlow()

    fun loadEvent(eventId: String) {
        scope.launch {
            try {
                _uiState.value = EventDetailUiState.Loading
                val eventResult = eventRepository.getEventById(eventId)
                eventResult.onSuccess { event ->
                    _uiState.value = EventDetailUiState.Success(event)
                }.onFailure {
                    _uiState.value = EventDetailUiState.Error(it.message ?: "Error al cargar evento")
                }
            } catch (e: Exception) {
                _uiState.value = EventDetailUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    fun joinEvent(eventId: String) {
        scope.launch {
            try {
                _isJoining.value = true
                val currentlyAttended = _hasAttended.value
                val newAttendedState = !currentlyAttended
                _hasAttended.value = newAttendedState

                val delta = if (newAttendedState) 1 else -1
                eventRepository.updateAttendeesCount(eventId, delta)
                    .onSuccess {
                        loadEvent(eventId)
                    }
                    .onFailure {
                        _hasAttended.value = currentlyAttended
                    }
            } catch (_: Exception) {
            } finally {
                _isJoining.value = false
            }
        }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit) {
        scope.launch {
            try {
                val deleteResult = eventRepository.deleteEvent(eventId)
                if (deleteResult.isSuccess) {
                    onSuccess()
                }
            } catch (_: Exception) {
            }
        }
    }

    fun likeEvent(eventId: String) {
        scope.launch {
            try {
                val currentlyLiked = _hasLiked.value
                val newLikedState = !currentlyLiked
                _hasLiked.value = newLikedState

                val delta = if (newLikedState) 1 else -1
                eventRepository.updateLikesCount(eventId, delta)
                    .onSuccess {
                        loadEvent(eventId)
                    }
                    .onFailure {
                        _hasLiked.value = currentlyLiked
                    }
            } catch (_: Exception) {
            }
        }
    }
}

sealed interface EventDetailUiState {
    object Loading : EventDetailUiState
    data class Success(val event: Event) : EventDetailUiState
    data class Error(val message: String) : EventDetailUiState
}
