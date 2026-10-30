package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.model.EventComment
import com.motormeet.uruguay.domain.repository.EventRepository
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

class EventRepositoryImpl : EventRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    companion object {
        private const val EVENT_EXPIRY_MILLIS = 7_200_000L // 2 hours after event
    }

    override fun getUpcomingEvents(): Flow<List<Event>> = callbackFlow {
        try {
            cleanupExpiredEvents()

            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val now = System.currentTimeMillis()
            val events = mutableListOf<Event>()

            val docs = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (docs != null) {
                val docArray = Json.parseToJsonElement(docs)
                // Parse each event document
            }

            emit(events)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    private suspend fun cleanupExpiredEvents() {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val now = System.currentTimeMillis()
            val cutoff = now - EVENT_EXPIRY_MILLIS

            val docs = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (docs != null) {
                // Delete expired events
            }
        } catch (_: Exception) {
            // Ignore cleanup errors
        }
    }

    override suspend fun getEventById(eventId: String): kotlin.Result<Event> = kotlin.runCatching {
        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents/$eventId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject

        Event(
            id = data["id"]?.jsonPrimitive?.content ?: eventId,
            title = data["title"]?.jsonPrimitive?.content ?: "",
            description = data["description"]?.jsonPrimitive?.content ?: "",
            date = (data["date"]?.jsonPrimitive?.content?.toLong()) ?: System.currentTimeMillis(),
            hour = data["hour"]?.jsonPrimitive?.content ?: "",
            location = data["location"]?.jsonPrimitive?.content ?: "",
            latitude = (data["latitude"]?.jsonPrimitive?.content?.toDouble()),
            longitude = (data["longitude"]?.jsonPrimitive?.content?.toDouble()),
            city = data["city"]?.jsonPrimitive?.content ?: "",
            organizerId = data["organizerId"]?.jsonPrimitive?.content ?: "",
            organizerName = data["organizerName"]?.jsonPrimitive?.content ?: "",
            type = data["type"]?.jsonPrimitive?.content ?: "",
            price = data["price"]?.jsonPrimitive?.content ?: "Gratis",
            imageUrl = data["imageUrl"]?.jsonPrimitive?.content,
            attendeesCount = (data["attendeesCount"]?.jsonPrimitive?.content?.toInt()) ?: 0,
            likesCount = (data["likesCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
        )
    }

    override suspend fun createEvent(event: Event): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val data = mapOf(
            "title" to event.title,
            "description" to event.description,
            "date" to event.date,
            "hour" to event.hour,
            "location" to event.location,
            "latitude" to (event.latitude ?: 0.0),
            "longitude" to (event.longitude ?: 0.0),
            "city" to event.city,
            "organizerId" to event.organizerId,
            "organizerName" to event.organizerName,
            "type" to event.type,
            "price" to event.price,
            "imageUrl" to (event.imageUrl ?: ""),
            "attendeesCount" to event.attendeesCount,
            "likesCount" to event.likesCount
        )
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun deleteEvent(eventId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        client.delete("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents/$eventId")
    }

    override suspend fun joinEvent(eventId: String, userId: String): kotlin.Result<kotlin.Unit> = updateAttendeesCount(eventId, 1)

    override suspend fun leaveEvent(eventId: String, userId: String): kotlin.Result<kotlin.Unit> = updateAttendeesCount(eventId, -1)

    override suspend fun likeEvent(eventId: String, userId: String): kotlin.Result<kotlin.Unit> = updateLikesCount(eventId, 1)

    override suspend fun updateAttendeesCount(eventId: String, delta: Int): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents/$eventId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject
        val currentCount = (data["attendeesCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
        val newCount = (currentCount + delta).coerceAtLeast(0)

        client.put("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents/$eventId") {
            contentType = ContentType.Application.Json
            setBody(mapOf("attendeesCount" to newCount))
        }
    }

    override suspend fun updateLikesCount(eventId: String, delta: Int): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents/$eventId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject
        val currentCount = (data["likesCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
        val newCount = (currentCount + delta).coerceAtLeast(0)

        client.put("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.EVENTS_COLLECTION_ID}/documents/$eventId") {
            contentType = ContentType.Application.Json
            setBody(mapOf("likesCount" to newCount))
        }
    }

    override fun getEventComments(eventId: String): Flow<List<EventComment>> = callbackFlow { emit(emptyList()) }

    override suspend fun addComment(eventId: String, userId: String, userName: String, content: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {}

    override suspend fun uploadEventPhoto(eventId: String, uri: String): kotlin.Result<String> = kotlin.runCatching { "" }
}
