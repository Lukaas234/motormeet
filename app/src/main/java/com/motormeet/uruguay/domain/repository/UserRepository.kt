package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<User?>
    suspend fun updateUserProfile(user: User): Result<Unit>
    suspend fun uploadProfilePhoto(userId: String, uri: String): Result<String>
    suspend fun searchUsers(query: String): Result<List<User>>
    suspend fun isFollowing(currentUserId: String, targetUserId: String): Result<Boolean>
    suspend fun followUser(currentUserId: String, targetUserId: String): Result<Unit>
    suspend fun unfollowUser(currentUserId: String, targetUserId: String): Result<Unit>
}
