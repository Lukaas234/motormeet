package com.motormeet.uruguay.data.repository

import android.content.Context
import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.PostComment
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.util.AppwriteConstants
import dagger.hilt.android.qualifiers.ApplicationContext
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.models.InputFile
import io.appwrite.services.Databases
import io.appwrite.services.Storage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val databases: Databases,
    private val storage: Storage,
    @ApplicationContext private val context: Context
) : PostRepository {

    override fun getPosts(): Flow<List<Post>> = flow {
        try {
            val response = databases.listDocuments(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
                queries = listOf(Query.orderDesc("createdAt"))
            )
            val posts = response.documents.map { doc ->
                val data = doc.data
                Post(
                    id = doc.id,
                    userId = data["userId"]?.toString() ?: "",
                    userName = data["userName"]?.toString() ?: "",
                    userPhotoUrl = data["userPhotoUrl"]?.toString(),
                    isUserVerified = data["isUserVerified"] as? Boolean ?: false,
                    content = data["content"]?.toString() ?: "",
                    imageUrl = data["imageUrl"]?.toString(),
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                    likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
                    commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0
                )
            }
            emit(posts)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> = flow {
        try {
            var targetName = ""
            try {
                val userDoc = databases.getDocument(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.USERS_COLLECTION_ID,
                    documentId = userId
                )
                targetName = userDoc.data["displayName"]?.toString() ?: userDoc.data["name"]?.toString() ?: ""
            } catch (_: Exception) {
                try {
                    val list = databases.listDocuments(
                        databaseId = AppwriteConstants.DATABASE_ID,
                        collectionId = AppwriteConstants.USERS_COLLECTION_ID
                    )
                    val found = list.documents.find { it.id == userId }
                    if (found != null) {
                        targetName = found.data["displayName"]?.toString() ?: found.data["name"]?.toString() ?: ""
                    }
                } catch (_: Exception) {}
            }

            val response = try {
                databases.listDocuments(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.POSTS_COLLECTION_ID
                )
            } catch (_: Exception) {
                null
            }

            if (response == null) {
                emit(emptyList())
                return@flow
            }

            val posts = response.documents.mapNotNull { doc ->
                val data = doc.data
                val docUserId = data["userId"]?.toString() ?: ""
                val docUserName = data["userName"]?.toString() ?: ""

                val isMatch = docUserId == userId ||
                        (targetName.isNotEmpty() && docUserName.equals(targetName, ignoreCase = true))

                if (isMatch || userId.isBlank()) {
                    Post(
                        id = doc.id,
                        userId = docUserId,
                        userName = docUserName,
                        userPhotoUrl = data["userPhotoUrl"]?.toString(),
                        isUserVerified = data["isUserVerified"] as? Boolean ?: false,
                        content = data["content"]?.toString() ?: "",
                        imageUrl = data["imageUrl"]?.toString(),
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
                        commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0
                    )
                } else null
            }
            emit(posts)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getPostById(postId: String): Result<Post> = runCatching {
        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
            documentId = postId
        )
        val data = doc.data
        Post(
            id = doc.id,
            userId = data["userId"]?.toString() ?: "",
            userName = data["userName"]?.toString() ?: "",
            userPhotoUrl = data["userPhotoUrl"]?.toString(),
            isUserVerified = data["isUserVerified"] as? Boolean ?: false,
            content = data["content"]?.toString() ?: "",
            imageUrl = data["imageUrl"]?.toString(),
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis(),
            likesCount = (data["likesCount"] as? Number)?.toInt() ?: 0,
            commentsCount = (data["commentsCount"] as? Number)?.toInt() ?: 0
        )
    }

    override suspend fun createPost(post: Post): Result<Unit> = runCatching {
        val data = mapOf(
            "userId" to post.userId,
            "userName" to post.userName,
            "userPhotoUrl" to (post.userPhotoUrl ?: ""),
            "isUserVerified" to post.isUserVerified,
            "content" to post.content,
            "imageUrl" to (post.imageUrl ?: ""),
            "createdAt" to post.createdAt,
            "likesCount" to post.likesCount,
            "commentsCount" to post.commentsCount
        )
        databases.createDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )
    }

    override suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        databases.deleteDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
            documentId = postId
        )
    }

    override suspend fun likePost(postId: String, userId: String): Result<Unit> = runCatching {
        val prefs = context.getSharedPreferences("post_likes_prefs", Context.MODE_PRIVATE)
        val key = "liked_${userId}_$postId"
        val alreadyLiked = prefs.getBoolean(key, false)
        val newLikedState = !alreadyLiked
        prefs.edit().putBoolean(key, newLikedState).apply()

        val doc = databases.getDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
            documentId = postId
        )
        val currentLikes = (doc.data["likesCount"] as? Number)?.toInt() ?: 0
        val newLikes = if (newLikedState) currentLikes + 1 else (currentLikes - 1).coerceAtLeast(0)

        databases.updateDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
            documentId = postId,
            data = mapOf("likesCount" to newLikes)
        )
    }

    override fun getPostComments(postId: String): Flow<List<PostComment>> = flow {
        try {
            val response = try {
                databases.listDocuments(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.COMMENTS_COLLECTION_ID,
                    queries = listOf(Query.equal("postId", postId))
                )
            } catch (_: Exception) {
                databases.listDocuments(
                    databaseId = AppwriteConstants.DATABASE_ID,
                    collectionId = AppwriteConstants.COMMENTS_COLLECTION_ID
                )
            }

            val comments = response.documents.mapNotNull { doc ->
                val data = doc.data
                val docPostId = data["postId"]?.toString() ?: ""
                if (docPostId == postId || postId.isBlank()) {
                    PostComment(
                        id = doc.id,
                        postId = docPostId,
                        userId = data["userId"]?.toString() ?: "",
                        userName = data["userName"]?.toString() ?: "Usuario",
                        content = data["content"]?.toString() ?: "",
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                } else null
            }
            emit(comments.sortedBy { it.createdAt })
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addPostComment(
        postId: String,
        userId: String,
        userName: String,
        content: String
    ): Result<Unit> = runCatching {
        val data = mapOf(
            "postId" to postId,
            "userId" to userId,
            "userName" to userName,
            "content" to content,
            "createdAt" to System.currentTimeMillis()
        )
        databases.createDocument(
            databaseId = AppwriteConstants.DATABASE_ID,
            collectionId = AppwriteConstants.COMMENTS_COLLECTION_ID,
            documentId = ID.unique(),
            data = data
        )

        try {
            val doc = databases.getDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
                documentId = postId
            )
            val currentCount = (doc.data["commentsCount"] as? Number)?.toInt() ?: 0
            databases.updateDocument(
                databaseId = AppwriteConstants.DATABASE_ID,
                collectionId = AppwriteConstants.POSTS_COLLECTION_ID,
                documentId = postId,
                data = mapOf("commentsCount" to currentCount + 1)
            )
        } catch (_: Exception) {}
    }

    override suspend fun uploadPostPhoto(postId: String, uri: String): Result<String> = runCatching {
        val fileUri = android.net.Uri.parse(uri)
        val inputStream = context.contentResolver.openInputStream(fileUri)
        val tempFile = java.io.File(context.cacheDir, "post_${System.currentTimeMillis()}.jpg")
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
}
