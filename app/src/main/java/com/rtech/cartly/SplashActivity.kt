package com.rtech.cartly

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.rtech.cartly.data.UserProvider

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler = Handler(Looper.getMainLooper())
        val startTime = System.currentTimeMillis()

        UserProvider.ensureSignedIn {
            val elapsed = System.currentTimeMillis() - startTime
            val remaining = (2000 - elapsed).coerceAtLeast(0)
            handler.postDelayed({
                startActivity(Intent(this, ContainerActivity::class.java))
                finish()
            }, remaining)
        }
    }
}