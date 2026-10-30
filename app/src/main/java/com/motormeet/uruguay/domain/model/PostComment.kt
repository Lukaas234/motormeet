package com.motormeet.uruguay.domain.model

data class PostComment(
    val id: String = "",
    val postId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val isUserVerified: Boolean = false,
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
