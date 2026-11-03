package com.example.spotifay.data.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.spotifay.data.auth.AuthConfig.AUTHORIZATION_ENDPOINT
import com.example.spotifay.data.auth.AuthConfig.CLIENT_ID
import com.example.spotifay.data.auth.AuthConfig.REDIRECT_URI
import com.example.spotifay.data.auth.AuthConfig.SCOPES
import com.example.spotifay.data.auth.AuthConfig.TOKEN_ENDPOINT
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import org.json.JSONObject
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Owns the OAuth 2.0 + PKCE session with Spotify.
 *
 * AppAuth handles PKCE (code verifier/challenge), the custom-tab bounce and
 * refresh-token exchange; this class persists the serialised [AuthState] and
 * exposes it as a [StateFlow] for the UI.
 */
class SpotifyAuth(context: Context) {

    private val appContext = context.applicationContext
    private val service = AuthorizationService(appContext)
    private val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(restore())
    val state: StateFlow<AuthState?> = _state.asStateFlow()

    val isSignedIn: Boolean get() = _state.value?.accessToken != null

    private val configuration = AuthorizationServiceConfiguration(
        Uri.parse(AUTHORIZATION_ENDPOINT),
        Uri.parse(TOKEN_ENDPOINT),
    )

    /** Intent that opens the Spotify consent screen in a Custom Tab. */
    fun loginIntent(): Intent {
        val request = AuthorizationRequest.Builder(
            configuration,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI),
        )
            .setScopes(SCOPES)
            .build()

        return service.getAuthorizationRequestIntent(request)
    }

    /**
     * Completes the flow. [resultCode] / [data] come straight from the
     * activity result of [loginIntent].
     */
    fun handleAuthResponse(
        resultCode: Int,
        data: Intent?,
        onResult: (Throwable?) -> Unit,
    ) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            onResult(CancellationExceptionMarker())
            return
        }

        val exception = AuthorizationException.fromIntent(data)
        val response = AuthorizationResponse.fromIntent(data)

        if (response == null) {
            onResult(exception ?: IllegalStateException("No authorization response"))
            return
        }

        service.performTokenRequest(response.createTokenExchangeRequest()) { tokenResponse, tokenError ->
            if (tokenResponse == null) {
                onResult(tokenError ?: IllegalStateException("Token exchange failed"))
                return@performTokenRequest
            }
            persist(AuthState(response, tokenResponse))
            onResult(null)
        }
    }

    /** Serialises a fresh access token, refreshing first when it has expired. */
    fun validAccessTokenBlocking(timeoutMs: Long = 20_000L): String? {
        val current = _state.value ?: return null
        val token = current.accessToken
        if (token != null && !current.needsTokenRefresh) return token

        val latch = CountDownLatch(1)
        @Volatile var refreshed: String? = null

        current.performActionWithFreshTokens(service) { accessToken, _, exception ->
            if (exception == null && accessToken != null) {
                refreshed = accessToken
                persist(current)
            }
            latch.countDown()
        }

        latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        return refreshed ?: current.accessToken
    }

    fun signOut() {
        _state.value?.let { service.performRevokeAccess(it, null) }
        prefs.edit().remove(KEY_STATE).apply()
        _state.value = null
    }

    private fun persist(state: AuthState) {
        prefs.edit().putString(KEY_STATE, state.jsonSerializeString()).apply()
        _state.value = state
    }

    private fun restore(): AuthState? {
        val raw = prefs.getString(KEY_STATE, null) ?: return null
        return runCatching { AuthState.jsonDeserialize(JSONObject(raw)) }.getOrNull()
    }

    /** Thrown when the user backs out of the consent screen. */
    class CancellationExceptionMarker : Exception("Sign-in cancelled")

    private companion object {
        const val PREFS = "spotifay_auth"
        const val KEY_STATE = "auth_state"
    }
}