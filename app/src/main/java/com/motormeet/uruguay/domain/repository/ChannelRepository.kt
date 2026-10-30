package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Channel
import com.motormeet.uruguay.domain.model.ChannelMessage
import kotlinx.coroutines.flow.Flow

interface ChannelRepository {
    fun getChannels(): Flow<List<Channel>>
    suspend fun getChannelById(channelId: String): Result<Channel>
    suspend fun createChannel(channel: Channel): Result<Unit>
    suspend fun deleteChannel(channelId: String): Result<Unit>
    fun getChannelMessages(channelId: String): Flow<List<ChannelMessage>>
    suspend fun sendMessage(channelId: String, senderId: String, senderName: String, content: String, imageUrl: String? = null): Result<Unit>
    suspend fun deleteChannelMessage(messageId: String): Result<Unit>
    suspend fun uploadChannelPhoto(channelId: String, uri: String): Result<String>
    suspend fun subscribeChannel(channelId: String, userId: String): Result<Unit>
}
