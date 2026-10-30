package com.motormeet.uruguay.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val eventRepository: EventRepository,
    private val userRepository: UserRepository,
    private val account: Account,
) : ViewModel() {

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
        viewModelScope.launch {
            try {
                _uiState.value = CreateEventUiState.Loading
                val appwriteUser = account.get()
                
                val event = Event(
                    title = title,
                    description = description,
                    location = location,
                    city = city,
                    date = date,
                    hour = hour,
                    latitude = latitude,
                    longitude = longitude,
                    organizerId = appwriteUser.id,
                    organizerName = appwriteUser.name,
                    type = type,
                    price = price,
                    imageUrl = null
                )
                
                eventRepository.createEvent(event)
                    .onSuccess { 
                        // Incrementar contador de eventos creados
                        val profile = userRepository.getUserProfile(appwriteUser.id).firstOrNull()
                        profile?.let { p ->
                            userRepository.updateUserProfile(p.copy(createdEventsCount = p.createdEventsCount + 1))
                        }
                        _uiState.value = CreateEventUiState.Success 
                    }
                    .onFailure { _uiState.value = CreateEventUiState.Error(it.message ?: "Error al crear evento") }
            } catch (e: Exception) {
                _uiState.value = CreateEventUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface CreateEventUiState {
    data object Idle : CreateEventUiState
    data object Loading : CreateEventUiState
    data object Success : CreateEventUiState
    data class Error(val message: String) : CreateEventUiState
}
