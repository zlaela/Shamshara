package net.tribls.shamshara.services

import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.Response.Listener
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.tribls.shamshara.utils.*
import org.json.JSONException
import org.json.JSONObject

/**
 * A Singleton that handles authorization
 */
object AuthService {
    val TAG = AuthService::class.java.canonicalName?: "AuthService"

    var isLoggedIn = false
    var authToken = ""
    var userEmail = ""

    // Context for the Volley web request
    // Email and Password
    // Completion handler (function literal that takes a Boolean as a completion parameter) and returns nothing (Unit)
    fun registerUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        // Create the JSON body
        // Object passing with the web request for the API
        // API checks it for the expected parameters (email, password)
        val jsonBody = JSONObject()
        // Put in the key-value parameters
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        // The Volley request sends a ByteArray, so convert to String
        val requestBody = jsonBody.toString()

        // The web request expects a String response from the specified URL, so use a StringRequest
        // Method type is POST
        // Listen for response (error, and otherwise)
        val registerRequest = object : StringRequest(
            Request.Method.POST,
            URL_REGISTER,
            Listener { response->
                // Success listener
                println("lailaaaa... response is $response")
                complete(true)
            }, Response.ErrorListener { error->
                // Error listener
                Log.d(TAG, "Could not register user: $error")
                complete(false)
            }) {

            // Specify the body content type
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            // Add the JSON body to the request
            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(registerRequest)
    }

    // Log in an already registered user, or prompt to sign up
    fun loginUser(context: Context, email: String, password: String, complete: (Boolean) -> Unit) {
        // Create the JSON body
        // Object passing with the web request for the API
        // API checks it for the expected parameters (email, password)
        val jsonBody = JSONObject()
        // Put in the key-value parameters
        jsonBody.put("email", email)
        jsonBody.put("password", password)

        // The Volley request sends a ByteArray, so convert to String
        val requestBody = jsonBody.toString()

        // The web request expects a String response from the specified URL, so use a StringRequest
        // Method type is POST
        // Listen for response (error, and otherwise)
        val loginRequest = object : JsonObjectRequest(
            Request.Method.POST,
            URL_LOGIN,
            null,
            Listener { response->
                // Success listener
                println("lailaaaa... response is $response")
                // If there are no matching maps, catch the exception
                try {
                    authToken = response.getString("token")
                    userEmail = response.getString("user")
                    isLoggedIn = true
                    complete(true)
                } catch (exception: JSONException) {
                    //TODO: handle this exception
                    Log.d(TAG, "Exception: ${exception.localizedMessage}")
                    complete(false)
                }
            }, Response.ErrorListener { error->
                // Error listener
                Log.d(TAG, "Could not log in user: $error")
                complete(false)
            }) {

            // Specify the body content type
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            // Add the JSON body to the request
            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(loginRequest)
    }


    // Create a user
    fun createUser(context: Context, name: String, email: String, avatarName: String, avatarColor: String, complete: (Boolean) -> Unit) {
        // Create the JSON body
        // Object passing with the web request for the API
        // API checks it for the expected parameters (name, email, avatarColor, avatarName)
        val jsonBody = JSONObject()
        // Put in the key-value parameters
        jsonBody.put("name", name)
        jsonBody.put("email", email)
        jsonBody.put("avatarName", avatarName)
        jsonBody.put("avatarColor", avatarColor)

        // The Volley request sends a ByteArray, so convert to String
        val requestBody = jsonBody.toString()

        // The web request expects a String response from the specified URL, so use a StringRequest
        // Method type is POST
        // Listen for response (error, and otherwise)
        val addUserRequest = object : JsonObjectRequest(
            Request.Method.POST,
            URL_CREATE_USER,
            null,
            Listener { response->
                // Success listener
                println("lailaaaa... response is $response")

                // Add the values to the data user service
                // If there are no matching maps, catch the exception
                try {
                    UserDataService.id = response.getString("_id")
                    UserDataService.name = response.getString("name")
                    UserDataService.email = response.getString("email")
                    UserDataService.avatarColor = response.getString("avatarColor")
                    UserDataService.avatarName = response.getString("avatarName")

                    complete(true)
                } catch (exception: JSONException) {
                    //TODO: handle this exception
                    Log.d(TAG, "Exception: ${exception.localizedMessage}")
                    complete(false)
                }
            }, Response.ErrorListener { error->
                // Error listener
                Log.d(TAG, "Could not log in user: $error")
                complete(false)
            }) {

            // Specify the body content type
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            // Add the JSON body to the request
            override fun getBody(): ByteArray {
                return requestBody.toByteArray()
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                // To match
                headers["Authorization"] = "Bearer $authToken"
                return headers
            }
        }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(addUserRequest)
    }

    // This only takes a context - only a GET request
    fun findUserByEmail(context: Context, complete: (Boolean) -> Unit) {
        // No JSON body required - go straight to the request
        val findUserRequest = object: JsonObjectRequest(
            Method.GET,
            "$URL_GET_USER$userEmail",
            null,
            Listener { response ->
                try {
                    // Extract the user's information from the response and add it to the service
                    UserDataService.id = response.getString("_id")
                    UserDataService.name = response.getString("name")
                    UserDataService.email = response.getString("email")
                    UserDataService.avatarColor = response.getString("avatarColor")
                    UserDataService.avatarName = response.getString("avatarName")

                    // Tell anyone listening that the user's data has changed
                    val userDataChange = Intent(BROADCAST_USER_DATA_CHANGED)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(userDataChange)
                    complete(true)
                } catch (e: JSONException) {
                    Log.d("JSON", "EXC: " + e.localizedMessage)
                }

            }, Response.ErrorListener { error ->
                Log.d("ERROR", "Could not find user.")
                complete(false)
            }) {

                override fun getBodyContentType(): String {
                    return "application/json; charset=utf-8"
                }

                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $authToken"
                    return headers
                }
            }

        // Add the request to the RequestQueue.
        Volley.newRequestQueue(context).add(findUserRequest)
    }
}