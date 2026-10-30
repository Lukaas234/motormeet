package com.motormeet.uruguay.data.repository

import com.motormeet.uruguay.domain.model.Notification
import com.motormeet.uruguay.domain.repository.NotificationRepository
import io.appwrite.services.Databases
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val databases: Databases
) : NotificationRepository {
    override fun getNotifications(userId: String): Flow<List<Notification>> = flow { emit(emptyList()) }
    override suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {}
}
