package com.motormeet.uruguay.presentation.garage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.appwrite.services.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddVehicleViewModel @Inject constructor(
    private val vehicleRepository: VehicleRepository,
    private val userRepository: UserRepository,
    private val account: Account
) : ViewModel() {

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
        viewModelScope.launch {
            try {
                _uiState.value = AddVehicleUiState.Loading
                val userId = account.get().id
                
                val photoUrl = photoUri?.let { uri ->
                    vehicleRepository.uploadVehiclePhoto(io.appwrite.ID.unique(), uri).getOrNull()
                }

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
                    photoUrl = photoUrl,
                    isPublic = isPublic
                )
                vehicleRepository.addVehicle(vehicle)
                    .onSuccess { 
                        viewModelScope.launch {
                            try {
                                val profile = userRepository.getUserProfile(userId).first()
                                profile?.let { p ->
                                    userRepository.updateUserProfile(p.copy(vehicleCount = p.vehicleCount + 1))
                                }
                            } catch (_: Exception) {}
                        }
                        _uiState.value = AddVehicleUiState.Success 
                    }
                    .onFailure { _uiState.value = AddVehicleUiState.Error(it.message ?: "Error al guardar") }
            } catch (e: Exception) {
                _uiState.value = AddVehicleUiState.Error("Error: ${e.message}")
            }
        }
    }
}

sealed interface AddVehicleUiState {
    data object Idle : AddVehicleUiState
    data object Loading : AddVehicleUiState
    data object Success : AddVehicleUiState
    data class Error(val message: String) : AddVehicleUiState
}
