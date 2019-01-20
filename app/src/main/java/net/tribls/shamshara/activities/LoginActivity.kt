package net.tribls.shamshara.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_create_account.*
import net.tribls.shamshara.R
import net.tribls.shamshara.services.AuthService

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onLoginClicked(view: View) {
        // Get the username and password from the edittexts
        val email = email_text_field.text.toString()
        val password = password_text_field.text.toString()

        // Log in the user
        AuthService.loginUser(this, email, password) { loginSuccess ->
            if (loginSuccess) {
                Toast.makeText(this, "Log In completed successfully", Toast.LENGTH_SHORT).show()
            } else {
                // TODO: handle errors
                Toast.makeText(this, "Log In failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onCreateAccountClicked(view: View) {
        startActivity(Intent(this, CreateAccountActivity::class.java))
        // TODO: animations
        finish()
    }
}