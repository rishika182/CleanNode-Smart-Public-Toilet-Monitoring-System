package com.example.firebase

import android.util.Log
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CleanNodeFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Device Registration Token: $token")
        // Can be forwarded to backend or saved in Firestore under users/{phone}/fcmToken
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val title = remoteMessage.notification?.title ?: data["title"] ?: "CleanNode Alert"
        val body = remoteMessage.notification?.body ?: data["body"] ?: "New sanitation alert"
        val alertType = data["type"] ?: "ALERT"

        when (alertType) {
            "DANGER_ZONE" -> {
                val facilityName = data["facilityName"] ?: "Bengaluru Restroom"
                val pci = data["pci"]?.toIntOrNull() ?: 24
                val reason = data["reason"] ?: "Sensor anomaly detected"
                val staffPhone = data["staffPhone"] ?: ""
                NotificationHelper.sendDutyAssignedNotification(
                    context = applicationContext,
                    facilityName = facilityName,
                    pci = pci,
                    reason = reason,
                    staffPhone = staffPhone
                )
            }
            "SUPERVISOR_ESCALATION" -> {
                val facilityName = data["facilityName"] ?: "Restroom"
                val reason = data["reason"] ?: "Staff acceptance window expired"
                NotificationHelper.sendSupervisorEscalationNotification(
                    context = applicationContext,
                    facilityName = facilityName,
                    supervisorPhone = data["supervisorPhone"] ?: "+91 98452 34567",
                    reason = reason
                )
            }
            "ADMIN_ALERT" -> {
                val facilityName = data["facilityName"] ?: "Public Complex"
                NotificationHelper.sendAdminEscalationNotification(applicationContext, facilityName)
            }
            else -> {
                // General alert
                NotificationHelper.sendDutyAssignedNotification(
                    context = applicationContext,
                    facilityName = title,
                    pci = 45,
                    reason = body,
                    staffPhone = ""
                )
            }
        }
    }

    companion object {
        private const val TAG = "CleanNodeFCM"
    }
}
