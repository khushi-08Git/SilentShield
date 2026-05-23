package com.example.silentshield

import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.silentshield.service.CallDetectionService
import com.example.silentshield.utils.NotificationHelper

class SilentShieldApp : Application() {
    @RequiresApi(Build.VERSION_CODES.O)
     override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
        try {
            startForegroundService(Intent(this, CallDetectionService::class.java))
        } catch (e: Exception) {
            // Service will still start via CallStateReceiver when a call comes in
            android.util.Log.e("SilentShieldApp", "Could not start service: ${e.message}")
        }
    }
}