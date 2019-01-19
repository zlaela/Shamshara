package net.tribls.shamshara

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
    }

    fun onLoginClicked(view: View) {
        // TODO: login here
        Toast.makeText(this, "clicked login", Toast.LENGTH_SHORT).show()
    }

    fun onCreateAccountClicked(view: View) {
        startActivity(Intent(this, CreateAccountActivity::class.java))
        // TODO: animations
        finish()
    }
}