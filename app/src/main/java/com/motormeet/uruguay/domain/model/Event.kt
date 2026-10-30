package com.motormeet.uruguay.domain.model

data class Event(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val date: Long = System.currentTimeMillis(),
    val hour: String = "",
    val location: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val city: String = "",
    val organizerId: String = "",
    val organizerName: String = "",
    val type: String = "",
    val price: String = "Gratis",
    val imageUrl: String? = null,
    val attendeesCount: Int = 0,
    val likesCount: Int = 0,
    val attendeeIds: List<String> = emptyList(),
    val likesIds: List<String> = emptyList()
)
