package com.motormeet.uruguay.di

import com.motormeet.uruguay.data.repository.createAuthRepository
import com.motormeet.uruguay.data.repository.createUserRepository
import com.motormeet.uruguay.data.repository.createPostRepository
import com.motormeet.uruguay.data.repository.createEventRepository
import com.motormeet.uruguay.data.repository.createChannelRepository
import com.motormeet.uruguay.data.repository.createVehicleRepository
import com.motormeet.uruguay.data.repository.createNotificationRepository
import com.motormeet.uruguay.domain.repository.AuthRepository
import com.motormeet.uruguay.domain.repository.UserRepository
import com.motormeet.uruguay.domain.repository.PostRepository
import com.motormeet.uruguay.domain.repository.EventRepository
import com.motormeet.uruguay.domain.repository.ChannelRepository
import com.motormeet.uruguay.domain.repository.VehicleRepository
import com.motormeet.uruguay.domain.repository.NotificationRepository
import com.motormeet.uruguay.presentation.auth.AuthViewModel
import com.motormeet.uruguay.presentation.home.HomeViewModel
import com.motormeet.uruguay.presentation.home.CreatePostViewModel
import com.motormeet.uruguay.presentation.home.PostDetailViewModel
import com.motormeet.uruguay.presentation.events.EventViewModel
import com.motormeet.uruguay.presentation.events.CreateEventViewModel
import com.motormeet.uruguay.presentation.events.EventDetailViewModel
import com.motormeet.uruguay.presentation.garage.GarageViewModel
import com.motormeet.uruguay.presentation.garage.AddVehicleViewModel
import com.motormeet.uruguay.presentation.garage.VehicleDetailViewModel
import com.motormeet.uruguay.presentation.profile.ProfileViewModel
import com.motormeet.uruguay.presentation.profile.EditProfileViewModel
import com.motormeet.uruguay.presentation.channels.ChannelsViewModel
import com.motormeet.uruguay.presentation.channels.CreateChannelViewModel
import com.motormeet.uruguay.presentation.channels.ChannelDetailViewModel
import org.koin.dsl.module

val appModule = module {

    // Repositories (via expect/actual factory)
    single<AuthRepository> { createAuthRepository() }
    single<UserRepository> { createUserRepository() }
    single<PostRepository> { createPostRepository() }
    single<EventRepository> { createEventRepository() }
    single<ChannelRepository> { createChannelRepository() }
    single<VehicleRepository> { createVehicleRepository() }
    single<NotificationRepository> { createNotificationRepository() }

    // ViewModels
    factory { AuthViewModel(get(), get()) }
    factory { HomeViewModel(get(), get()) }
    factory { CreatePostViewModel(get()) }
    factory { PostDetailViewModel(get()) }
    factory { EventViewModel(get()) }
    factory { CreateEventViewModel(get()) }
    factory { EventDetailViewModel(get()) }
    factory { GarageViewModel(get(), get()) }
    factory { AddVehicleViewModel(get()) }
    factory { VehicleDetailViewModel(get()) }
    factory { ProfileViewModel(get()) }
    factory { EditProfileViewModel(get(), get()) }
    factory { ChannelsViewModel(get()) }
    factory { CreateChannelViewModel(get()) }
    factory { ChannelDetailViewModel(get()) }
}
