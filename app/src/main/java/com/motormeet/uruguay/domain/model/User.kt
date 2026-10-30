package com.motormeet.uruguay.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val bio: String = "",
    val city: String = "",
    val socialLinks: Map<String, String> = emptyMap(),
    val followerCount: Int = 0,
    val followingCount: Int = 0,
    val postCount: Int = 0,
    val vehicleCount: Int = 0,
    val createdEventsCount: Int = 0,
    val attendedEventsCount: Int = 0,
    val isVerified: Boolean = false
)
