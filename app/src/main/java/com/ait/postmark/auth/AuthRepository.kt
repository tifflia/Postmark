package com.ait.postmark.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Thin wrapper over Firebase Auth.
 */
class AuthRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    /** Emits the current user whenever auth state changes (login, logout, token refresh). */
    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await().user!!
    }

    suspend fun register(email: String, password: String, displayName: String): Result<FirebaseUser> = runCatching {
        val user = auth.createUserWithEmailAndPassword(email, password).await().user!!
        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
            this.displayName = displayName
        }
        user.updateProfile(profileUpdates).await()
        user
    }

    fun signOut() = auth.signOut()
}
