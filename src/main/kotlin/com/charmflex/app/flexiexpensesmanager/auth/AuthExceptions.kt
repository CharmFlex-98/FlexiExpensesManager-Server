package com.charmflex.app.flexiexpensesmanager.auth

import com.charmflex.app.flexiexpensesmanager.core.exceptions.ExceptionBase
import jakarta.servlet.http.HttpServletResponse

object MissingAuthorizationException : ExceptionBase(
    HttpServletResponse.SC_UNAUTHORIZED,
    "AUTH_MISSING_BEARER_TOKEN",
    "Authorization bearer token is required."
)

object InvalidAuthorizationException : ExceptionBase(
    HttpServletResponse.SC_UNAUTHORIZED,
    "AUTH_INVALID_BEARER_TOKEN",
    "Authorization bearer token is invalid."
)

object FirebaseAuthNotConfiguredException : ExceptionBase(
    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
    "AUTH_FIREBASE_NOT_CONFIGURED",
    "Firebase auth is not configured."
)
