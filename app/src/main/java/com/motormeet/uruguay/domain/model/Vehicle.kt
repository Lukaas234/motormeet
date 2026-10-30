package com.motormeet.uruguay.domain.model

data class Vehicle(
    val id: String = "",
    val ownerId: String = "",
    val brand: String = "",
    val model: String = "",
    val year: Int = 2024,
    val engine: String = "",
    val power: String = "",
    val transmission: String = "",
    val color: String = "",
    val description: String = "",
    val modifications: String = "",
    val photoUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isPublic: Boolean = true
)
