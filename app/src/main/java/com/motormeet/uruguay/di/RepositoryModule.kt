package com.motormeet.uruguay.di

import com.motormeet.uruguay.data.repository.AuthRepositoryImpl
import com.motormeet.uruguay.data.repository.ChannelRepositoryImpl
import com.motormeet.uruguay.data.repository.EventRepositoryImpl
import com.motormeet.uruguay.data.repository.NotificationRepositoryImpl
import com.motormeet.uruguay.data.repository.PostRepositoryImpl
import com.motormeet.uruguay.data.repository.UserRepositoryImpl
import com.motormeet.uruguay.data.repository.VehicleRepositoryImpl
import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.NotificationRepository
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(authRepositoryImpl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun bindVehicleRepository(vehicleRepositoryImpl: VehicleRepositoryImpl): VehicleRepository

    @Binds
    @Singleton
    abstract fun bindEventRepository(eventRepositoryImpl: EventRepositoryImpl): EventRepository

    @Binds
    @Singleton
    abstract fun bindPostRepository(postRepositoryImpl: PostRepositoryImpl): PostRepository

    @Binds
    @Singleton
    abstract fun bindNotificationRepository(notificationRepositoryImpl: NotificationRepositoryImpl): NotificationRepository

    @Binds
    @Singleton
    abstract fun bindChannelRepository(channelRepositoryImpl: ChannelRepositoryImpl): ChannelRepository
}
