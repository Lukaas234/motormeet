package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Vehicle
import kotlinx.coroutines.flow.Flow

interface VehicleRepository {
    fun getVehicles(userId: String): Flow<List<Vehicle>>
    suspend fun getVehicleById(vehicleId: String): Vehicle?
    suspend fun addVehicle(vehicle: Vehicle): Unit
    suspend fun updateVehicle(vehicle: Vehicle): Unit
    suspend fun deleteVehicle(vehicleId: String): Unit
    suspend fun uploadVehiclePhoto(vehicleId: String, uri: String): String?
}
