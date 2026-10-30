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

class VehicleDetailViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<VehicleDetailUiState>(VehicleDetailUiState.Loading)
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    fun loadVehicle(vehicleId: String) {
        scope.launch {
            try {
                _uiState.value = VehicleDetailUiState.Loading
                vehicleRepository.getVehicleById(vehicleId)
                    .onSuccess { vehicle -> _uiState.value = VehicleDetailUiState.Success(vehicle) }
                    .onFailure { _uiState.value = VehicleDetailUiState.Error(it.message ?: "Error al cargar") }
            } catch (e: Exception) {
                _uiState.value = VehicleDetailUiState.Error(e.message ?: "Error al cargar")
            }
        }
    }

    fun updateVehicleVisibility(vehicle: Vehicle, isPublic: Boolean) {
        scope.launch {
            try {
                val updated = vehicle.copy(isPublic = isPublic)
                vehicleRepository.updateVehicle(updated)
                    .onSuccess {
                        _uiState.value = VehicleDetailUiState.Success(updated)
                    }
            } catch (_: Exception) {
            }
        }
    }

    fun deleteVehicle(vehicleId: String, onSuccess: () -> Unit) {
        scope.launch {
            try {
                val deleteResult = vehicleRepository.deleteVehicle(vehicleId)
                if (deleteResult.isSuccess) {
                    onSuccess()
                }
            } catch (_: Exception) {
            }
        }
    }
}

sealed interface VehicleDetailUiState {
    object Loading : VehicleDetailUiState
    data class Success(val vehicle: Vehicle) : VehicleDetailUiState
    data class Error(val message: String) : VehicleDetailUiState
}
