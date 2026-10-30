package com.motormeet.uruguay.data.repository

import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.domain.repository.NotificationRepository

expect fun createAuthRepository(): AuthRepository
expect fun createUserRepository(): UserRepository
expect fun createPostRepository(): PostRepository
expect fun createEventRepository(): EventRepository
expect fun createChannelRepository(): ChannelRepository
expect fun createVehicleRepository(): VehicleRepository
expect fun createNotificationRepository(): NotificationRepository
