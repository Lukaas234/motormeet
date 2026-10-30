package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<User?>
    suspend fun updateUserProfile(user: User): Unit
    suspend fun uploadProfilePhoto(userId: String, uri: String): String?
    suspend fun searchUsers(query: String): List<User>
    suspend fun isFollowing(currentUserId: String, targetUserId: String): Boolean
    suspend fun followUser(currentUserId: String, targetUserId: String): Unit
    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Unit
}
