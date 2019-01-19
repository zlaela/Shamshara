package net.tribls.shamshara

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_account.*
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
        Toast.makeText(this, "clicked sign up", Toast.LENGTH_SHORT).show()
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
