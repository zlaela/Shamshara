package net.tribls.shamshara.activities

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.LocalBroadcastManager
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_account.*
import net.tribls.shamshara.R
import net.tribls.shamshara.services.AuthService
import net.tribls.shamshara.utils.BROADCAST_USER_DATA_CHANGED
import java.util.*

class CreateAccountActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        spinner.visibility = View.GONE
    }

    fun onMakeAvatarClicked(view: View) {
        getRandomImage()
    }

    fun onGenerateBackgroundColorClicked(view: View) {
        getRandomColor()
    }

    fun onSignUpClicked(view: View) {
        showSpinner(true)
        hideKeyboard()

        // TODO: Text validation
        // Get the username and password from the edittexts
        val username = username_text_field.text.toString()
        val email = email_text_field.text.toString()
        val password = password_text_field.text.toString()

        // Create the user account
        AuthService.registerUser(this, email, password){ complete->
            // If created successfully, log in the user
            if(complete){
                AuthService.loginUser(this, email, password) { loginSuccess->
                    if(loginSuccess) {
                        // Add the user with its avatar and background color
                        AuthService.createUser(this, username, email, userAvatar, avatarColor){ createSuccess ->
                            if(createSuccess){

                                // Tell the main activity that we have successfully logged in
                                // Use Local Broadcasts (Local Broadcast Manager)
                                val userDataChanged = Intent(BROADCAST_USER_DATA_CHANGED)
                                LocalBroadcastManager.getInstance(this).sendBroadcast(userDataChanged)

                                showSpinner(false)
                                // Finish this activity
                                finish()
                            } else {
                                // TODO: handle errors
//                                Toast.makeText(this, "Failed to create user", Toast.LENGTH_LONG).show()
                                makeToast()
                            }
                        }
                    } else {
                        // TODO: handle errors
//                        Toast.makeText(this, "Log In failed", Toast.LENGTH_SHORT).show()
                        makeToast()
                    }
                }
            } else {
                // TODO: handle errors
//                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
                makeToast()
            }
        }
    }

    private fun getRandomImage() {
        val random = Random()
        // 0-2 EXCLUSIVE, so 0-1
        val color = random.nextInt(2)
        // 0-28 EXCLUSIVE, we have images numbered 0-27
        val avatar = random.nextInt(28)

        // Generate the avatar name by concatenating a random number to either light/dark
        userAvatar = if(color == 0){
            "light$avatar"
        } else {
            "dark$avatar"
        }

        // Select the avatar from our drawables by its identifier
        // The identifier is [userAvatar] we generated earlier
        val resourceId = resources.getIdentifier(userAvatar, "drawable", packageName)
        avatar_image.setImageResource(resourceId)
    }

    private fun getRandomColor() {
        // Generate 3 random values for RGB, and construct a color
        val random = Random()
        val r = random.nextInt(256)
        val g = random.nextInt(256)
        val b = random.nextInt(256)

        // Set the background image
        avatar_image.setBackgroundColor(Color.rgb(r,g,b))

        // iOS uses 0-1 values, so convert to a usable value for the API
        avatarColor = "[${r.toDouble()/255}, ${g.toDouble()/255}, ${b.toDouble()/255}]"
    }

    private fun showSpinner(showSpinner: Boolean){
        // Show/hide the spinner
        spinner.visibility = if(showSpinner) View.VISIBLE else View.GONE

        // Spinner visible? Disable buttons. And vice versa
        avatar_image.isEnabled = !showSpinner
        generate_background_color.isEnabled = !showSpinner
        sign_up.isEnabled = !showSpinner
    }

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if(inputManager.isAcceptingText) {
            inputManager.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    private fun makeToast(){
        showSpinner(false)
        Toast.makeText(this, "Something went wrong. Please try again later.", Toast.LENGTH_SHORT).show()
    }
}
