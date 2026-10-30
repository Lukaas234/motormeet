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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn

class GarageViewModel(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository
) {

    private val scope = CoroutineScope(Job())

    private val _uiState = MutableStateFlow<GarageUiState>(GarageUiState.Loading)
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadVehicles()
    }

    private fun loadVehicles() {
        scope.launch {
            val userId = _currentUserId.value ?: return@launch
            vehicleRepository.getVehicles(userId)
                .onEach { vehicles ->
                    _uiState.value = GarageUiState.Success(vehicles)
                }
                .catch { _uiState.value = GarageUiState.Error(it.message ?: "Error al cargar garage") }
                .launchIn(scope)
        }
    }

    fun deleteVehicle(vehicleId: String) {
        scope.launch {
            try {
                val userId = _currentUserId.value ?: return@launch
                vehicleRepository.deleteVehicle(vehicleId)
                    .onSuccess {
                        loadVehicles()
                    }
            } catch (_: Exception) {}
        }
    }
}

sealed interface GarageUiState {
    object Loading : GarageUiState
    data class Success(val vehicles: List<Vehicle>) : GarageUiState
    data class Error(val message: String) : GarageUiState
}
