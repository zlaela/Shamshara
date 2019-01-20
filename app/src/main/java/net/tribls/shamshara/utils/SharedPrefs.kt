package net.tribls.shamshara.utils

import android.content.Context
import android.content.SharedPreferences
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley

/**
 * Save key-value pairs to the device
 */
class SharedPrefs(context: Context) {
    // Make a global reference to shared preferences
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences(PREFS, 0) // 0 = private
    // Make a global request queue for all web requests
    val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    // Get and Set each preference
    var isLoggedIn: Boolean
        get() = sharedPrefs.getBoolean(IS_LOGGED_IN, false)
        set(value) = sharedPrefs.edit().putBoolean(IS_LOGGED_IN, value).apply()

    var authToken: String
        get() = sharedPrefs.getString(AUTH_TOKEN, "")?: ""
        set(value) = sharedPrefs.edit().putString(AUTH_TOKEN, value).apply()

    var userEmail: String
        get() = sharedPrefs.getString(USER_EMAIL, "")?: ""
        set(value) = sharedPrefs.edit().putString(USER_EMAIL, value).apply()

    companion object {
        private const val PREFS = "prefs"
        private const val IS_LOGGED_IN = "isLoggedIn"
        private const val AUTH_TOKEN = "authToken"
        private const val USER_EMAIL = "userEmail"
    }
}