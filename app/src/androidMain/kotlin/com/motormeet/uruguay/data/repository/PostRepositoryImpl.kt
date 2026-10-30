package com.motormeet.uruguay.data.repository.android

import com.motormeet.uruguay.domain.model.Post
import com.motormeet.uruguay.domain.model.PostComment
import com.motormeet.uruguay.domain.repository.PostRepository
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

class PostRepositoryImpl : PostRepository {

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            serializer(JsonSerializer(kotlinx.serialization.json.Json { ignoreUnknownKeys = true }))
        }
    }

    private data class PostDoc(
        val id: String,
        val userId: String,
        val userName: String,
        val userPhotoUrl: String?,
        val isUserVerified: Boolean,
        val content: String,
        val imageUrl: String?,
        val createdAt: Long,
        val likesCount: Int,
        val commentsCount: Int
    )

    override fun getPosts(): Flow<List<Post>> = callbackFlow {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents?queries=[" +
                "['orderDesc', 'createdAt']" +
                "]")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val posts = mutableListOf<Post>()

            val docs = json.jsonObject["documents"]?.jsonPrimitive?.content
            if (docs != null) {
                val docArray = Json.parseToJsonElement(docs)
                // Simplified parsing - in production you'd properly parse the JSON array
            }

            emit(posts)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override fun getUserPosts(userId: String): Flow<List<Post>> = callbackFlow {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val posts = mutableListOf<Post>()

            emit(posts)
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun getPostById(postId: String): kotlin.Result<Post> = kotlin.runCatching {
        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents/$postId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject

        Post(
            id = data["id"]?.jsonPrimitive?.content ?: postId,
            userId = data["userId"]?.jsonPrimitive?.content ?: "",
            userName = data["userName"]?.jsonPrimitive?.content ?: "",
            userPhotoUrl = data["userPhotoUrl"]?.jsonPrimitive?.content,
            isUserVerified = (data["isUserVerified"]?.jsonPrimitive?.content?.toBoolean()) ?: false,
            content = data["content"]?.jsonPrimitive?.content ?: "",
            imageUrl = data["imageUrl"]?.jsonPrimitive?.content,
            createdAt = (data["createdAt"]?.jsonPrimitive?.content?.toLong()) ?: System.currentTimeMillis(),
            likesCount = (data["likesCount"]?.jsonPrimitive?.content?.toInt()) ?: 0,
            commentsCount = (data["commentsCount"]?.jsonPrimitive?.content?.toInt()) ?: 0
        )
    }

    override suspend fun createPost(post: Post): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
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
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun deletePost(postId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        client.delete("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents/$postId")
    }

    override suspend fun likePost(postId: String, userId: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        // Toggle like state using Multiplatform Settings would go here
        val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents/$postId")
        val json = Json.parseToJsonElement(response.bodyAsText())
        val data = json.jsonObject
        val currentLikes = (data["likesCount"]?.jsonPrimitive?.content?.toInt()) ?: 0

        client.put("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.POSTS_COLLECTION_ID}/documents/$postId") {
            contentType = ContentType.Application.Json
            setBody(mapOf("likesCount" to (currentLikes + 1)))
        }
    }

    override fun getPostComments(postId: String): Flow<List<PostComment>> = callbackFlow {
        try {
            val response = client.get("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.COMMENTS_COLLECTION_ID}/documents")
            val json = Json.parseToJsonElement(response.bodyAsText())
            val comments = mutableListOf<PostComment>()
            emit(comments.sortedBy { it.createdAt })
        } catch (_: Exception) {
            emit(emptyList())
        }
    }

    override suspend fun addPostComment(postId: String, userId: String, userName: String, content: String): kotlin.Result<kotlin.Unit> = kotlin.runCatching {
        val data = mapOf(
            "postId" to postId,
            "userId" to userId,
            "userName" to userName,
            "content" to content,
            "createdAt" to System.currentTimeMillis()
        )
        client.post("${AppwriteConstants.APPWRITE_PUBLIC_ENDPOINT}/databases/${AppwriteConstants.DATABASE_ID}/collections/${AppwriteConstants.COMMENTS_COLLECTION_ID}/documents") {
            contentType = ContentType.Application.Json
            setBody(data)
        }
    }

    override suspend fun uploadPostPhoto(postId: String, uri: String): kotlin.Result<String> = kotlin.runCatching {
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
}
