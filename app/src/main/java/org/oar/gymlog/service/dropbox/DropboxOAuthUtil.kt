package org.oar.gymlog.service.dropbox

import android.content.Context
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.android.Auth
import org.oar.gymlog.util.Constants.Dropbox.APP_KEY
import org.oar.gymlog.util.extensions.PreferencesExts.save

class DropboxOAuthUtil(
    private val context: Context
) {
    var isAwaitingResult: Boolean = false

    /**
     * Starts the Dropbox OAuth process by launching the Dropbox official app or web
     * browser if dropbox official app is not available. In browser flow, normally user needs to
     * sign in.
     *
     * Because mobile apps need to keep Dropbox secrets in their binaries we need to use PKCE.
     * Read more about this here: https://dropbox.tech/developers/pkce--what-and-why-
     **/
    fun startDropboxAuthorization2PKCE(context: Context) {
        val requestConfig = DbxRequestConfig("db-${APP_KEY}")

        // The scope's your app will need from Dropbox
        // Read more about Scopes here: https://developers.dropbox.com/oauth-guide#dropbox-api-permissions
        val scopes = listOf(
            "account_info.read",
            "files.content.write",
            "files.content.read",
            "sharing.read"
        )
        Auth.startOAuth2PKCE(context, APP_KEY, requestConfig, scopes)
        isAwaitingResult = true
    }

    /**
     * Starts the Dropbox OAuth process by launching the Dropbox official app or web
     * browser if dropbox official app is not available. In browser flow, normally user needs to
     * sign in.
     *
     * Because mobile apps need to keep Dropbox secrets in their binaries we need to use PKCE.
     * Read more about this here: https://dropbox.tech/developers/pkce--what-and-why-
     **/
    fun startDropboxAuthorizationOAuth2(context: Context) {
        Auth.startOAuth2Authentication(context, APP_KEY)
        isAwaitingResult = true
    }

    /**
     * Call this from onResume() in the activity you are awaiting an Auth Result
     *
     * Returns true if an auth process did start
     */
    fun retrieveAndSaveDbxCredential(): Boolean {
        if (isAwaitingResult) {
            val authDbxCredential = Auth.getDbxCredential() //fetch the result from the AuthActivity
            isAwaitingResult = false
            if (authDbxCredential != null) {
                context.save(authDbxCredential)
            }
            return true
        }
        return false
    }
}