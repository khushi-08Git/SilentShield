package com.example.silentshield.service

import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.silentshield.data.detection.ScamNumberChecker
import com.example.silentshield.data.local.AlertRepository
import com.example.silentshield.presentation.home.RiskLevel
import com.example.silentshield.utils.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CallDetectionService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var alertRepository: AlertRepository
    private var lastCheckedNumber = ""

    // ── Android 12+ callback ───────────────────────────────────────────────
    private val telephonyCallback: TelephonyCallback? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : TelephonyCallback(), TelephonyCallback.CallStateListener {
                override fun onCallStateChanged(state: Int) {
                    // On API 31+, onCallStateChanged no longer provides the number
                    // We get it from the broadcast receiver instead
                    // This callback just keeps the service alive
                }
            }
        } else null
    }

    // ── Legacy listener for Android < 12 ──────────────────────────────────
    @Suppress("DEPRECATION")
    private val legacyPhoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            if (state == TelephonyManager.CALL_STATE_RINGING &&
                !phoneNumber.isNullOrBlank()) {
                if (phoneNumber == lastCheckedNumber) return
                lastCheckedNumber = phoneNumber
                handleIncomingCall(phoneNumber)
            }
            if (state == TelephonyManager.CALL_STATE_IDLE) {
                lastCheckedNumber = ""
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        alertRepository  = AlertRepository(applicationContext)
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager

        NotificationHelper.createChannels(this)

        ServiceCompat.startForeground(
            this,
            NotificationHelper.FOREGROUND_NOTIFICATION_ID,
            NotificationHelper.buildForegroundNotification(this),
            android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )

        registerTelephonyListener()
    }

    private fun registerTelephonyListener() {
        // Check permission before registering — the receiver handles
        // the actual number on Android 12+, so we only need the
        // legacy listener on older devices
        val hasPermission = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Log.w("CallDetectionService", "READ_PHONE_STATE not granted — skipping listener registration. Receiver will still catch calls.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // On Android 12+, register the new TelephonyCallback
            // The actual phone number comes from CallStateReceiver via
            // ACTION_PHONE_STATE_CHANGED broadcast
            telephonyCallback?.let {
                telephonyManager.registerTelephonyCallback(
                    mainExecutor,
                    it
                )
            }
        } else {
            // Legacy path for Android < 12
            @Suppress("DEPRECATION")
            telephonyManager.listen(
                legacyPhoneStateListener,
                PhoneStateListener.LISTEN_CALL_STATE
            )
        }
    }

    // Called from CallStateReceiver for Android 12+ (number comes from broadcast)
    fun handleIncomingCall(phoneNumber: String) {
        if (phoneNumber == lastCheckedNumber) return
        lastCheckedNumber = phoneNumber

        serviceScope.launch {
            val result = ScamNumberChecker.check(phoneNumber)
            if (result.isScam) {
                alertRepository.saveCallAlert(
                    phoneNumber = phoneNumber,
                    summary     = result.reason,
                    riskLevel   = result.riskLevel
                )
                val title = when (result.riskLevel) {
                    RiskLevel.HIGH   -> "⚠ Scam Call Detected!"
                    RiskLevel.MEDIUM -> "Suspicious Call"
                    RiskLevel.LOW    -> "Unknown Caller"
                }
                NotificationHelper.showScamAlert(
                    context        = applicationContext,
                    title          = title,
                    message        = "From $phoneNumber — ${result.reason}",
                    notificationId = phoneNumber.hashCode()
                )
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle number passed via intent from CallStateReceiver
        intent?.getStringExtra("phone_number")?.let { number ->
            if (number.isNotBlank()) handleIncomingCall(number)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyCallback?.let { telephonyManager.unregisterTelephonyCallback(it) }
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(legacyPhoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}