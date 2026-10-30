package com.motormeet.uruguay.data.repository

import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.util.AppwriteConstants
import io.appwrite.ID
import io.appwrite.services.Account
import io.appwrite.services.Databases
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val account: Account,
    private val databases: Databases
) : AuthRepository {
    override suspend fun login(email: String, pass: String): Result<Unit> = runCatching {
        try {
            account.deleteSession(sessionId = "current")
        } catch (_: Exception) {
            // Ignore if no session is active
        }
        account.createEmailPasswordSession(email = email, password = pass)
    }

    override suspend fun signUp(email: String, pass: String, name: String): Result<Unit> = runCatching {
        try {
            account.deleteSession(sessionId = "current")
        } catch (_: Exception) {
            // Ignore if no session is active
        }
        val userResponse = account.create(userId = ID.unique(), email = email, password = pass, name = name)
        account.createEmailPasswordSession(email = email, password = pass)

        try {
            databases.createDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                documentId = userResponse.id,
                data = mapOf(
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
            )
        } catch (_: Exception) {
            try {
                databases.createDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = userResponse.id,
                    data = mapOf(
                        "email" to email,
                        "displayName" to name,
                        "isVerified" to false
                    )
                )
            } catch (_: Exception) {}
        }
    }

    override suspend fun resetPassword(email: String): Result<Unit> = runCatching {
        account.createRecovery(email = email, url = "https://motormeet.uruguay/reset")
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        account.deleteSession(sessionId = "current")
    }
}
