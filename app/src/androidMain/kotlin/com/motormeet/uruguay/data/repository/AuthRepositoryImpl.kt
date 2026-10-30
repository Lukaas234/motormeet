package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.util.AppwriteConstants
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepositoryImpl : AuthRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    override suspend fun login(email: String, pass: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val payload = mapOf(
            "email" to email,
            "password" to pass
        )
        val response = client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/account/sessions/emailPassword") {
            contentType = ContentType.Application.Json
            setBody(payload)
        }
        if (!response.isSuccess) throw Exception("Login failed: ${response.status}")
    }

    override suspend fun signUp(email: String, pass: String, name: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val payload = mapOf(
            "email" to email,
            "password" to pass,
            "name" to name
        )
        val response = client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/account") {
            contentType = ContentType.Application.Json
            setBody(payload)
        }
        if (!response.isSuccess) throw Exception("Sign up failed: ${response.status}")

        // Create session
        val sessionPayload = mapOf(
            "email" to email,
            "password" to pass
        )
        val sessionResponse = client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/account/sessions/emailPassword") {
            contentType = ContentType.Application.Json
            setBody(sessionPayload)
        }
        if (!sessionResponse.isSuccess) throw Exception("Session creation failed: ${sessionResponse.status}")

        // Create user document in database
        val userDocPayload = mapOf(
            "email" to email,
            "displayName" to name,
            "isVerified" to false,
            "followerCount" to 0,
            "followingCount" to 0,
            "postCount" to 0,
            "createdEventsCount" to 0,
            "attendedEventsCount" to 0,
            "bio" to "",
            "city" to ""
        )
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.USERS_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(userDocPayload)
        }
    }

    override suspend fun resetPassword(email: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val payload = mapOf(
            "email" to email,
            "url" to "https://motormeet.uruguay/reset"
        )
        val response = client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/account/recovery") {
            contentType = ContentType.Application.Json
            setBody(payload)
        }
        if (!response.isSuccess) throw Exception("Reset password failed: ${response.status}")
    }

    override suspend fun signOut(): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val response = client.delete("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/account/sessions/current")
        if (!response.isSuccess) throw Exception("Sign out failed: ${response.status}")
    }
}
