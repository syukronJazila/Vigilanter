package com.belajar.vigilanter

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class SplachScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splach_screen)
        Handler().postDelayed({
            val intent =
                Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }, 1000)
    }
}