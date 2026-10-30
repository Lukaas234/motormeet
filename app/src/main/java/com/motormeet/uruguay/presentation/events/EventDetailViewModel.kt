package com.motormeet.uruguay.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EventDetailViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val account: Account,
) : ViewModel() {

    private val _uiState = MutableStateFlow<EventDetailUiState>(EventDetailUiState.Loading)
    val uiState: StateFlow<EventDetailUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    private val _isJoining = MutableStateFlow(value = false)
    val isJoining = _isJoining.asStateFlow()

    private val _hasLiked = MutableStateFlow(false)
    val hasLiked = _hasLiked.asStateFlow()

    private val _hasAttended = MutableStateFlow(false)
    val hasAttended = _hasAttended.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (_: Exception) {
                _currentUserId.value = null
            }
        }
    }

    fun loadEvent(eventId: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            try {
                _isJoining.value = true
                val currentlyAttended = _hasAttended.value
                val newAttendedState = !currentlyAttended
                _hasAttended.value = newAttendedState

                val delta = if (newAttendedState) 1 else -1
                eventRepository.updateAttendeesCount(eventId, delta)
                    .onSuccess {
                        kotlinx.coroutines.delay(300)
                        loadEvent(eventId)
                    }
                    .onFailure {
                        _hasAttended.value = currentlyAttended
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isJoining.value = false
            }
        }
    }

    fun deleteEvent(eventId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = account.get().id
                val deleteResult = eventRepository.deleteEvent(eventId)
                if (deleteResult.isSuccess) {
                    try {
                        val profile = userRepository.getUserProfile(userId).first()
                        profile?.let { p ->
                            userRepository.updateUserProfile(p.copy(createdEventsCount = (p.createdEventsCount - 1).coerceAtLeast(0)))
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    onSuccess()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun likeEvent(eventId: String) {
        viewModelScope.launch {
            try {
                val currentlyLiked = _hasLiked.value
                val newLikedState = !currentlyLiked
                _hasLiked.value = newLikedState

                val delta = if (newLikedState) 1 else -1
                eventRepository.updateLikesCount(eventId, delta)
                    .onSuccess {
                        kotlinx.coroutines.delay(300)
                        loadEvent(eventId)
                    }
                    .onFailure {
                        _hasLiked.value = currentlyLiked
                    }
            } catch (_: Exception) {}
        }
    }
}

sealed interface EventDetailUiState {
    data object Loading : EventDetailUiState
    data class Success(val event: Event) : EventDetailUiState
    data class Error(val message: String) : EventDetailUiState
}
