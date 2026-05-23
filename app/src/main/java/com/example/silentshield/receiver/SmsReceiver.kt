package com.example.silentshield.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Telephony
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.silentshield.data.detection.SmsAnalyzer
import com.example.silentshield.data.local.AlertRepository
import com.example.silentshield.utils.NotificationHelper
import com.example.silentshield.presentation.home.RiskLevel


class SmsReceiver : BroadcastReceiver() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
        if (messages.isNullOrEmpty()) return

        // Combine multi-part SMS into one message
        val sender  = messages[0].originatingAddress ?: "Unknown"
        val fullMsg = messages.joinToString("") { it.messageBody ?: "" }

        val result = SmsAnalyzer.analyze(sender, fullMsg)

        if (result.isPhishing) {
            val repository = AlertRepository(context)
            NotificationHelper.createChannels(context)

            CoroutineScope(Dispatchers.IO).launch {
                repository.saveSmsAlert(
                    sender    = sender,
                    summary   = result.reason,
                    riskLevel = result.riskLevel
                )

                val title = when (result.riskLevel) {
                    RiskLevel.HIGH   -> "⚠ Phishing SMS Detected!"
                    RiskLevel.MEDIUM -> "Suspicious SMS"
                    RiskLevel.LOW    -> "Unusual SMS"
                }

                NotificationHelper.showScamAlert(
                    context        = context,
                    title          = title,
                    message        = "From $sender — ${result.reason}",
                    notificationId = (sender + System.currentTimeMillis()).hashCode()
                )
            }
        }
    }
}