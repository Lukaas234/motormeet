package com.motormeet.uruguay.presentation.garage

import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class AddVehicleViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<AddVehicleUiState>(AddVehicleUiState.Idle)
    val uiState: StateFlow<AddVehicleUiState> = _uiState.asStateFlow()

    fun addVehicle(
        brand: String,
        model: String,
        year: Int,
        engine: String,
        power: String,
        transmission: String,
        color: String,
        description: String,
        modifications: String,
        photoUri: String? = null,
        isPublic: Boolean = true
    ) {
        scope.launch {
            try {
                _uiState.value = AddVehicleUiState.Loading
                val userId = "current_user" // Would come from auth state

                val vehicle = Vehicle(
                    ownerId = userId,
                    brand = brand,
                    model = model,
                    year = year,
                    engine = engine,
                    power = power,
                    transmission = transmission,
                    color = color,
                    description = description,
                    modifications = modifications,
                    photoUrl = null,
                    isPublic = isPublic
                )
                vehicleRepository.addVehicle(vehicle)
                    .onSuccess {
                        _uiState.value = AddVehicleUiState.Success
                    }
                    .onFailure {
                        _uiState.value = AddVehicleUiState.Error(it.message ?: "Error al guardar")
                    }
            } catch (e: Exception) {
                _uiState.value = AddVehicleUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface AddVehicleUiState {
    object Idle : AddVehicleUiState
    object Loading : AddVehicleUiState
    object Success : AddVehicleUiState
    data class Error(val message: String) : AddVehicleUiState
}
