package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannels(): Flow<List<Channel>>
    suspend fun getChannelById(channelId: String): Channel?
    suspend fun createChannel(channel: Channel): Unit
    suspend fun deleteChannel(channelId: String): Unit
    fun getChannelMessages(channelId: String): Flow<List<ChannelMessage>>
    suspend fun sendMessage(channelId: String, senderId: String, senderName: String, content: String, imageUrl: String?): Unit
    suspend fun deleteChannelMessage(messageId: String): Unit
    suspend fun uploadChannelPhoto(channelId: String, uri: String): String?
    suspend fun subscribeChannel(channelId: String, userId: String): Unit
}
