package com.motormeet.uruguay.domain.repository

interface AuthRepository {
    suspend fun login(email: String, pass: String): Unit
    suspend fun signUp(email: String, pass: String, name: String): Unit
    suspend fun resetPassword(email: String): Unit
    suspend fun signOut(): Unit
}
