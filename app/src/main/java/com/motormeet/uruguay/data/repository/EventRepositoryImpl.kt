package com.motormeet.uruguay.data.repository

import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.model.EventComment
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.util.AppwriteConstants
import io.appwrite.ID
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EventRepositoryImpl @Inject constructor(
    private val databases: Databases,
    private val storage: Storage
) : EventRepository {

    companion object {
        private const val EVENT_EXPIRY_MILLIS = 7_200_000L // 2 horas después del evento
    }

    override fun getUpcomingEvents(): Flow<List<Event>> = flow {
        try {
            cleanupExpiredEvents()

            val response = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.EVENTS_COLLECTION_ID
            )
            val now = System.currentTimeMillis()
            val events = response.documents.mapNotNull { doc ->
                val data = doc.data
                val eventDate = (data["date"] as? Number)?.toLong() ?: now
                if (now - eventDate > EVENT_EXPIRY_MILLIS) {
                    null
                } else {
                    Event(
                        id = doc.id,
                        title = data["title"]?.toString() ?: "",
                        description = data["description"]?.toString() ?: "",
                        date = eventDate,
                        hour = data["hour"]?.toString() ?: "",
                        location = data["location"]?.toString() ?: "",
                        latitude = (data["latitude"] as? Number)?.toDouble(),
                        longitude = (data["longitude"] as? Number)?.toDouble(),
                        city = data["city"]?.toString() ?: "",
                        organizerId = data["organizerId"]?.toString() ?: "",
                        organizerName = data["organizerName"]?.toString() ?: "",
                        type = data["type"]?.toString() ?: "",
                        price = data["price"]?.toString() ?: "Gratis",
                        imageUrl = data["imageUrl"]?.toString(),
                        attendeesCount = (data["attendeesCount"] as? Number)?.toInt() ?: 0,
                        likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0
                    )
                }
            }
            emit(events)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    private suspend fun cleanupExpiredEvents() {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.EVENTS_COLLECTION_ID
            )
            val now = System.currentTimeMillis()
            val cutoff = now - EVENT_EXPIRY_MILLIS

            response.documents.forEach { doc ->
                val data = doc.data
                val eventDate = (data["date"] as? Number)?.toLong() ?: now
                if (eventDate < cutoff) {
                    databases.deleteDocument(
                        databaseId = AppwriteConstants.DATABASE_ID,
                        collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
                        documentId = doc.id
                    )
                }
            }
        } catch (_: Exception) {
            // Ignorar errores de limpieza
        }
    }

    override suspend fun getEventById(eventId: String): Result<Event> = runCatching {
        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = eventId
        )
        val data = doc.data
        Event(
            id = doc.id,
            title = data["title"]?.toString() ?: "",
            description = data["description"]?.toString() ?: "",
            date = (data["date"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            hour = data["hour"]?.toString() ?: "",
            location = data["location"]?.toString() ?: "",
            latitude = (data["latitude"] as? Number)?.toDouble(),
            longitude = (data["longitude"] as? Number)?.toDouble(),
            city = data["city"]?.toString() ?: "",
            organizerId = data["organizerId"]?.toString() ?: "",
            organizerName = data["organizerName"]?.toString() ?: "",
            type = data["type"]?.toString() ?: "",
            price = data["price"]?.toString() ?: "Gratis",
            imageUrl = data["imageUrl"]?.toString(),
            attendeesCount = (data["attendeesCount"] as? Number)?.toInt() ?: 0,
            likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0
        )
    }

    override suspend fun createEvent(event: Event): Result<Unit> = runCatching {
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
        databases.createDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
    }

    override suspend fun deleteEvent(eventId: String): Result<Unit> = runCatching {
        databases.deleteDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = eventId
        )
    }

    override suspend fun joinEvent(eventId: String, userId: String): Result<Unit> = updateAttendeesCount(eventId, 1)

    override suspend fun leaveEvent(eventId: String, userId: String): Result<Unit> = updateAttendeesCount(eventId, -1)

    override suspend fun likeEvent(eventId: String, userId: String): Result<Unit> = updateLikesCount(eventId, 1)

    override suspend fun updateAttendeesCount(eventId: String, delta: Int): Result<Unit> = runCatching {
        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = eventId
        )
        val currentCount = (doc.data["attendeesCount"] as? Number)?.toInt() ?: 0
        val newCount = (currentCount + delta).coerceAtLeast(0)
        databases.updateDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = eventId,
            data = mapOf("attendeesCount" to newCount)
        )
    }

    override suspend fun updateLikesCount(eventId: String, delta: Int): Result<Unit> = runCatching {
        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = eventId
        )
        val currentCount = (doc.data["likesCount"] as? Number)?.toInt() ?: 0
        val newCount = (currentCount + delta).coerceAtLeast(0)
        databases.updateDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.EVENTS_COLLECTION_ID,
            documentId = eventId,
            data = mapOf("likesCount" to newCount)
        )
    }

    override fun getEventComments(eventId: String): Flow<List<EventComment>> = flow { emit(emptyList()) }
    override suspend fun addComment(eventId: String, userId: String, userName: String, content: String): Result<Unit> = runCatching {}
    override suspend fun uploadEventPhoto(eventId: String, uri: String): Result<String> = runCatching { "" }
}
