package com.motormeet.uruguay.data.repository

import android.content.Context
import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.util.AppwriteConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VehicleRepositoryImpl @Inject constructor(
    private val databases: Databases,
    private val storage: Storage,
    @ApplicationContext private val context: Context
) : VehicleRepository {

    override fun getVehicles(userId: String): Flow<List<Vehicle>> = flow {
        try {
            val response = try {
                databases.listDocuments(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.VEHICLES_COLLECTION_ID,
                    queries = listOf(Query.equal("ownerId", userId))
                )
            } catch (_: Exception) {
                databases.listDocuments(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.VEHICLES_COLLECTION_ID
                )
            }

            val vehicles = response.documents.mapNotNull { doc ->
                val data = doc.data
                val ownerId = data["ownerId"]?.toString() ?: ""
                if (ownerId == userId || userId.isBlank()) {
                    Vehicle(
                        id = doc.id,
                        ownerId = ownerId,
                        brand = data["brand"]?.toString() ?: "",
                        model = data["model"]?.toString() ?: "",
                        year = (data["year"] as? Number)?.toInt() ?: 2024,
                        engine = data["engine"]?.toString() ?: "",
                        power = data["power"]?.toString() ?: "",
                        transmission = data["transmission"]?.toString() ?: "",
                        color = data["color"]?.toString() ?: "",
                        description = data["description"]?.toString() ?: "",
                        modifications = data["modifications"]?.toString() ?: "",
                        photoUrl = data["photoUrl"]?.toString(),
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        isPublic = data["isPublic"] as? Boolean ?: true
                    )
                } else null
            }
            emit(vehicles)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getVehicleById(vehicleId: String): Result<Vehicle> = runCatching {
        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.VEHICLES_COLLECTION_ID,
            documentId = vehicleId
        )
        val data = doc.data
        Vehicle(
            id = doc.id,
            ownerId = data["ownerId"]?.toString() ?: "",
            brand = data["brand"]?.toString() ?: "",
            model = data["model"]?.toString() ?: "",
            year = (data["year"] as? Number)?.toInt() ?: 2024,
            engine = data["engine"]?.toString() ?: "",
            power = data["power"]?.toString() ?: "",
            transmission = data["transmission"]?.toString() ?: "",
            color = data["color"]?.toString() ?: "",
            description = data["description"]?.toString() ?: "",
            modifications = data["modifications"]?.toString() ?: "",
            photoUrl = data["photoUrl"]?.toString(),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            isPublic = data["isPublic"] as? Boolean ?: true
        )
    }

    override suspend fun addVehicle(vehicle: Vehicle): Result<Unit> = runCatching {
        val data = mapOf(
            "ownerId" to vehicle.ownerId,
            "brand" to vehicle.brand,
            "model" to vehicle.model,
            "year" to vehicle.year,
            "engine" to vehicle.engine,
            "power" to vehicle.power,
            "transmission" to vehicle.transmission,
            "color" to vehicle.color,
            "description" to vehicle.description,
            "modifications" to vehicle.modifications,
            "photoUrl" to (vehicle.photoUrl ?: ""),
            "createdAt" to vehicle.createdAt,
            "isPublic" to vehicle.isPublic
        )
        databases.createDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.VEHICLES_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Result<Unit> = runCatching {
        val data = mapOf(
            "brand" to vehicle.brand,
            "model" to vehicle.model,
            "year" to vehicle.year,
            "engine" to vehicle.engine,
            "power" to vehicle.power,
            "transmission" to vehicle.transmission,
            "color" to vehicle.color,
            "description" to vehicle.description,
            "modifications" to vehicle.modifications,
            "photoUrl" to (vehicle.photoUrl ?: ""),
            "isPublic" to vehicle.isPublic
        )
        databases.updateDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.VEHICLES_COLLECTION_ID,
            documentId = vehicle.id,
            data = data
        )
    }

    override suspend fun deleteVehicle(vehicleId: String): Result<Unit> = runCatching {
        databases.deleteDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.VEHICLES_COLLECTION_ID,
            documentId = vehicleId
        )
    }

    override suspend fun uploadVehiclePhoto(vehicleId: String, uri: String): Result<String> = runCatching {
        val fileUri = android.net.Uri.parse(uri)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val tempFile = java.io.File(context.cacheDir, "vehicle_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        val file = InputFile.fromFile(tempFile)
        val response = storage.createFile(
            bucketId = AppwriteConstants.STORAGE_BUCKET_ID,
            fileId = ID.unique(),
            file = file
        )
        val fileId = response.id
        try { tempFile.delete() } catch (_: Exception) {}

        "${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${AppwriteConstants.STORAGE_BUCKET_ID}/files/$fileId/view?project=${AppwriteConstants.APPWRITE_PROJECT_ID}"
    }
}
