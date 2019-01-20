package net.tribls.shamshara

import android.app.Application
import net.tribls.shamshara.utils.SharedPrefs

/**
 * The application class is initialized before anything else
 * It provides global context. Using to create a single instance of shared prefs
 */
class App : Application() {

    override fun onCreate() {
        // Init shared prefs with the application context.
        // Now shared prefs can be accessed everywhere
        sharedPrefs = SharedPrefs(applicationContext)
        super.onCreate()
    }

    companion object {
        // like a singleton in a specific class
        lateinit var sharedPrefs: SharedPrefs
    }
}