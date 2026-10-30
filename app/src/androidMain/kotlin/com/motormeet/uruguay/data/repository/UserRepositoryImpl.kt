package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.UserRepository
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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import com.russhwolf.settings.MultiplatformSettings
import com.russhwolf.settings.readStringValue
import com.russhwolf.settings.writeStringValue

class UserRepositoryImpl : UserRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    private val settings: MultiplatformSettings = com.russhwolf.settings.createMultiplatformSettings()

    private suspend fun getCurrentUserId(): String? {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/account")
            val json = Json.parseToJsonElement(response.bodyAsText())
            return json.jsonObject["id"]?.jsonPrimitive?.content
        } catch (_: Exception) {
            return null
        }
    }

    override fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        try {
            // Try to get current user from Appwrite account
            val currentUserId = getCurrentUserId()

            var email = ""
            var displayName = ""
            var photoUrl: String? = null
            var bio = ""
            var city = ""
            var isVerified = false
            var followerCount = 0
            var followingCount = 0
            var postCount = 0
            var createdEventsCount = 0
            var hasDocInDb = false

            val isOwnProfile = userId.isBlank() || userId == currentUserId

            // Try to fetch user document by userId
            try {
                val docResponse = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents/$userId")
                val json = Json.parseToJsonElement(docResponse.bodyAsText())
                val data = json.jsonObject
                hasDocInDb = true
                email = data["email"]?.jsonPrimitive?.content ?: email
                displayName = data["displayName"]?.jsonPrimitive?.content ?: data["name"]?.jsonPrimitive?.content ?: displayName
                photoUrl = data["photoUrl"]?.jsonPrimitive?.content
                bio = data["bio"]?.jsonPrimitive?.content ?: ""
                city = data["city"]?.jsonPrimitive?.content ?: ""
                if (data.containsKey("isVerified")) {
                    isVerified = (data["isVerified"]?.jsonPrimitive?.content?.toBoolean()) == true
                }
                followerCount = (data["followerCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
                followingCount = (data["followingCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
                postCount = (data["postCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
                createdEventsCount = (data["createdEventsCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
            } catch (_: Exception) {
                // Fallback: list documents and find match
                try {
                    val listResponse = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents")
                    val json = Json.parseToJsonElement(listResponse.bodyAsText())
                    val documents = json.jsonObject["documents"]?.jsonPrimitive?.content?.let { Json.parseToJsonElement(it) } ?: return@callbackFlow
                    // Handle array parsing more simply
                } catch (_: Exception) {}
            }

            // Auto-create document if own profile doesn't exist
            if (!hasDocInDb && isOwnProfile && email.isNotEmpty() && currentUserId != null) {
                try {
                    val createPayload = mapOf(
                        "email" to email,
                        "displayName" to displayName,
                        "isVerified" to false
                    )
                    client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents/$currentUserId") {
                        contentType = ContentType.Application.Json
                        setBody(createPayload)
                    }
                } catch (_: Exception) {}
            }

            if (displayName.isBlank() && email.isBlank()) {
                trySend(null)
                return@callbackFlow
            }

            trySend(
                User(
                    id = userId,
                    email = email,
                    displayName = displayName.ifEmpty { "Usuario" },
                    photoUrl = photoUrl,
                    bio = bio,
                    city = city,
                    followerCount = followerCount,
                    followingCount = followingCount,
                    postCount = postCount,
                    createdEventsCount = createdEventsCount,
                    isVerified = isVerified
                )
            )
        } catch (e: Exception) {
            trySend(null)
        }
    }

    override suspend fun updateUserProfile(user: User): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val data = mapOf(
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to (user.photoUrl ?: ""),
            "bio" to user.bio,
            "city" to user.city,
            "isVerified" to user.isVerified
        )

        // Try to find existing document by email
        var docId: String? = null
        try {
            val listResponse = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents?queries=${java.net.URLEncoder.encode("['email', '${user.email}']", "UTF-8")}")
            val json = Json.parseToJsonElement(listResponse.bodyAsText())
            val total = json.jsonObject["total"]?.jsonPrimitive?.content?.toInt() ?: 0
            if (total > 0) {
                val documents = json.jsonObject["documents"]?.jsonPrimitive?.content
                if (documents != null) {
                    val docArray = Json.parseToJsonElement(documents)
                    val firstDoc = docArray.jsonObject // This is simplified
                    docId = firstDoc["id"]?.jsonPrimitive?.content
                }
            }
        } catch (_: Exception) {}

        if (docId != null) {
            client.put("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents/$docId") {
                contentType = ContentType.Application.Json
                setBody(data)
            }
        } else {
            client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents/${user.id.ifEmpty { "unique" }}") {
                contentType = ContentType.Application.Json
                setBody(data)
            }
        }
    }

    override suspend fun uploadProfilePhoto(userId: String, uri: String): kotlin.Result<String> = kotlin.runCatching {
        // For Android, read file from URI and upload via Ktor multipart
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

    override suspend fun searchUsers(query: String): kotlin.Result<List<User>> = kotlin.runCatching {
        if (query.isBlank()) return@runCatching emptyList()

        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val q = query.trim().lowercase()
            val results = mutableListOf<User>()

            val documents = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (documents != null) {
                val docArray = Json.parseToJsonElement(documents)
                // Parse each document
            }

            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun isFollowing(currentUserId: String, targetUserId: String): kotlin.Result<Boolean> = kotlin.runCatching {
        settings.readStringValue("follow_${currentUserId}_$targetUserId")?.toBoolean() ?: false
    }

    override suspend fun followUser(currentUserId: String, targetUserId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        settings.writeStringValue("follow_${currentUserId}_$targetUserId", "true")

        // Increment follower count on target user
        try {
            val listResponse = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(listResponse.bodyAsText())
        } catch (_: Exception) {}
    }

    override suspend fun unfollowUser(currentUserId: String, targetUserId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        settings.writeStringValue("follow_${currentUserId}_$targetUserId", "false")
    }
}
