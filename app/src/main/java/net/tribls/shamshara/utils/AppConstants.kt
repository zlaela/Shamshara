package net.tribls.shamshara.utils


// The base URL for the Heroku API endpoint
const val BASE_URL = "https://shamshara.herokuapp.com/v1/"
const val SOCKET_URL = "https://shamshara.herokuapp.com/"
const val URL_REGISTER = "${BASE_URL}account/register"
const val URL_LOGIN = "${BASE_URL}account/login"
const val URL_CREATE_USER = "${BASE_URL}user/add"
const val URL_GET_USER = "${BASE_URL}user/byEmail/"
const val URL_GET_CHANNELS = "${BASE_URL}channel/"



// Braodcast constants
const val BROADCAST_USER_DATA_CHANGED = "BROADCAST_USER_DATA_CHANGED"
