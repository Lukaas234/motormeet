package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import com.motormeet.uruguay.domain.repository.ChannelRepository
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

class ChannelRepositoryImpl : ChannelRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    private val localChannels = mutableListOf<Channel>()
    private val localMessages = mutableMapOf<String, MutableList<ChannelMessage>>()

    override fun getChannels(): Flow<List<Channel>> = callbackFlow {
        val channels = mutableMapOf<String, Channel>()
        localChannels.forEach { channels[it.id] = it }

        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNELS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())

            val docs = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (docs != null) {
                // Parse channel documents
            }
        } catch (_: Exception) {}

        emit(channels.values.sortedByDescending { it.createdAt })
    }

    override suspend fun getChannelById(channelId: String): kotlin.Result<Channel> = kotlin.runCatching {
        val found = localChannels.find { it.id == channelId }
        if (found != null) return@runCatching found

        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNELS_COLLECTION_ID}/documents/$channelId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject

        Channel(
            id = data["id"]?.jsonPrimitive?.content ?: channelId,
            name = data["name"]?.jsonPrimitive?.content ?: "",
            description = data["description"]?.jsonPrimitive?.content ?: "",
            ownerId = data["ownerId"]?.jsonPrimitive?.content ?: "",
            ownerName = data["ownerName"]?.jsonPrimitive?.content ?: "",
            ownerPhotoUrl = data["ownerPhotoUrl"]?.jsonPrimitive?.content,
            isOwnerVerified = (data["isOwnerVerified"]?.jsonPrimitive?.content?.toBoolean()) ?: true,
            imageUrl = data["imageUrl"]?.jsonPrimitive?.content,
            createdAt = (data["createdAt"]?.jsonPrimitive?.content?.toLong()) ?: System.currentTimeMillis(),
            subscribersCount = (data["subscribersCount"]?.jsonPrimitive?.content?.toInt()) ?: 1
        )
    }

    override suspend fun createChannel(channel: Channel): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val newChannel = if (channel.id.isBlank()) channel.copy(id = java.util.UUID.randomUUID().toString()) else channel
        localChannels.add(newChannel)

        val data = mapOf(
            "name" to newChannel.name,
            "description" to newChannel.description,
            "ownerId" to newChannel.ownerId,
            "ownerName" to newChannel.ownerName,
            "isOwnerVerified" to newChannel.isOwnerVerified
        )
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNELS_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun deleteChannel(channelId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        localChannels.removeIf { it.id == channelId }
        client.delete("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNELS_COLLECTION_ID}/documents/$channelId")
    }

    override fun getChannelMessages(channelId: String): Flow<List<ChannelMessage>> = callbackFlow {
        val messagesMap = mutableMapOf<String, ChannelMessage>()
        localMessages[channelId]?.forEach { messagesMap[it.id] = it }

        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())

            val docs = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (docs != null) {
                // Parse channel messages
            }
        } catch (_: Exception) {}

        emit(messagesMap.values.sortedBy { it.createdAt })
    }

    override suspend fun sendMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        content: String,
        imageUrl: String? = null
    ): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val msg = ChannelMessage(
            id = java.util.UUID.randomUUID().toString(),
            channelId = channelId,
            senderId = senderId,
            senderName = senderName,
            content = content.ifEmpty { " " },
            imageUrl = imageUrl,
            createdAt = System.currentTimeMillis()
        )
        localMessages.getOrPut(channelId) { mutableListOf() }.add(msg)

        val data = mapOf(
            "channelId" to channelId,
            "senderId" to senderId,
            "senderName" to senderName,
            "content" to msg.content,
            "imageUrl" to (imageUrl ?: ""),
            "createdAt" to msg.createdAt
        )
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun deleteChannelMessage(messageId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        localMessages.values.forEach { list ->
            list.removeIf { it.id == messageId }
        }
        client.delete("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID}/documents/$messageId")
    }

    override suspend fun uploadChannelPhoto(channelId: String, uri: String): kotlin.Result<String> = kotlin.runCatching {
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

    override suspend fun subscribeChannel(channelId: String, userId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        try {
            client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.CHANNELS_COLLECTION_ID}/documents/$channelId")
        } catch (_: Exception) {}
    }
}
