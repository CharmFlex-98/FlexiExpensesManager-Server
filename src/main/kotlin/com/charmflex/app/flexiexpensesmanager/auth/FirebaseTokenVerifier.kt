package com.charmflex.app.flexiexpensesmanager.auth

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import java.io.FileInputStream

@Service
class FirebaseTokenVerifier(
    @Value("\${firebase.service-account-json:}") private val serviceAccountJson: String,
    @Value("\${firebase.service-account-path:}") private val serviceAccountPath: String,
) {
    private val firebaseAuth: FirebaseAuth? by lazy { initFirebaseAuth() }

    fun verify(idToken: String): AuthenticatedUser {
        val auth = firebaseAuth ?: throw FirebaseAuthNotConfiguredException
        val token = runCatching { auth.verifyIdToken(idToken) }.getOrElse {
            throw InvalidAuthorizationException
        }
        val uid = token.uid
        if (uid.isBlank()) throw InvalidAuthorizationException
        val displayName = token.name?.takeIf { it.isNotBlank() }
            ?: token.email?.takeIf { it.isNotBlank() }
            ?: uid
        return AuthenticatedUser(
            remoteUserId = uid,
            displayName = displayName,
            email = token.email
        )
    }

    private fun initFirebaseAuth(): FirebaseAuth? {
        val credentials = runCatching {
            when {
                serviceAccountJson.isNotBlank() -> GoogleCredentials.fromStream(
                    ByteArrayInputStream(serviceAccountJson.toByteArray())
                )
                serviceAccountPath.isNotBlank() -> GoogleCredentials.fromStream(FileInputStream(serviceAccountPath))
                else -> null
            }
        }.getOrNull() ?: return null

        return runCatching {
            val app = FirebaseApp.getApps().firstOrNull()
                ?: FirebaseApp.initializeApp(FirebaseOptions.builder().setCredentials(credentials).build())
            FirebaseAuth.getInstance(app)
        }.getOrNull()
    }
}
