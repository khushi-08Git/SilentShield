package com.example.silentshield.presentation.onboarding


import androidx.annotation.DrawableRes

data class OnboardingPage(
    val title: String,
    val desc: String,
    @DrawableRes val image: Int
)