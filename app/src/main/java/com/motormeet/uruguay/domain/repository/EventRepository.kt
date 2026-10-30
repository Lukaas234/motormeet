package com.motormeet.uruguay.domain.repository

import com.motormeet.uruguay.domain.model.Event
import com.motormeet.uruguay.domain.model.EventComment
import kotlinx.coroutines.flow.Flow

interface EventRepository {
    fun getUpcomingEvents(): Flow<List<Event>>
    suspend fun getEventById(eventId: String): Result<Event>
    suspend fun createEvent(event: Event): Result<Unit>
    suspend fun deleteEvent(eventId: String): Result<Unit>
    suspend fun joinEvent(eventId: String, userId: String): Result<Unit>
    suspend fun leaveEvent(eventId: String, userId: String): Result<Unit>
    suspend fun likeEvent(eventId: String, userId: String): Result<Unit>
    suspend fun updateAttendeesCount(eventId: String, delta: Int): Result<Unit>
    suspend fun updateLikesCount(eventId: String, delta: Int): Result<Unit>
    fun getEventComments(eventId: String): Flow<List<EventComment>>
    suspend fun addComment(eventId: String, userId: String, userName: String, content: String): Result<Unit>
    suspend fun uploadEventPhoto(eventId: String, uri: String): Result<String>
}
