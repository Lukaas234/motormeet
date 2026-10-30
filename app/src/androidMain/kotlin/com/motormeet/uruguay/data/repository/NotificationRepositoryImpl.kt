package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.model.Notification
import com.motormeet.uruguay.domain.repository.NotificationRepository
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

class NotificationRepositoryImpl : NotificationRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    override fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.NOTIFICATIONS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val notifications = mutableListOf<Notification>()

            emit(notifications)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun markAsRead(notificationId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {}
}
