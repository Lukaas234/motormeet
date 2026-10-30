package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.model.Vehicle
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.util.AppwriteConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

class VehicleRepositoryImpl : VehicleRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    override fun getVehicles(userId: String): Flow<List<Vehicle>> = callbackFlow {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.VEHICLES_COLLECTION_ID}/documents?queries=[" +
                "['equal', 'ownerId', '$userId']" +
                "]")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val vehicles = mutableListOf<Vehicle>()

            val docs = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (docs != null) {
                // Parse vehicle documents
            }

            emit(vehicles)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getVehicleById(vehicleId: String): kotlin.Result<Vehicle> = kotlin.runCatching {
        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.VEHICLES_COLLECTION_ID}/documents/$vehicleId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject

        Vehicle(
            id = data["id"]?.jsonPrimitive?.content ?: vehicleId,
            ownerId = data["ownerId"]?.jsonPrimitive?.content ?: "",
            brand = data["brand"]?.jsonPrimitive?.content ?: "",
            model = data["model"]?.jsonPrimitive?.content ?: "",
            year = (data["year"]?.jsonPrimitive?.content?.toInt()) ?: 2024,
            engine = data["engine"]?.jsonPrimitive?.content ?: "",
            power = data["power"]?.jsonPrimitive?.content ?: "",
            transmission = data["transmission"]?.jsonPrimitive?.content ?: "",
            color = data["color"]?.jsonPrimitive?.content ?: "",
            description = data["description"]?.jsonPrimitive?.content ?: "",
            modifications = data["modifications"]?.jsonPrimitive?.content ?: "",
            photoUrl = data["photoUrl"]?.jsonPrimitive?.content,
            createdAt = (data["createdAt"]?.jsonPrimitive?.content?.toLong()) ?: System.currentTimeMillis(),
            isPublic = (data["isPublic"]?.jsonPrimitive?.content?.toBoolean()) ?: true
        )
    }

    override suspend fun addVehicle(vehicle: Vehicle): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
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
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.VEHICLES_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
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
        client.put("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.VEHICLES_COLLECTION_ID}/documents/${vehicle.id}") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun deleteVehicle(vehicleId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        client.delete("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.VEHICLES_COLLECTION_ID}/documents/$vehicleId")
    }

    override suspend fun uploadVehiclePhoto(vehicleId: String, uri: String): kotlin.Result<String> = kotlin.runCatching {
        val file = java.io.File(uri)
        val response = client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${AppwriteConstants.STORAGE_BUCKET_ID}/files") {
            contentType = ContentType.Multipart.FormData
            formData {
                append("fileId", "unique()")
                append("file", file)
            }
        }
        val json = Json.parseToJsonElement(response.bodyAsText())
        val fileId = json.jsonObject["id"]?.jsonPrimitive?.content ?: ""
        "${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/storage/buckets/${AppwriteConstants.STORAGE_BUCKET_ID}/files/$fileId/view?project=${AppwriteConstants.APPWRITE_PROJECT_ID}"
    }
}
