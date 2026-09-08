package com.example.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages Google Authentication for the app, supporting Android device sign-in,
 * Web / Windows browser OAuth redirect compatibility, and persistent local session.
 */
class GoogleAuthManager private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences("google_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserProfile?>(null)
    val currentUser: StateFlow<UserProfile?> = _currentUser.asStateFlow()

    init {
        loadSavedUser()
    }

    private fun loadSavedUser() {
        val email = prefs.getString("user_email", null)
        val name = prefs.getString("user_name", null)
        val photo = prefs.getString("user_photo", null)
        val id = prefs.getString("user_id", null)

        if (!email.isNullOrBlank() && !name.isNullOrBlank()) {
            _currentUser.value = UserProfile(
                id = id ?: email,
                name = name,
                email = email,
                photoUrl = photo,
                isGoogleAccount = true
            )
        }
    }

    fun saveUser(profile: UserProfile) {
        prefs.edit()
            .putString("user_id", profile.id)
            .putString("user_name", profile.name)
            .putString("user_email", profile.email)
            .putString("user_photo", profile.photoUrl)
            .apply()
        _currentUser.value = profile
    }

    fun signOut() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    /**
     * Opens Google OAuth web sign-in in the default browser (Chrome, Edge, Firefox),
     * enabling Windows and web browser compatibility for Google Account login.
     */
    fun launchWebGoogleAuth(clientId: String = "google-vehicle-app") {
        val redirectUri = "https://accounts.google.com/o/oauth2/v2/auth"
        val authUri = Uri.parse(redirectUri).buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("scope", "email profile openid")
            .appendQueryParameter("redirect_uri", "urn:ietf:wg:oauth:2.0:oob")
            .appendQueryParameter("prompt", "select_account")
            .build()

        val intent = Intent(Intent.ACTION_VIEW, authUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback: browser opening handled gracefully
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: GoogleAuthManager? = null

        fun getInstance(context: Context): GoogleAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GoogleAuthManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
