package net.tribls.shamshara.services

import android.content.Context
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import net.tribls.shamshara.models.Channel
import net.tribls.shamshara.utils.URL_GET_CHANNELS
import org.json.JSONException

/**
 * This service's purpose is to download the channels, the messages
 * and to store the list of channels
 */
object MessageService {
    val TAG = MessageService::class.java.canonicalName?: "MessageService"

    // init empty list of channels
    val channels = ArrayList<Channel>()

    // This is a GET request, so no reason for a request body
    fun getChannels(context: Context, complete: (Boolean) -> Unit) {
        val getChannelsRequest = object : JsonArrayRequest(
            Method.GET,
            URL_GET_CHANNELS,
            null, // no JSON object to pass in
            Response.Listener { responseArray ->

                // Getting an array of Objects that we'll iterate through.
                // JSON objects can throw exceptions, so we try/catch
                try {
                    // Each object in the array has an id, name, and description
                    // Create a channel out of each object
                    for(i in 0 until responseArray.length()){
                        val channel = responseArray.getJSONObject(i)
                        val id = channel.getString("_id")
                        val name = channel.getString("name")
                        val description = channel.getString("description")

                        // Add each channel to the list of channels in the MessageService
                        MessageService.channels.add(
                            Channel(name, description, id)
                        )
                    }
                    complete(true)
                } catch (exception: JSONException) {
                    //TODO: handle this exception
                    Log.d(TAG, "JSON Exception: ${exception.localizedMessage}")
                    complete(false)
                }
            }, Response.ErrorListener {  error ->
                Log.d(TAG, "Could not get channels. \n$error")
                complete(false)
            }
        ) {
            override fun getBodyContentType(): String {
                return "application/json; charset=utf-8"
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["authorization"] = "Bearer ${AuthService.authToken}"

                return headers
            }
        }

        // Add the request to the request queue
        Volley.newRequestQueue(context).add(getChannelsRequest)
    }
}
