package com.motormeet.uruguay.domain.model

data class Channel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val ownerPhotoUrl: String? = null,
    val isOwnerVerified: Boolean = true,
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val subscribersCount: Int = 0
)
