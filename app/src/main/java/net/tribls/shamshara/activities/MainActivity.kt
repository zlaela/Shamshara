package net.tribls.shamshara.activities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatEditText
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import io.socket.client.IO
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.nav_header_main.*
import net.tribls.shamshara.App
import net.tribls.shamshara.R
import net.tribls.shamshara.adapter.MessagesAdapter
import net.tribls.shamshara.models.Channel
import net.tribls.shamshara.models.Message
import net.tribls.shamshara.services.AuthService
import net.tribls.shamshara.services.MessageService
import net.tribls.shamshara.services.UserDataService
import net.tribls.shamshara.utils.BROADCAST_USER_DATA_CHANGED
import net.tribls.shamshara.utils.SOCKET_URL

class MainActivity : AppCompatActivity() {
    private lateinit var channelAdapter: ArrayAdapter<Channel>
    private lateinit var messageAdapter: MessagesAdapter
    private val socket = IO.socket(SOCKET_URL)

    private var selectedChannel: Channel? = null

    // Listeners and Receivers
    private val userDataChangedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            // When we received the broadcast
            if(App.sharedPrefs.isLoggedIn) {
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

                // Once the user is logged in, get the list of channels
                MessageService.getChannels { success ->
                    if(success) {
                        // If we have channels, default to the first one
                        if(MessageService.channels.size > 0) {
                            selectedChannel = MessageService.channels[0]
                            // Tell the adapter that the data set changed so it repopulates
                            channelAdapter.notifyDataSetChanged()
                            // Update the UI with the channel name
                            updateWithSelectedChannel()
                        }
                    }
                }
            }
        }
    }
    private val channelCreatedListener = Emitter.Listener { args ->
        // The API emits channel.name, channel.description, channel.id.
        // This is performed on a background thread, so update the list view on the main thread
        if(App.sharedPrefs.isLoggedIn){
            runOnUiThread {
                val channelName = args[0] as String
                val channelDesc = args[1] as String
                val channelId = args[2] as String

                // Create a channel object
                val newChannel  = Channel(channelName, channelDesc, channelId)
                // Save it to the MessageService array
                MessageService.channels.add(newChannel)
                // Tell the adapter that a new channel was added so it can re-populate
                channelAdapter.notifyDataSetChanged()
            }
        }
    }
    private val messageCreatedListener = Emitter.Listener { args ->

        if(App.sharedPrefs.isLoggedIn){
            // Run on the main thread
            runOnUiThread {
                // Extract from the args the values that we need
                // we get back an object with in "messageCreated" with messageBody, userId, channelId, userName, userAvatar, userAvatarColor, id, timeStamp
                val channelId = args[2] as String
                // Check if the message belongs in the channel we're on
                if(selectedChannel?.id == channelId) {
                    val messageBody = args[0] as String
                    // Skip userId since we're not using it
                    val userName = args[3] as String
                    val useAvatar = args[4] as String
                    val userAvatarColor = args[5] as String
                    val id = args[6] as String
                    val timeStamp = args[7] as String

                    // Create a new message object
                    val message = Message(messageBody, userName, channelId, useAvatar, userAvatarColor, id, timeStamp)
                    // Save it to the messages array in MessageServices
                    MessageService.messages.add(message)
                    // Tell the adapter that we have messages
                    messageAdapter.notifyDataSetChanged()
                    // Scroll to the end
                    if(messageAdapter.itemCount > 0) {
                        messages_recycler_view.smoothScrollToPosition(messageAdapter.itemCount - 1)
                    }
                }
            }
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

        // setup the adapter for the list of channels
        setupAdapter()
        // Connect the socket
        socket.connect()
        // Listen on the event, using the given listener
        socket.on("channelCreated", channelCreatedListener)
        socket.on("messageCreated", messageCreatedListener)
        // Register a broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(
            userDataChangedReceiver,
            IntentFilter(BROADCAST_USER_DATA_CHANGED)
        )
        // If we're logged in, go ahead and fetch the information
        if(App.sharedPrefs.isLoggedIn) {
            AuthService.findUserByEmail(this){
                //No-op
            }
        }
    }

    override fun onResume() {
        super.onResume()
        channel_list.setOnItemClickListener { _, _, i, _ ->
            // Set the selected channel to the one clicked
           selectedChannel = MessageService.channels[i]
            // Close the drawer
            drawer_layout.closeDrawer(GravityCompat.START)
            // Update the UI
            updateWithSelectedChannel()
        }
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
        if(App.sharedPrefs.isLoggedIn){
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
        if(App.sharedPrefs.isLoggedIn) {
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
        // check we're logged in, check that there's text in th message box
        if(App.sharedPrefs.isLoggedIn && message_text_field.text.isNotEmpty()) {
            selectedChannel?.let { channel ->
                val userId = UserDataService.id
                val channelId = channel.id

                // The API expects in "newMessage", messageBody, userId, channelId, userName, userAvatar, userAvatarColor
                socket.emit("newMessage",
                    message_text_field.text.toString(),
                    userId,
                    channelId,
                    UserDataService.name,
                    UserDataService.avatarName,
                    UserDataService.avatarColor)
                // Clear the text field
                message_text_field.text.clear()
                // Hide the keyboard
                hideKeyboard()
            }
        }
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
        // clear the adapters
        channelAdapter.notifyDataSetChanged()
        messageAdapter.notifyDataSetChanged()
        user_image.setImageResource(R.drawable.profiledefault)
        user_image.setBackgroundColor(Color.TRANSPARENT)
        user_name.text = getString(R.string.log_in)
        user_email.text = ""
        current_channel_name.text = getString(R.string.log_in)
        login.text = getString(R.string.log_in)
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    // Create an adapter for the list of channels we get from the db
    private fun setupAdapter() {
        channelAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, MessageService.channels)
        channel_list.adapter = channelAdapter

        messageAdapter = MessagesAdapter(this, MessageService.messages)
        messages_recycler_view.adapter = messageAdapter
        messages_recycler_view.layoutManager = LinearLayoutManager(this)
    }

    // Function called whenever we select a channel from the populated list of channels
    private fun updateWithSelectedChannel() {
        current_channel_name.text = String.format(getString(R.string.channel_name_format), selectedChannel?.name)
        // Download messages for the channel we're viewing
        selectedChannel?.let {  channel ->
            MessageService.getMessages(channel.id) { complete ->
                if(complete) {
                    // Tell the adapter that we have messages
                    messageAdapter.notifyDataSetChanged()
                    // Scroll to the end
                    if(messageAdapter.itemCount > 0){
                        messages_recycler_view.smoothScrollToPosition(messageAdapter.itemCount -1)
                    }
                }
            }
        }
    }
}
