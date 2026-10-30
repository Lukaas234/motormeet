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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onEach

class CreateEventViewModel(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<CreateEventUiState>(CreateEventUiState.Idle)
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    fun createEvent(
        title: String,
        description: String,
        location: String,
        city: String,
        date: Long,
        hour: String,
        type: String,
        price: String,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        scope.launch {
            try {
                _uiState.value = CreateEventUiState.Loading

                val event = Event(
                    id = "",
                    title = title,
                    description = description,
                    date = date,
                    hour = hour,
                    location = location,
                    latitude = latitude,
                    longitude = longitude,
                    city = city,
                    organizerId = "",
                    organizerName = "",
                    type = type,
                    price = price,
                    imageUrl = null,
                    attendeesCount = 0,
                    likesCount = 0
                )

                eventRepository.createEvent(event)
                    .onSuccess {
                        _uiState.value = CreateEventUiState.Success
                    }
                    .onFailure {
                        _uiState.value = CreateEventUiState.Error(it.message ?: "Error al crear evento")
                    }
            } catch (e: Exception) {
                _uiState.value = CreateEventUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface CreateEventUiState {
    object Idle : CreateEventUiState
    object Loading : CreateEventUiState
    object Success : CreateEventUiState
    data class Error(val message: String) : CreateEventUiState
}
