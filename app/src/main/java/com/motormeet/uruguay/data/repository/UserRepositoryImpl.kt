package com.motormeet.uruguay.data.repository

import android.content.Context
import com.motormeet.uruguay.domain.model.User
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.util.AppwriteConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.InputFile
import io.appwrite.services.Account
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val databases: Databases,
    private val storage: Storage,
    private val account: Account,
    @ApplicationContext private val context: Context
) : UserRepository {
    override fun getUserProfile(userId: String): Flow<User?> = flow {
        var displayName = ""
        var email = ""
        var photoUrl: String? = null
        var bio = ""
        var city = ""
        var isVerified = false
        var followerCount = 0
        var followingCount = 0
        var postCount = 0
        var createdEventsCount = 0
        var hasDocInDb = false

        var currentLoggedInId: String? = null
        try {
            val appwriteUser = account.get()
            currentLoggedInId = appwriteUser.id
            if (userId.isBlank() || userId == currentLoggedInId) {
                displayName = appwriteUser.name
                email = appwriteUser.email
                if (appwriteUser.emailVerification || appwriteUser.labels.contains("verified") || appwriteUser.labels.contains("isVerified")) {
                    isVerified = true
                }
            }
        } catch (_: Exception) {}

        val isOwnProfile = userId.isBlank() || userId == currentLoggedInId

        // Fetch user document from database by userId or list documents
        try {
            val doc = databases.getDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                documentId = userId
            )
            val data = doc.data
            hasDocInDb = true
            email = data["email"]?.toString()?.takeIf { it.isNotEmpty() } ?: email
            displayName = data["displayName"]?.toString()?.takeIf { it.isNotEmpty() } ?: data["name"]?.toString()?.takeIf { it.isNotEmpty() } ?: displayName
            photoUrl = data["photoUrl"]?.toString()
            bio = data["bio"]?.toString() ?: ""
            city = data["city"]?.toString() ?: ""
            if (data.containsKey("isVerified")) {
                isVerified = (data["isVerified"] as? Boolean) == true
            }
            followerCount = (data["followerCount"] as? Number)?.toInt() ?: 0
            followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0
            postCount = (data["postCount"] as? Number)?.toInt() ?: 0
            createdEventsCount = (data["createdEventsCount"] as? Number)?.toInt() ?: 0
        } catch (_: Exception) {
            try {
                val list = databases.listDocuments(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID
                )
                val targetDoc = list.documents.find { doc ->
                    doc.id == userId || (isOwnProfile && email.isNotEmpty() && doc.data["email"]?.toString() == email)
                }
                if (targetDoc != null) {
                    hasDocInDb = true
                    val data = targetDoc.data
                    email = data["email"]?.toString()?.takeIf { it.isNotEmpty() } ?: email
                    displayName = data["displayName"]?.toString()?.takeIf { it.isNotEmpty() } ?: data["name"]?.toString()?.takeIf { it.isNotEmpty() } ?: displayName
                    photoUrl = data["photoUrl"]?.toString()
                    bio = data["bio"]?.toString() ?: ""
                    city = data["city"]?.toString() ?: ""
                    if (data.containsKey("isVerified")) {
                        isVerified = (data["isVerified"] as? Boolean) == true
                    }
                    followerCount = (data["followerCount"] as? Number)?.toInt() ?: 0
                    followingCount = (data["followingCount"] as? Number)?.toInt() ?: 0
                    postCount = (data["postCount"] as? Number)?.toInt() ?: 0
                    createdEventsCount = (data["createdEventsCount"] as? Number)?.toInt() ?: 0
                }
            } catch (_: Exception) {}
        }

        // Auto-create document in 'users' collection in Appwrite Databases if it doesn't exist yet
        if (!hasDocInDb && isOwnProfile && email.isNotEmpty() && !currentLoggedInId.isNullOrEmpty()) {
            try {
                databases.createDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = currentLoggedInId,
                    data = mapOf(
                        "email" to email,
                        "displayName" to displayName,
                        "isVerified" to false
                    )
                )
            } catch (_: Exception) {}
        }

        if (displayName.isBlank() && email.isBlank()) {
            emit(null)
            return@flow
        }

        emit(
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
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> = runCatching {
        try {
            account.updateName(name = user.displayName)
        } catch (_: Exception) {}

        val data = mapOf(
            "email" to user.email,
            "displayName" to user.displayName,
            "photoUrl" to (user.photoUrl ?: ""),
            "bio" to user.bio,
            "city" to user.city,
            "isVerified" to user.isVerified
        )

        var docId: String? = null
        try {
            val list = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                queries = listOf(Query.equal("email", user.email))
            )
            if (list.documents.isNotEmpty()) {
                docId = list.documents.first().id
            }
        } catch (_: Exception) {}

        if (docId != null) {
            try {
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = docId,
                    data = data
                )
            } catch (_: Exception) {
                val basicData = mapOf(
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "isVerified" to user.isVerified
                )
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = docId,
                    data = basicData
                )
            }
        } else {
            try {
                databases.createDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = user.id.ifEmpty { ID.unique() },
                    data = data
                )
            } catch (_: Exception) {
                val basicData = mapOf(
                    "email" to user.email,
                    "displayName" to user.displayName,
                    "isVerified" to user.isVerified
                )
                databases.createDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = user.id.ifEmpty { ID.unique() },
                    data = basicData
                )
            }
        }
    }

    override suspend fun uploadProfilePhoto(userId: String, uri: String): Result<String> = runCatching {
        val fileUri = android.net.Uri.parse(uri)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val tempFile = java.io.File(context.cacheDir, "profile_${System.currentTimeMillis()}.jpg")
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

    override suspend fun searchUsers(query: String): Result<List<User>> = runCatching {
        if (query.isBlank()) return@runCatching emptyList()

        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID
            )
            val q = query.trim().lowercase()
            val results = response.documents.mapNotNull { doc ->
                val data = doc.data
                val name = data["displayName"]?.toString() ?: data["name"]?.toString() ?: ""
                val email = data["email"]?.toString() ?: ""
                val city = data["city"]?.toString() ?: ""
                val photoUrl = data["photoUrl"]?.toString()
                val isVerified = (data["isVerified"] as? Boolean) == true

                if (name.lowercase().contains(q) || email.lowercase().contains(q) || city.lowercase().contains(q)) {
                    User(
                        id = doc.id,
                        email = email,
                        displayName = name.ifEmpty { "Usuario" },
                        photoUrl = photoUrl,
                        city = city,
                        isVerified = isVerified
                    )
                } else null
            }
            results
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun isFollowing(currentUserId: String, targetUserId: String): Result<Boolean> = runCatching {
        val prefs = context.getSharedPreferences("follow_prefs", Context.MODE_PRIVATE)
        prefs.getBoolean("follow_${currentUserId}_$targetUserId", false)
    }

    override suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        val prefs = context.getSharedPreferences("follow_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("follow_${currentUserId}_$targetUserId", true).apply()

        // 1. Target user followerCount +1
        try {
            val list = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID
            )
            val targetDoc = list.documents.find { it.id == targetUserId }
            if (targetDoc != null) {
                val currentFollowers = (targetDoc.data["followerCount"] as? Number)?.toInt() ?: 0
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = targetDoc.id,
                    data = mapOf("followerCount" to currentFollowers + 1)
                )
            } else {
                val doc = databases.getDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = targetUserId
                )
                val currentFollowers = (doc.data["followerCount"] as? Number)?.toInt() ?: 0
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = targetUserId,
                    data = mapOf("followerCount" to currentFollowers + 1)
                )
            }
        } catch (_: Exception) {}

        // 2. Current user followingCount +1
        try {
            val myAppwriteUser = account.get()
            val list = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID
            )
            val myDoc = list.documents.find { it.id == currentUserId || it.data["email"] == myAppwriteUser.email }
            if (myDoc != null) {
                val currentFollowing = (myDoc.data["followingCount"] as? Number)?.toInt() ?: 0
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = myDoc.id,
                    data = mapOf("followingCount" to currentFollowing + 1)
                )
            } else {
                val doc = databases.getDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = currentUserId
                )
                val currentFollowing = (doc.data["followingCount"] as? Number)?.toInt() ?: 0
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = currentUserId,
                    data = mapOf("followingCount" to currentFollowing + 1)
                )
            }
        } catch (_: Exception) {}
    }

    override suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit> = runCatching {
        val prefs = context.getSharedPreferences("follow_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("follow_${currentUserId}_$targetUserId", false).apply()

        // 1. Target user followerCount -1
        try {
            val list = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID
            )
            val targetDoc = list.documents.find { it.id == targetUserId }
            if (targetDoc != null) {
                val currentFollowers = (targetDoc.data["followerCount"] as? Number)?.toInt() ?: 0
                val newFollowers = (currentFollowers - 1).coerceAtLeast(0)
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = targetDoc.id,
                    data = mapOf("followerCount" to newFollowers)
                )
            } else {
                val doc = databases.getDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = targetUserId
                )
                val currentFollowers = (doc.data["followerCount"] as? Number)?.toInt() ?: 0
                val newFollowers = (currentFollowers - 1).coerceAtLeast(0)
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = targetUserId,
                    data = mapOf("followerCount" to newFollowers)
                )
            }
        } catch (_: Exception) {}

        // 2. Current user followingCount -1
        try {
            val myAppwriteUser = account.get()
            val list = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.USERS_COLLECTION_ID
            )
            val myDoc = list.documents.find { it.id == currentUserId || it.data["email"] == myAppwriteUser.email }
            if (myDoc != null) {
                val currentFollowing = (myDoc.data["followingCount"] as? Number)?.toInt() ?: 0
                val newFollowing = (currentFollowing - 1).coerceAtLeast(0)
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = myDoc.id,
                    data = mapOf("followingCount" to newFollowing)
                )
            } else {
                val doc = databases.getDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = currentUserId
                )
                val currentFollowing = (doc.data["followingCount"] as? Number)?.toInt() ?: 0
                val newFollowing = (currentFollowing - 1).coerceAtLeast(0)
                databases.updateDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = currentUserId,
                    data = mapOf("followingCount" to newFollowing)
                )
            }
        } catch (_: Exception) {}
    }
}
