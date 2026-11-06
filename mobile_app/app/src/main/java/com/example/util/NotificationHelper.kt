package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val TAG = "CleanNodeNotify"
    const val CHANNEL_ALERTS = "cleannode_alerts"
    const val CHANNEL_OTP = "cleannode_otp"
    const val CHANNEL_DISPATCH = "cleannode_dispatch"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val alertChannel = NotificationChannel(
                CHANNEL_ALERTS,
                "CleanNode Danger Zone Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alerts when restrooms enter danger zone or sensor anomalies occur"
                enableVibration(true)
            }

            val otpChannel = NotificationChannel(
                CHANNEL_OTP,
                "CleanNode OTP Security",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "One-Time Password authentication codes"
                enableVibration(true)
            }

            val dispatchChannel = NotificationChannel(
                CHANNEL_DISPATCH,
                "CleanNode Staff Duty Dispatch",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Automated cleaning duty assignments and escalation notifications"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(alertChannel)
            notificationManager.createNotificationChannel(otpChannel)
            notificationManager.createNotificationChannel(dispatchChannel)
        }
    }

    fun sendOtpNotification(context: Context, phone: String, otpCode: String) {
        createChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            101,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_OTP)
            .setSmallIcon(R.drawable.cleannode_logo)
            .setContentTitle("CleanNode OTP Verification")
            .setContentText("Your verification OTP is $otpCode (Valid for 5 mins).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Your CleanNode security OTP code is $otpCode.\nValid for 5 minutes. Do not share this code with anyone.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(101, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted: ${e.message}")
        }

        // Send real SMS if permission exists, or log
        sendRealSms(context, phone, "Your CleanNode verification OTP is: $otpCode. Valid for 5 minutes.")
    }

    fun sendDutyAssignedNotification(
        context: Context,
        facilityName: String,
        pci: Int,
        reason: String,
        staffPhone: String
    ) {
        createChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            201,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DISPATCH)
            .setSmallIcon(R.drawable.cleannode_logo)
            .setContentTitle("🚨 CleanNode: Urgent Cleaning Duty Assigned!")
            .setContentText("$facilityName enters Danger Zone (PCI: $pci/100).")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("AUTOMATED DISPATCH: $facilityName dropped to PCI $pci/100.\nReason: $reason\n10-Minute Acceptance Window Active. Please accept and report to location.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(201, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted: ${e.message}")
        }

        // Send SMS to assigned staff phone
        sendRealSms(
            context,
            staffPhone,
            "[CleanNode BBMP] URGENT DUTY: $facilityName entered Danger Zone (PCI $pci). $reason. Please open CleanNode app and accept within 10 min."
        )
    }

    fun sendSupervisorEscalationNotification(
        context: Context,
        facilityName: String,
        supervisorPhone: String,
        reason: String
    ) {
        createChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.cleannode_logo)
            .setContentTitle("⚠️ CleanNode Escalation: Supervisor Action Required")
            .setContentText("Staff response timed out for $facilityName.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("ESCALATION STAGE 2: Assigned staff did not accept duty within 10 minutes for $facilityName.\nReason: $reason\nPlease contact staff or initiate manual re-assignment.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(301, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted: ${e.message}")
        }

        sendRealSms(
            context,
            supervisorPhone,
            "[CleanNode Escalation] ATTENTION SUPERVISOR: Staff acceptance timed out for $facilityName. Auto-escalated to Supervisor level. Please take action."
        )
    }

    fun sendAdminEscalationNotification(context: Context, facilityName: String) {
        createChannels(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.cleannode_logo)
            .setContentTitle("🛑 BBMP Command Alert: Danger Zone Unresolved")
            .setContentText("Top Priority Escalation for $facilityName.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("ESCALATION STAGE 3: Facility $facilityName remained unresolved after Supervisor notification.\nSurfaced to BBMP Central Command Dashboard. Van dispatch recommended.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(401, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "Notification permission not granted: ${e.message}")
        }
    }

    private fun sendRealSms(context: Context, phone: String, message: String) {
        try {
            val cleanPhone = phone.replace(" ", "").replace("-", "")
            if (cleanPhone.isNotBlank()) {
                val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    context.getSystemService(SmsManager::class.java) ?: @Suppress("DEPRECATION") SmsManager.getDefault()
                } else {
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
                val parts = smsManager.divideMessage(message)
                if (parts.size > 1) {
                    smsManager.sendMultipartTextMessage(cleanPhone, null, parts, null, null)
                } else {
                    smsManager.sendTextMessage(cleanPhone, null, message, null, null)
                }
                Log.i(TAG, "SMS successfully sent to $cleanPhone: $message")
            }
        } catch (e: Exception) {
            Log.w(TAG, "SMS dispatch: ${e.message}")
        }
    }
}
