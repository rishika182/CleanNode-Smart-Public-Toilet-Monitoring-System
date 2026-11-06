package com.example.firebase

import android.util.Log
import com.example.data.model.CitizenReportEntity
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.EscalationEntity
import com.example.data.model.FacilityEntity
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await

import com.example.data.model.UserEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FirebaseSyncService {

    private const val TAG = "CleanNodeFirebaseSync"

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val realtimeDb by lazy {
        FirebaseDatabase.getInstance("https://cleannode-smartsanitation-default-rtdb.firebaseio.com")
    }

    /**
     * Pushes user login session and profile to Firestore for auditing and role verification
     */
    suspend fun syncUserLogin(user: UserEntity, email: String? = null, firebaseUid: String? = null) {
        try {
            val docKey = firebaseUid ?: (email?.replace(".", "_")?.replace("@", "_") ?: user.phoneNumber.replace("+", "").replace(" ", "").trim())
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(user.lastLoginTime))
            val userData = hashMapOf<String, Any>(
                "uid" to (firebaseUid ?: ""),
                "phoneNumber" to user.phoneNumber,
                "email" to (email ?: ""),
                "name" to user.name,
                "role" to user.role.name,
                "preferredLanguage" to user.preferredLanguage.name,
                "assignedFacilityId" to (user.assignedFacilityId ?: ""),
                "isLoggedIn" to true,
                "lastLoginTime" to user.lastLoginTime,
                "lastLoginFormatted" to dateStr
            )

            // Store active user profile
            firestore.collection("users").document(docKey).set(userData).await()

            // Also record in login_audit_trail collection for tracking all logins
            val auditLog = hashMapOf<String, Any>(
                "userDocKey" to docKey,
                "role" to user.role.name,
                "identifier" to (email ?: user.phoneNumber),
                "timestamp" to user.lastLoginTime,
                "loginDate" to dateStr
            )
            firestore.collection("login_history").add(auditLog).await()
            Log.d(TAG, "Successfully synced user login for ${user.role} to Firestore ($docKey)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing user login to Firestore: ${e.message}")
        }
    }

    /**
     * Pushes local facility state to Firestore & Realtime DB
     */
    suspend fun syncFacilityToCloud(facility: FacilityEntity) {
        try {
            firestore.collection("facilities").document(facility.id).set(facility).await()

            val dashboardId = when (facility.id) {
                "CN-BLR-01" -> "Jayanagar_BBMP_01"
                "CN-BLR-02" -> "Jayanagar_BBMP_02"
                "CN-BLR-03" -> "Jayanagar_BBMP_03"
                "CN-BLR-04" -> "Cubbon_Park_BBMP_01"
                "CN-BLR-05" -> "Cubbon_Park_BBMP_02"
                else -> facility.id
            }

            val facUpdates = hashMapOf<String, Any>(
                "id" to dashboardId,
                "name" to facility.name,
                "pciScore" to facility.pciScore,
                "cleanlinessBand" to facility.cleanlinessBand.name,
                "last_cleaned" to facility.lastCleanedTimestamp,
                "last_updated" to facility.lastUpdatedTimestamp
            )
            realtimeDb.getReference("facilities/$dashboardId").updateChildren(facUpdates).await()
            if (dashboardId != facility.id) {
                realtimeDb.getReference("facilities/${facility.id}").updateChildren(facUpdates).await()
            }

            // Update cubicles for both Mens and Womens blocks so Web Dashboard immediately reflects Clean
            val isoTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }.format(Date())

            val cleanCubicle = hashMapOf<String, Any>(
                "PCI" to facility.pciScore.toDouble(),
                "cleanlinessStatus" to "Clean",
                "alert" to "None",
                "alertActive" to false,
                "gas" to 420.0,
                "ammonia_level" to 420.0,
                "hours_since_cleaned" to 0.0,
                "anomalyDetected" to false,
                "maintenanceRequired" to false,
                "last_updated" to isoTime
            )

            for (block in listOf("Mens", "Womens")) {
                for (cNum in 1..3) {
                    realtimeDb.getReference("facilities/$dashboardId/blocks/$block/cubicles/Cubicle_$cNum")
                        .updateChildren(cleanCubicle)
                }
            }
            Log.d(TAG, "Successfully synced clean state to cloud for $dashboardId")
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing facility ${facility.id} to cloud: ${e.message}")
        }
    }

    /**
     * Pushes a new cleaning duty assignment to Firestore and Realtime DB
     */
    suspend fun syncCleaningDuty(duty: CleaningDutyEntity) {
        try {
            val dashboardFacId = when (duty.facilityId) {
                "CN-BLR-01" -> "Jayanagar_BBMP_01"
                "CN-BLR-02" -> "Jayanagar_BBMP_02"
                "CN-BLR-03" -> "Jayanagar_BBMP_03"
                "CN-BLR-04" -> "Cubbon_Park_BBMP_01"
                "CN-BLR-05" -> "Cubbon_Park_BBMP_02"
                else -> duty.facilityId
            }

            val photoVal = duty.proofPhotoUri ?: ""

            val dashboardDuty = hashMapOf<String, Any>(
                "id" to duty.id,
                "duty_id" to duty.id,
                "facility_id" to dashboardFacId,
                "facility_name" to duty.facilityName,
                "area_id" to dashboardFacId.split("_BBMP_")[0],
                "block_id" to if (duty.cubicle.contains("Women", ignoreCase = true)) "Womens" else "Mens",
                "cubicle_id" to (if (duty.cubicle.contains("Cubicle 2")) "Cubicle_2" else if (duty.cubicle.contains("Cubicle 3")) "Cubicle_3" else "Cubicle_1"),
                "staff" to duty.assignedStaffName,
                "staff_name" to duty.assignedStaffName,
                "assigned_staff" to duty.assignedStaffName,
                "status" to when (duty.status) {
                    com.example.data.model.DutyStatus.COMPLETED -> "Completed"
                    com.example.data.model.DutyStatus.IN_PROGRESS -> "In Progress"
                    com.example.data.model.DutyStatus.DECLINED -> "Declined"
                    else -> "Pending"
                },
                "priority" to duty.priority,
                "remarks" to (duty.proofNotes ?: duty.remarks),
                "proof_photo" to photoVal,
                "proof_photo_url" to photoVal,
                "proofPhotoUri" to photoVal,
                "photo_url" to photoVal,
                "photoUrl" to photoVal,
                "imageUrl" to photoVal,
                "started_time" to duty.dispatchTime,
                "completed_time" to (duty.completedTime ?: System.currentTimeMillis()),
                "verification" to "Pending",
                "cubicle" to duty.cubicle,
                "floorMopped" to duty.floorMopped,
                "bowlDisinfected" to duty.bowlDisinfected,
                "waterSupplyChecked" to duty.waterSupplyChecked,
                "soapRefilled" to duty.soapRefilled,
                "trashEmptied" to duty.trashEmptied,
                "aiSanitationScore" to (duty.aiSanitationScore ?: 95)
            )

            // Update in Realtime DB nodes
            realtimeDb.getReference("assignments/${duty.id}").updateChildren(dashboardDuty).await()
            realtimeDb.getReference("assignment_overrides/${duty.id}").updateChildren(dashboardDuty).await()
            realtimeDb.getReference("active_duties/${duty.id}").updateChildren(dashboardDuty).await()

            // Update in Firestore
            firestore.collection("cleaning_duties").document(duty.id).set(duty).await()
            firestore.collection("cleaning_duties").document(duty.id).update(dashboardDuty as Map<String, Any>).await()

            Log.d(TAG, "Successfully synced duty ${duty.id} with photo to Firebase ($photoVal)")
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing cleaning duty ${duty.id}: ${e.message}")
        }
    }

    /**
     * Pushes an active escalation to Firestore
     */
    suspend fun syncEscalation(escalation: EscalationEntity) {
        try {
            firestore.collection("escalations").document(escalation.id).set(escalation).await()
            realtimeDb.getReference("escalations/${escalation.id}").setValue(escalation).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing escalation ${escalation.id}: ${e.message}")
        }
    }

    /**
     * Submits a citizen report to Firestore & Realtime DB
     */
    suspend fun syncCitizenReport(report: CitizenReportEntity) {
        try {
            val photoVal = report.photoUri ?: ""
            val reportMap = hashMapOf<String, Any>(
                "id" to report.id,
                "report_id" to report.id,
                "facility_id" to report.facilityId,
                "facility_name" to report.facilityName,
                "citizen_phone" to report.citizenPhone,
                "citizenPhone" to report.citizenPhone,
                "rating" to report.rating,
                "category" to report.category.name,
                "comment" to report.comment,
                "photo_url" to photoVal,
                "photoUrl" to photoVal,
                "photoUri" to photoVal,
                "proof_photo" to photoVal,
                "imageUrl" to photoVal,
                "status" to report.status,
                "priority" to report.aiTriagePriority,
                "aiTriagePriority" to report.aiTriagePriority,
                "timestamp" to report.timestamp
            )

            firestore.collection("citizen_reports").document(report.id).set(reportMap).await()
            realtimeDb.getReference("citizen_reports/${report.id}").setValue(reportMap).await()
            realtimeDb.getReference("reports/${report.id}").setValue(reportMap).await()
            Log.d(TAG, "Successfully synced citizen report ${report.id} with photo to Firebase")
        } catch (e: Exception) {
            Log.w(TAG, "Failed syncing report ${report.id}: ${e.message}")
        }
    }

    /**
     * Listen for real-time sensor updates on a specific facility
     */
    fun listenToFacilitySensors(
        facilityId: String,
        onUpdate: (pci: Int, ammonia: Float, flowRate: Float) -> Unit
    ): ListenerRegistration {
        return firestore.collection("facilities").document(facilityId)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                val pci = snapshot.getLong("pciScore")?.toInt() ?: 75
                val ammonia = snapshot.getDouble("ammoniaPpm")?.toFloat() ?: 2.0f
                val flow = snapshot.getDouble("flushFlowRate")?.toFloat() ?: 5.0f
                onUpdate(pci, ammonia, flow)
            }
    }
}
