package com.charmflex.app.flexiexpensesmanager.auth

data class AuthenticatedUser(
    val remoteUserId: String,
    val displayName: String,
    val email: String?,
)
