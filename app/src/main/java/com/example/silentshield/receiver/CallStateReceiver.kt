package com.example.silentshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import com.example.silentshield.service.CallDetectionService

class CallStateReceiver : BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state  = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
        val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (state == TelephonyManager.EXTRA_STATE_RINGING && !number.isNullOrBlank()) {
            // Start service and pass number via intent
            val serviceIntent = Intent(context, CallDetectionService::class.java).apply {
                putExtra("phone_number", number)
            }
            context.startForegroundService(serviceIntent)
        }
    }
}

