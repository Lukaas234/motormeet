package com.motormeet.uruguay.data.repository

import com.motormeet.uruguay.data.repository.android.*
import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.domain.repository.NotificationRepository

actual fun createAuthRepository(): AuthRepository = AuthRepositoryImpl()
actual fun createUserRepository(): UserRepository = UserRepositoryImpl()
actual fun createPostRepository(): PostRepository = PostRepositoryImpl()
actual fun createEventRepository(): EventRepository = EventRepositoryImpl()
actual fun createChannelRepository(): ChannelRepository = ChannelRepositoryImpl()
actual fun createVehicleRepository(): VehicleRepository = VehicleRepositoryImpl()
actual fun createNotificationRepository(): NotificationRepository = NotificationRepositoryImpl()
