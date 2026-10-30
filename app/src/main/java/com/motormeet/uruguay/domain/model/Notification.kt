package com.motormeet.uruguay.domain.model

data class Notification(
    val id: String = "",
    val userId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhotoUrl: String? = null,
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val postId: String? = null,
    val eventId: String? = null
)
