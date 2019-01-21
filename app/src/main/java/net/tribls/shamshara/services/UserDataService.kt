package net.tribls.shamshara.services

import android.graphics.Color
import net.tribls.shamshara.App

object UserDataService {
    var id = ""
    var name = ""
    var email = ""
    var avatarColor = ""
    var avatarName = ""

    fun logOut() {
        id = ""
        name = ""
        email = ""
        avatarColor = ""
        avatarName = ""

        // Save to shared preferences
        App.sharedPrefs.authToken = ""
        App.sharedPrefs.userEmail = ""
        App.sharedPrefs.isLoggedIn = false

        // Clear the messages and channels
        MessageService.clearMessages()
        MessageService.clearChannels()
    }

    fun getAvatarColor(): Int {
        val array = mutableListOf<Int>()
        // "avatarColor":"[0.1411764705882353, 0.7647058823529411, 0.054901960784313725]"
        avatarColor
            .replace("[", "")
            .replace("]", "")
            .split(",")
            .forEach {
                val floatColor = it.toFloat()*255
                array.add(floatColor.toInt())
            }
        return Color.rgb(array[0], array[1], array[2])
    }
}