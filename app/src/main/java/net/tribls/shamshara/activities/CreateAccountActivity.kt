package net.tribls.shamshara.activities

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_account.*
import net.tribls.shamshara.R
import net.tribls.shamshara.services.AuthService
import java.util.*

class CreateAccountActivity : AppCompatActivity() {

    var userAvatar = "profileDefault"
    var avatarColor = "[0.5, 0.5, 0.5, 1]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
    }

    fun onMakeAvatarClicked(view: View) {
        getRandomImage()
    }

    fun onGenerateBackgroundColorClicked(view: View) {
        getRandomColor()
    }

    fun onSignUpClicked(view: View) {
        // Get the username and password from the edittexts
        val username = username_text_field.text.toString()
        val email = email_text_field.text.toString()
        val password = password_text_field.text.toString()

        // Create the user account
        AuthService.registerUser(this, email, password){ complete->
            // If created successfully, log in the user
            if(complete){
                Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                AuthService.loginUser(this, email, password) { loginSuccess->
                    if(loginSuccess) {
                        Toast.makeText(this, "Log In completed successfully", Toast.LENGTH_SHORT).show()
                        // Add the user with its avatar and background color
                        AuthService.createUser(this, username, email, userAvatar, avatarColor){ createSuccess ->
                            if(createSuccess){
                                Toast.makeText(this, "User added successfully", Toast.LENGTH_SHORT).show()
                                // Finish this activity
                                finish()
                            } else {

                                Toast.makeText(this, "Failed to create user", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        // TODO: handle errors
                        Toast.makeText(this, "Log In failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                // TODO: handle errors
                Toast.makeText(this, "Failed to create account", Toast.LENGTH_SHORT).show()
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
}
