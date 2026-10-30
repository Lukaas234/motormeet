package com.motormeet.uruguay.domain.model

data class ChannelMessage(
    val id: String = "",
    val channelId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val imageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
