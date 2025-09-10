package com.example.moneymaster.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.moneymaster.R
import com.example.moneymaster.auth.LoginActivity

class OnboardingActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var nextButton: Button
    private lateinit var skipButton: Button
    private val onboardingPages = listOf(
        Triple(
            R.drawable.onb1,
            "Track Your Expenses",
            "Keep track of your daily expenses and income with ease"
        ),
        Triple(
            R.drawable.onb2,
            "Set Budgets",
            "Create budgets for different categories and stay within limits"
        ),
        Triple(
            R.drawable.onb3,
            "Financial Insights",
            "Get detailed insights and analytics about your spending habits"
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        indicatorLayout = findViewById(R.id.indicatorLayout)
        nextButton = findViewById(R.id.nextButton)
        skipButton = findViewById(R.id.skipButton)

        setupViewPager()
        setupIndicators()
        setupButtons()
    }

    private fun setupViewPager() {
        viewPager.adapter = object : androidx.viewpager2.adapter.FragmentStateAdapter(this) {
            override fun getItemCount() = onboardingPages.size

            override fun createFragment(position: Int) = OnboardingFragment.newInstance(
                onboardingPages[position].first,
                onboardingPages[position].second,
                onboardingPages[position].third
            )
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateIndicators(position)
                updateButtons(position)
            }
        })
    }

    private fun setupIndicators() {
        val indicators = Array(onboardingPages.size) { ImageView(this) }
        val layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        indicators.forEach { indicator ->
            indicator.setImageDrawable(
                ContextCompat.getDrawable(this, R.drawable.indicator_inactive)
            )
            indicatorLayout.addView(indicator, layoutParams)
        }
        
        // Set initial indicator
        updateIndicators(0)
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val indicator = indicatorLayout.getChildAt(i) as ImageView
            indicator.setImageDrawable(
                ContextCompat.getDrawable(
                    this,
                    if (i == position) R.drawable.indicator_active else R.drawable.indicator_inactive
                )
            )
        }
    }

    private fun setupButtons() {
        skipButton.setOnClickListener {
            navigateToLogin()
        }

        nextButton.setOnClickListener {
            if (viewPager.currentItem == onboardingPages.size - 1) {
                navigateToLogin()
            } else {
                viewPager.currentItem++
            }
        }
    }

    private fun updateButtons(position: Int) {
        if (position == onboardingPages.size - 1) {
            nextButton.text = "Get Started"
            skipButton.visibility = android.view.View.GONE
        } else {
            nextButton.text = "Next"
            skipButton.visibility = android.view.View.VISIBLE
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
} 