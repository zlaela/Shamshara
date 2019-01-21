package net.tribls.shamshara.models

class Message(
    val messageBody: String,
    val userName: String,
    val channelId: String,
    val userAvatar: String,
    val userAvatarColor: String,
    val id: String,
    val timeStamp: String
)