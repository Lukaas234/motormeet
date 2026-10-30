package com.motormeet.uruguay.domain.model

data class EventComment(
    val id: String = "",
    val eventId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String? = null,
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
