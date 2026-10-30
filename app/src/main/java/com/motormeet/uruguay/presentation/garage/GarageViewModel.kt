package com.motormeet.uruguay.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.VehicleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GarageViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: com.motormeet.uruguay.domain.repository.UserRepository,
    private val account: Account
) : ViewModel() {

    private val _uiState = MutableStateFlow<GarageUiState>(GarageUiState.Loading)
    val uiState: StateFlow<GarageUiState> = _uiState.asStateFlow()

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId = _currentUserId.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                _currentUserId.value = account.get().id
            } catch (e: Exception) {}
        }
        loadVehicles()
    }

    private fun loadVehicles() {
        viewModelScope.launch {
            try {
                val userId = account.get().id
                vehicleRepository.getVehicles(userId)
                    .onEach { vehicles ->
                        _uiState.value = GarageUiState.Success(vehicles)
                    }
                    .catch { _uiState.value = GarageUiState.Error(it.message ?: "Error al cargar garage") }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _uiState.value = GarageUiState.Error("Usuario no autenticado")
            }
        }
    }

    fun deleteVehicle(vehicleId: String) {
        viewModelScope.launch {
            try {
                val userId = account.get().id
                vehicleRepository.deleteVehicle(vehicleId)
                    .onSuccess {
                        // Restar del contador del perfil
                        viewModelScope.launch {
                            try {
                                val profile = userRepository.getUserProfile(userId).first()
                                profile?.let { p ->
                                    userRepository.updateUserProfile(p.copy(vehicleCount = (p.vehicleCount - 1).coerceAtLeast(0)))
                                }
                            } catch (e: Exception) {}
                        }
                        loadVehicles() // Recargar lista
                    }
            } catch (e: Exception) {}
        }
    }
}

sealed interface GarageUiState {
    data object Loading : GarageUiState
    data class Success(val vehicles: List<Vehicle>) : GarageUiState
    data class Error(val message: String) : GarageUiState
}
