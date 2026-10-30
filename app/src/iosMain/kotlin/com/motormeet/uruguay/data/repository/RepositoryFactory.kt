package com.motormeet.uruguay.data.repository

import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.domain.repository.NotificationRepository

actual fun createAuthRepository(): AuthRepository = object : AuthRepository {
    override suspend fun login(email: String, pass: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun signUp(email: String, pass: String, name: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun resetPassword(email: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun signOut(): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}

actual fun createUserRepository(): UserRepository = object : UserRepository {
    override fun getUserProfile(userId: String): kotlinx.coroutines.flow.Flow<User?> =
        kotlinx.coroutines.flow.flow { emit(null) }
    override suspend fun updateUserProfile(user: User): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun uploadProfilePhoto(userId: String, uri: String): kotlin.Result<String> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun searchUsers(query: String): kotlin.Result<List<User>> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun isFollowing(currentUserId: String, targetUserId: String): kotlin.Result<Boolean> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun followUser(currentUserId: String, targetUserId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun unfollowUser(currentUserId: String, targetUserId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}

actual fun createPostRepository(): PostRepository = object : PostRepository {
    override fun getPosts(): kotlinx.coroutines.flow.Flow<List<Post>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override fun getUserPosts(userId: String): kotlinx.coroutines.flow.Flow<List<Post>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun getPostById(postId: String): kotlin.Result<Post> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun createPost(post: Post): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun deletePost(postId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun likePost(postId: String, userId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override fun getPostComments(postId: String): kotlinx.coroutines.flow.Flow<List<PostComment>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun addPostComment(postId: String, userId: String, userName: String, content: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun uploadPostPhoto(postId: String, uri: String): kotlin.Result<String> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}

actual fun createEventRepository(): EventRepository = object : EventRepository {
    override fun getUpcomingEvents(): kotlinx.coroutines.flow.Flow<List<Event>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun getEventById(eventId: String): kotlin.Result<Event> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun createEvent(event: Event): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun deleteEvent(eventId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun joinEvent(eventId: String, userId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun leaveEvent(eventId: String, userId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun likeEvent(eventId: String, userId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun updateAttendeesCount(eventId: String, delta: Int): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun updateLikesCount(eventId: String, delta: Int): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override fun getEventComments(eventId: String): kotlinx.coroutines.flow.Flow<List<EventComment>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun addComment(eventId: String, userId: String, userName: String, content: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun uploadEventPhoto(eventId: String, uri: String): kotlin.Result<String> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}

actual fun createChannelRepository(): ChannelRepository = object : ChannelRepository {
    override fun getChannels(): kotlinx.coroutines.flow.Flow<List<Channel>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun getChannelById(channelId: String): kotlin.Result<Channel> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun createChannel(channel: Channel): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun deleteChannel(channelId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override fun getChannelMessages(channelId: String): kotlinx.coroutines.flow.Flow<List<ChannelMessage>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun sendMessage(channelId: String, senderId: String, senderName: String, content: String, imageUrl: String?): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun deleteChannelMessage(messageId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun uploadChannelPhoto(channelId: String, uri: String): kotlin.Result<String> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun subscribeChannel(channelId: String, userId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}

actual fun createVehicleRepository(): VehicleRepository = object : VehicleRepository {
    override fun getVehicles(userId: String): kotlinx.coroutines.flow.Flow<List<Vehicle>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun getVehicleById(vehicleId: String): kotlin.Result<Vehicle> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun addVehicle(vehicle: Vehicle): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun updateVehicle(vehicle: Vehicle): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun deleteVehicle(vehicleId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
    override suspend fun uploadVehiclePhoto(vehicleId: String, uri: String): kotlin.Result<String> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}

actual fun createNotificationRepository(): NotificationRepository = object : NotificationRepository {
    override fun getNotifications(userId: String): kotlinx.coroutines.flow.Flow<List<Notification>> =
        kotlinx.coroutines.flow.flow { emit(emptyList()) }
    override suspend fun markAsRead(notificationId: String): kotlin.Result<kotlin.Unit> =
        kotlin.Result.failure(UnsupportedOperationException("iOS not implemented"))
}
