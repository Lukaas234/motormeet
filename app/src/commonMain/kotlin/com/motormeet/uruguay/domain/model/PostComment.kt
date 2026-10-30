package com.motormeet.uruguay.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class PostComment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val content: String = "",
    val userPhotoUrl: String? = null,
    val isUserVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
