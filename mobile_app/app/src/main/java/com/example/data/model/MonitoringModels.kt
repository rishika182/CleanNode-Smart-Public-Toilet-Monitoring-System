package com.example.data.model

data class CubicleLiveSensor(
    val id: String,
    val facilityName: String,
    val block: String, // "Men's", "Women's"
    val cubicleId: String, // "Cubicle 1", "Cubicle 2", "Cubicle 3"
    val pciScore: Float,
    val tempC: Float,
    val humidityPct: Float,
    val gasPpm: Float,
    val isFlushed: Boolean,
    val isOccupied: Boolean,
    val alertTag: String?, // "Flush Issue", "Bad Smell", "Needs Cleaning", null
    val updatedAgo: String
)

data class ThresholdAlert(
    val id: String,
    val facilityName: String,
    val block: String,
    val cubicleId: String,
    val gasPpm: Float,
    val isFlushed: Boolean,
    val pciScore: Float,
    val alertTag: String, // "Needs Cleaning", "Bad Smell", "Flush Issue"
    val isCritical: Boolean = true,
    val isHandled: Boolean = false,
    val timeAgo: String
)

data class PredictiveMaintenanceItem(
    val id: String,
    val facilityName: String,
    val block: String,
    val cubicleId: String,
    val riskScore: Int, // 0..100
    val riskLevel: String, // "High risk", "Medium risk", "Low risk"
    val riskFactors: List<String>,
    val currentPci: Float,
    val band: String
)

data class AnomalyItem(
    val id: String,
    val facilityName: String,
    val block: String,
    val cubicleId: String,
    val severityScore: Int,
    val severityLevel: String, // "Critical", "Moderate", "Minor"
    val factors: List<String>,
    val tags: List<String>
)

data class StaffWorkload(
    val name: String,
    val phone: String,
    val activeCount: Int,
    val doneCount: Int
)

data class MaintenanceHistoryRecord(
    val id: String,
    val timestamp: String,
    val facilityName: String,
    val cubicle: String,
    val pciScore: Float,
    val gasPpm: Float,
    val humidityPct: Float,
    val tempC: Float,
    val waterStatus: String, // "Flushed", "Not Flushed"
    val alertTag: String
)
