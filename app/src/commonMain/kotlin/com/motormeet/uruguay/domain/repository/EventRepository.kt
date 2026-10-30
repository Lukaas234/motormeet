package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Event
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getEvents(): Flow<List<Event>>
    suspend fun getEventById(eventId: String): Event?
    suspend fun createEvent(event: Event): Unit
    suspend fun deleteEvent(eventId: String): Unit
    suspend fun joinEvent(eventId: String, userId: String): Unit
    suspend fun leaveEvent(eventId: String, userId: String): Unit
    suspend fun likeEvent(eventId: String, userId: String): Unit
}
