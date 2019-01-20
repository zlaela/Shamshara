package net.tribls.shamshara.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import net.tribls.shamshara.R
import net.tribls.shamshara.models.Channel
import net.tribls.shamshara.services.AuthService
import net.tribls.shamshara.services.MessageService
import net.tribls.shamshara.services.UserDataService
import net.tribls.shamshara.utils.BROADCAST_USER_DATA_CHANGED
import net.tribls.shamshara.utils.SOCKET_URL

class MainActivity : AppCompatActivity() {
    private val socket = IO.socket(SOCKET_URL)

    private val userDataChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // When we received the broadcast
            if(AuthService.isLoggedIn) {
                // update the UI elements
                val imageResourceId = resources.getIdentifier(
                    UserDataService.avatarName,
                    "drawable",
                    packageName)

                user_image.setBackgroundColor(UserDataService.getAvatarColor())
                user_image.setImageResource(imageResourceId)
                user_name.text = UserDataService.name
                user_email.text = UserDataService.email
                login.text = getString(R.string.log_out)
            }
        }
    }

    private val channelCreatedListener = Emitter.Listener { args ->
        // The API emits channel.name, channel.description, channel.id.
        // This is performed on a background thread, so update the list view on the main thread
        runOnUiThread {
            val channelName = args[0] as String
            val channelDesc = args[1] as String
            val channelId = args[2] as String

            // Create a channel object
            val newChannel  = Channel(channelName, channelDesc, channelId)

            // Save it to the MessageService array
            MessageService.channels.add(newChannel)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        hideKeyboard()

        // Toggles the drawer
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // Connect the socket
        socket.connect()
        // Listen on the event, using the given listener
        socket.on("channelCreated", channelCreatedListener)
    }

    override fun onResume() {
        // Register a broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            userDataChangedReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGED)
        )
        super.onResume()
    }

    override fun onDestroy() {
        // Disconnect the socket
        socket.disconnect()
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userDataChangedReceiver)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun onGoToLoginClicked(view: View) {
        if(AuthService.isLoggedIn){
            // Log Out
            UserDataService.logOut()
            resetUI()
        } else {
            // Go to the login screen
            startActivity(Intent(this, LoginActivity::class.java))
            // TODO: animations
        }
    }

    fun onAddChannelClicked(view: View) {
        // Only logged in person can add a channel
        if(AuthService.isLoggedIn) {
            val builder = AlertDialog.Builder(this)
            val dialogView = layoutInflater.inflate(R.layout.add_channel_dialog, null)
            builder.setView(dialogView)
                .setPositiveButton("Add") { dialogInterface, i ->
                    hideKeyboard()
                    // When clicked, do something
                    val channelName = dialogView.findViewById<AppCompatEditText>(R.id.add_channel_name).text.toString()
                    val channelDescription = dialogView.findViewById<AppCompatEditText>(R.id.add_channel_description).text.toString()

                    // Create channel with the above name/desc
                    socket.emit("newChannel", channelName, channelDescription)
                }
                .setNegativeButton("Cancel") { dialogInterface, i ->
                    // Cancel and close the dialog
                    hideKeyboard()
                }
                .show()
        }
    }

    fun onSendMessageClicked(view: View) {
        // TODO: send message work here
        Toast.makeText(this, "clicked send message", Toast.LENGTH_SHORT).show()
    }

    // <editor-fold desc="The 3-dot menu">
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }
    //</editor-fold>

    private fun resetUI(){
        user_image.setImageResource(R.drawable.profiledefault)
        user_image.setBackgroundColor(Color.TRANSPARENT)
        user_name.text = ""
        user_email.text = ""
        login.text = getString(R.string.log_in)
    }


    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }
}
