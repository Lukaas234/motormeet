package com.motormeet.uruguay.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VehicleDetailViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val account: io.appwrite.services.Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<VehicleDetailUiState>(VehicleDetailUiState.Loading)
    val uiState: StateFlow<VehicleDetailUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (_: Exception) {}
        }
    }

    fun loadVehicle(vehicleId: String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            try {
                val updated = vehicle.copy(isPublic = isPublic)
                vehicleRepository.updateVehicle(updated)
                    .onSuccess {
                        _uiState.value = VehicleDetailUiState.Success(updated)
                    }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteVehicle(vehicleId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId = account.get().id
                val deleteResult = vehicleRepository.deleteVehicle(vehicleId)
                if (deleteResult.isSuccess) {
                    try {
                        val profile = userRepository.getUserProfile(userId).first()
                        profile?.let { p ->
                            userRepository.updateUserProfile(p.copy(vehicleCount = (p.vehicleCount - 1).coerceAtLeast(0)))
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
}

sealed interface VehicleDetailUiState {
    data object Loading : VehicleDetailUiState
    data class Success(val vehicle: Vehicle) : VehicleDetailUiState
    data class Error(val message: String) : VehicleDetailUiState
}
