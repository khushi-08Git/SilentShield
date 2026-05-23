package com.example.silentshield.presentation.onboarding

import androidx.lifecycle.ViewModel
import com.example.silentshield.R

class OnboardingViewModel : ViewModel() {

    val pages = listOf(
        OnboardingPage(
            "Detect Scam Calls",
            "Get alerts before answering risky numbers.",
            R.drawable.onboarding_img1
        ),
        OnboardingPage(
            "Fraud SMS Protection",
            "We scan suspicious messages instantly.",
            R.drawable.onboarding_img2        ),
        OnboardingPage(
            "Safe Link Scanner",
            "Avoid phishing links and fake websites.",
            R.drawable.onboarding_img3        )
    )
}