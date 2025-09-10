package com.example.moneymaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.moneymaster.auth.AuthManager
import com.example.moneymaster.auth.LoginActivity
import com.example.moneymaster.onboarding.OnboardingActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Hide the action bar if it exists
        supportActionBar?.hide()

        // Create a handler to delay the transition
        Handler(Looper.getMainLooper()).postDelayed({
            if (AuthManager.isLoggedIn(this)) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, OnboardingActivity::class.java))
            }
            finish()
        }, 3000) // 3000 milliseconds = 3 seconds
    }
} 