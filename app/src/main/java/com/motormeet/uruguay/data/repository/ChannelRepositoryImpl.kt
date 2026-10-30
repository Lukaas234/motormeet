package com.motormeet.uruguay.data.repository

import android.content.Context
import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.util.AppwriteConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChannelRepositoryImpl @Inject constructor(
    private val databases: Databases,
    private val storage: Storage,
    @ApplicationContext private val context: Context
) : ChannelRepository {

    private val localChannels = mutableListOf<Channel>()
    private val localMessages = mutableMapOf<String, MutableList<ChannelMessage>>()

    override fun getChannels(): Flow<List<Channel>> = flow {
        val channels = mutableMapOf<String, Channel>()
        localChannels.forEach { channels[it.id] = it }

        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNELS_COLLECTION_ID
            )
            response.documents.forEach { doc ->
                val data = doc.data
                val ch = Channel(
                    id = doc.id,
                    name = data["name"]?.toString() ?: "",
                    description = data["description"]?.toString() ?: "",
                    ownerId = data["ownerId"]?.toString() ?: "",
                    ownerName = data["ownerName"]?.toString() ?: "",
                    ownerPhotoUrl = data["ownerPhotoUrl"]?.toString(),
                    isOwnerVerified = data["isOwnerVerified"] as? Boolean ?: true,
                    imageUrl = data["imageUrl"]?.toString(),
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    subscribersCount = (data["subscribersCount"] as? Number)?.toInt() ?: 1
                )
                channels[ch.id] = ch
            }
        } catch (_: Exception) {}

        emit(channels.values.sortedByDescending { it.createdAt })
    }

    override suspend fun getChannelById(channelId: String): Result<Channel> = runCatching {
        val found = localChannels.find { it.id == channelId }
        if (found != null) return@runCatching found

        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.CHANNELS_COLLECTION_ID,
            documentId = channelId
        )
        val data = doc.data
        Channel(
            id = doc.id,
            name = data["name"]?.toString() ?: "",
            description = data["description"]?.toString() ?: "",
            ownerId = data["ownerId"]?.toString() ?: "",
            ownerName = data["ownerName"]?.toString() ?: "",
            ownerPhotoUrl = data["ownerPhotoUrl"]?.toString(),
            isOwnerVerified = data["isOwnerVerified"] as? Boolean ?: true,
            imageUrl = data["imageUrl"]?.toString(),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            subscribersCount = (data["subscribersCount"] as? Number)?.toInt() ?: 1
        )
    }

    override suspend fun createChannel(channel: Channel): Result<Unit> = runCatching {
        val newChannel = if (channel.id.isBlank()) channel.copy(id = ID.unique()) else channel
        localChannels.add(newChannel)

        val data = mapOf(
            "name" to newChannel.name,
            "description" to newChannel.description,
            "ownerId" to newChannel.ownerId,
            "ownerName" to newChannel.ownerName,
            "isOwnerVerified" to newChannel.isOwnerVerified
        )
        try {
            databases.createDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNELS_COLLECTION_ID,
                documentId = newChannel.id,
                data = data
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun deleteChannel(channelId: String): Result<Unit> = runCatching {
        localChannels.removeIf { it.id == channelId }
        try {
            databases.deleteDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNELS_COLLECTION_ID,
                documentId = channelId
            )
        } catch (_: Exception) {}
    }

    override fun getChannelMessages(channelId: String): Flow<List<ChannelMessage>> = flow {
        val messagesMap = mutableMapOf<String, ChannelMessage>()
        localMessages[channelId]?.forEach { messagesMap[it.id] = it }

        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID
            )
            response.documents.forEach { doc ->
                val data = doc.data
                val docChannelId = data["channelId"]?.toString() ?: ""
                if (docChannelId == channelId || docChannelId.isEmpty()) {
                    val msg = ChannelMessage(
                        id = doc.id,
                        channelId = channelId,
                        senderId = data["senderId"]?.toString() ?: "",
                        senderName = data["senderName"]?.toString() ?: "Administrador",
                        content = data["content"]?.toString() ?: "",
                        imageUrl = data["imageUrl"]?.toString(),
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    messagesMap[msg.id] = msg
                }
            }
        } catch (_: Exception) {}

        emit(messagesMap.values.sortedBy { it.createdAt })
    }

    override suspend fun sendMessage(
        channelId: String,
        senderId: String,
        senderName: String,
        content: String,
        imageUrl: String?
    ): Result<Unit> = runCatching {
        val msg = ChannelMessage(
            id = ID.unique(),
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
        try {
            databases.createDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID,
                documentId = msg.id,
                data = data
            )
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                databases.createDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID,
                    documentId = msg.id,
                    data = mapOf("content" to msg.content)
                )
            } catch (_: Exception) {}
        }
    }

    override suspend fun deleteChannelMessage(messageId: String): Result<Unit> = runCatching {
        localMessages.values.forEach { list ->
            list.removeIf { it.id == messageId }
        }
        try {
            databases.deleteDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNEL_MESSAGES_COLLECTION_ID,
                documentId = messageId
            )
        } catch (_: Exception) {}
    }

    override suspend fun uploadChannelPhoto(channelId: String, uri: String): Result<String> = runCatching {
        val fileUri = android.net.Uri.parse(uri)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val tempFile = java.io.File(context.cacheDir, "channel_msg_${System.currentTimeMillis()}.jpg")
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

    override suspend fun subscribeChannel(channelId: String, userId: String): Result<Unit> = runCatching {
        try {
            databases.getDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.CHANNELS_COLLECTION_ID,
                documentId = channelId
            )
        } catch (_: Exception) {}
    }
}
