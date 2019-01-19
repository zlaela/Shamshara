package net.tribls.shamshara.services

import android.content.Context
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import net.tribls.shamshara.utils.URL_REGISTER
import org.json.JSONObject

/**
 * A Singleton that handles authorization
 */
object AuthService {

    val TAG = AuthService::class.java.canonicalName?: "AuthService"

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
            Response.Listener { response->
                // Success listener
                println(response)
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
}