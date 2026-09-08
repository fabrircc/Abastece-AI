package com.example.model

data class UserProfile(
    val id: String,
    val name: String,
    val email: String,
    val photoUrl: String? = null,
    val isGoogleAccount: Boolean = true,
    val loggedAt: Long = System.currentTimeMillis()
)
