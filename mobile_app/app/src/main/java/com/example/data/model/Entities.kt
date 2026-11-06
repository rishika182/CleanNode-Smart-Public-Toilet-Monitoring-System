package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val phoneNumber: String,
    val name: String,
    val role: UserRole,
    val preferredLanguage: AppLanguage,
    val assignedFacilityId: String? = null,
    val isLoggedIn: Boolean = true,
    val lastLoginTime: Long = System.currentTimeMillis()
)

@Entity(tableName = "facilities")
data class FacilityEntity(
    @PrimaryKey
    val id: String, // e.g. "CN-BLR-01"
    val name: String,
    val zone: String, // e.g. "South Zone - Jayanagar"
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isMonitoredPublic: Boolean, // true = CleanNode IoT public; false = private unmonitored
    val pciScore: Int, // 0..100 Predictive Cleanliness Index
    val cleanlinessBand: CleanlinessBand,
    // Sensor telemetry:
    val ammoniaPpm: Float, // MQ-135 (0 - 20 ppm)
    val temperatureC: Float, // DHT22 (°C)
    val humidityPct: Float, // DHT22 (%)
    val occupancyCount: Int, // PIR + door sensor daily footfall
    val isOccupied: Boolean,
    val flushFlowRate: Float, // YF-S201 (L/min)
    val flushHealthStatus: FlushHealthStatus,
    val waterTankLevelPct: Int, // 0..100 %
    val soapDispenserPct: Int, // 0..100 %
    // Operations & contacts
    val supervisorName: String,
    val supervisorPhone: String,
    val assignedStaffName: String,
    val assignedStaffPhone: String,
    val amenities: String, // e.g. "Wheelchair Accessible, Vending, Western/Indian"
    val openingHours: String = "24x7 Open",
    val distanceMeters: Int = 180, // Dynamic distance from user
    val cubiclesTotal: Int = 6,
    val cubiclesOccupied: Int = 4,
    val alertsCount: Int = 4,
    val lastCleanedTimestamp: Long = System.currentTimeMillis() - 3600000L,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "cleaning_duties")
data class CleaningDutyEntity(
    @PrimaryKey
    val id: String,
    val facilityId: String,
    val facilityName: String,
    val assignedStaffPhone: String,
    val assignedStaffName: String,
    val status: DutyStatus,
    val triggerReason: String, // e.g. "Danger Zone: PCI 24, Ammonia 8.5 ppm"
    val dispatchTime: Long,
    val acceptDeadlineTime: Long, // 10 minutes from dispatch
    val acceptedTime: Long? = null,
    val completedTime: Long? = null,
    val proofPhotoUri: String? = null,
    val proofNotes: String? = null,
    val floorMopped: Boolean = false,
    val bowlDisinfected: Boolean = false,
    val waterSupplyChecked: Boolean = false,
    val soapRefilled: Boolean = false,
    val trashEmptied: Boolean = false,
    val aiSanitationScore: Int? = null,
    val cubicle: String = "Men's · Cubicle 1",
    val priority: String = "High",
    val remarks: String = "",
    val overdueMinutes: Int = 120
)

@Entity(tableName = "escalations")
data class EscalationEntity(
    @PrimaryKey
    val id: String,
    val alertId: String,
    val facilityId: String,
    val facilityName: String,
    val initialPci: Int,
    val triggerCause: String,
    val stage: EscalationStage,
    val staffNotifiedTime: Long,
    val supervisorNotifiedTime: Long? = null,
    val adminEscalatedTime: Long? = null,
    val resolvedTime: Long? = null,
    val auditLog: String // Detailed event log
)

@Entity(tableName = "citizen_reports")
data class CitizenReportEntity(
    @PrimaryKey
    val id: String,
    val facilityId: String,
    val facilityName: String,
    val citizenPhone: String,
    val rating: Int, // 1 to 5
    val category: ReportCategory,
    val comment: String,
    val photoUri: String? = null,
    val status: String = "PENDING_REVIEW",
    val aiTriagePriority: String = "NORMAL",
    val timestamp: Long = System.currentTimeMillis()
)
