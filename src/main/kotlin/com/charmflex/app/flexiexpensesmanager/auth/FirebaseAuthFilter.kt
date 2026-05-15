package com.charmflex.app.flexiexpensesmanager.auth

import com.charmflex.app.flexiexpensesmanager.core.exceptions.ExceptionBase
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseAuthFilter(
    private val firebaseTokenVerifier: FirebaseTokenVerifier,
    private val objectMapper: ObjectMapper,
) : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return !path.startsWith("/api/v1/split-groups") &&
            path != "/api/v1/devices/notification-token"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization") ?: throw MissingAuthorizationException
            if (!authHeader.startsWith("Bearer ")) throw MissingAuthorizationException
            val token = authHeader.removePrefix("Bearer ").trim()
            if (token.isBlank()) throw MissingAuthorizationException
            request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, firebaseTokenVerifier.verify(token))
            filterChain.doFilter(request, response)
        } catch (exception: ExceptionBase) {
            response.status = exception.statusCode
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.writer.write(
                objectMapper.writeValueAsString(
                    mapOf(
                        "errorCode" to exception.errorCode,
                        "errorMessage" to exception.errorMessage
                    )
                )
            )
        }
    }

    companion object {
        const val AUTHENTICATED_USER_ATTRIBUTE = "authenticatedUser"
    }
}
