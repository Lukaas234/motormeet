package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getVehicles(userId: String): Flow<List<Vehicle>>
    suspend fun getVehicleById(vehicleId: String): Result<Vehicle>
    suspend fun addVehicle(vehicle: Vehicle): Result<Unit>
    suspend fun updateVehicle(vehicle: Vehicle): Result<Unit>
    suspend fun deleteVehicle(vehicleId: String): Result<Unit>
    suspend fun uploadVehiclePhoto(vehicleId: String, uri: String): Result<String>
}
