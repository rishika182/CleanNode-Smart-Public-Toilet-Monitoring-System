package com.example.data.repository

import com.example.data.model.AnomalyItem
import com.example.data.model.CubicleLiveSensor
import com.example.data.model.MaintenanceHistoryRecord
import com.example.data.model.PredictiveMaintenanceItem
import com.example.data.model.StaffWorkload
import com.example.data.model.ThresholdAlert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CleanNodeOperationsProvider {

    val fieldStaffList = listOf(
        StaffWorkload("Ramesh", "+91 98860 11223", 3, 0),
        StaffWorkload("Lakshmi", "+91 98451 22002", 1, 2),
        StaffWorkload("Fatima", "+91 98862 33003", 2, 0),
        StaffWorkload("Suresh", "+91 98450 11001", 1, 3),
        StaffWorkload("Girish", "+91 98453 44004", 2, 1)
    )

    private val _alerts = MutableStateFlow(
        listOf(
            ThresholdAlert("ALT-01", "Jayanagar BBMP-03", "Men's", "Cubicle 3", 2096.5f, true, 24.9f, "Needs Cleaning", true, false, "1m ago"),
            ThresholdAlert("ALT-02", "Cubbon Park BBMP-01", "Men's", "Cubicle 1", 1679.0f, true, 25.2f, "Needs Cleaning", true, false, "just now"),
            ThresholdAlert("ALT-03", "Cubbon Park BBMP-01", "Men's", "Cubicle 2", 2705.3f, true, 34.6f, "Bad Smell", true, false, "3m ago"),
            ThresholdAlert("ALT-04", "Jayanagar BBMP-03", "Women's", "Cubicle 3", 2976.6f, true, 37.6f, "Bad Smell", true, false, "1m ago"),
            ThresholdAlert("ALT-05", "Jayanagar BBMP-01", "Men's", "Cubicle 1", 822.6f, false, 40.8f, "Flush Issue", true, false, "1m ago"),
            ThresholdAlert("ALT-06", "Jayanagar BBMP-03", "Women's", "Cubicle 1", 2343.5f, false, 40.8f, "Flush Issue", true, false, "1m ago"),
            ThresholdAlert("ALT-07", "Jayanagar BBMP-02", "Women's", "Cubicle 2", 715.7f, true, 45.7f, "Needs Cleaning", false, false, "just now"),
            ThresholdAlert("ALT-08", "Jayanagar BBMP-02", "Women's", "Cubicle 1", 1240.0f, true, 48.0f, "Needs Cleaning", false, false, "2m ago")
        )
    )
    val alerts: StateFlow<List<ThresholdAlert>> = _alerts.asStateFlow()

    fun handleAlert(alertId: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == alertId) it.copy(isHandled = true) else it
        }
    }

    val liveCubicles = listOf(
        CubicleLiveSensor("CUB-01", "Jayanagar BBMP-03", "Men's", "Cubicle 3", 22.2f, 28.4f, 80.8f, 2246.4f, false, true, "Flush Issue", "2m ago"),
        CubicleLiveSensor("CUB-02", "Jayanagar BBMP-02", "Men's", "Cubicle 3", 25.7f, 30.3f, 55.2f, 2555.7f, true, true, "Bad Smell", "1m ago"),
        CubicleLiveSensor("CUB-03", "Jayanagar BBMP-01", "Women's", "Cubicle 3", 27.9f, 24.2f, 54.2f, 1242.7f, false, true, "Flush Issue", "3m ago"),
        CubicleLiveSensor("CUB-04", "Jayanagar BBMP-03", "Men's", "Cubicle 1", 28.4f, 30.6f, 57.4f, 2953.2f, true, true, "Bad Smell", "3m ago"),
        CubicleLiveSensor("CUB-05", "Cubbon Park BBMP-01", "Men's", "Cubicle 2", 29.4f, 31.5f, 70.8f, 2546.2f, true, true, "Bad Smell", "3m ago"),
        CubicleLiveSensor("CUB-06", "Jayanagar BBMP-03", "Women's", "Cubicle 3", 40.5f, 25.3f, 77.5f, 1104.7f, true, false, "Needs Cleaning", "2m ago"),
        CubicleLiveSensor("CUB-07", "Jayanagar BBMP-02", "Women's", "Cubicle 1", 40.7f, 31.3f, 50.2f, 2596.4f, true, false, "Bad Smell", "3m ago"),
        CubicleLiveSensor("CUB-08", "Jayanagar BBMP-01", "Men's", "Cubicle 2", 41.6f, 30.6f, 52.2f, 2601.0f, true, true, "Bad Smell", "3m ago"),
        CubicleLiveSensor("CUB-09", "Jayanagar BBMP-03", "Men's", "Cubicle 2", 47.1f, 24.3f, 50.7f, 1413.6f, true, true, "Needs Cleaning", "3m ago"),
        CubicleLiveSensor("CUB-10", "Jayanagar BBMP-01", "Women's", "Cubicle 1", 51.0f, 29.5f, 77.8f, 2499.8f, true, false, "Needs Cleaning", "just now")
    )

    val predictiveMaintenanceList = listOf(
        PredictiveMaintenanceItem(
            id = "PM-01",
            facilityName = "Cubbon Park BBMP-01",
            block = "Men's",
            cubicleId = "Cubicle 2",
            riskScore = 80,
            riskLevel = "High risk",
            riskFactors = listOf("Low cleanliness index", "High gas/ammonia level", "High humidity"),
            currentPci = 26.2f,
            band = "Dirty"
        ),
        PredictiveMaintenanceItem(
            id = "PM-02",
            facilityName = "Jayanagar BBMP-03",
            block = "Women's",
            cubicleId = "Cubicle 3",
            riskScore = 65,
            riskLevel = "Medium risk",
            riskFactors = listOf("Low cleanliness index", "High gas/ammonia level"),
            currentPci = 29.9f,
            band = "Dirty"
        ),
        PredictiveMaintenanceItem(
            id = "PM-03",
            facilityName = "Jayanagar BBMP-03",
            block = "Men's",
            cubicleId = "Cubicle 2",
            riskScore = 60,
            riskLevel = "Medium risk",
            riskFactors = listOf("Very low cleanliness index"),
            currentPci = 47.1f,
            band = "Needs Attention"
        ),
        PredictiveMaintenanceItem(
            id = "PM-04",
            facilityName = "Jayanagar BBMP-01",
            block = "Men's",
            cubicleId = "Cubicle 2",
            riskScore = 45,
            riskLevel = "Medium risk",
            riskFactors = listOf("Moderate cleanliness index", "High gas/ammonia level"),
            currentPci = 41.6f,
            band = "Needs Attention"
        )
    )

    val anomaliesList = listOf(
        AnomalyItem(
            id = "AN-01",
            facilityName = "Jayanagar BBMP-03",
            block = "Women's",
            cubicleId = "Cubicle 2",
            severityScore = 85,
            severityLevel = "Critical",
            factors = listOf("High gas/ammonia level", "Abnormally high humidity", "High occupancy", "Low cleanliness index"),
            tags = listOf("temperature", "humidity", "gas", "occupancy", "water_flow", "PCI")
        ),
        AnomalyItem(
            id = "AN-02",
            facilityName = "Jayanagar BBMP-03",
            block = "Men's",
            cubicleId = "Cubicle 1",
            severityScore = 70,
            severityLevel = "Critical",
            factors = listOf("High occupancy", "Occupancy detected but no water flow", "Low cleanliness index"),
            tags = listOf("temperature", "humidity", "gas", "occupancy", "water_flow", "PCI")
        ),
        AnomalyItem(
            id = "AN-03",
            facilityName = "Jayanagar BBMP-01",
            block = "Women's",
            cubicleId = "Cubicle 2",
            severityScore = 65,
            severityLevel = "Moderate",
            factors = listOf("Abnormally high humidity"),
            tags = listOf("humidity", "PCI")
        ),
        AnomalyItem(
            id = "AN-04",
            facilityName = "Jayanagar BBMP-02",
            block = "Women's",
            cubicleId = "Cubicle 2",
            severityScore = 65,
            severityLevel = "Moderate",
            factors = listOf("High gas/ammonia level"),
            tags = listOf("gas", "PCI")
        )
    )

    val maintenanceHistory = listOf(
        MaintenanceHistoryRecord("HIS-01", "25 Aug, 09:00 pm", "Jayanagar BBMP-01", "Men's · Cubicle 1", 44.4f, 464.9f, 75f, 26.3f, "Flushed", "Needs Cleaning"),
        MaintenanceHistoryRecord("HIS-02", "25 Aug, 09:00 pm", "Jayanagar BBMP-01", "Men's · Cubicle 2", 53.6f, 2333.4f, 64.9f, 25.0f, "Flushed", "Needs Cleaning"),
        MaintenanceHistoryRecord("HIS-03", "25 Aug, 09:00 pm", "Jayanagar BBMP-01", "Men's · Cubicle 3", 43.6f, 2698.6f, 74.5f, 30.3f, "Flushed", "Bad Smell"),
        MaintenanceHistoryRecord("HIS-04", "25 Aug, 09:00 pm", "Jayanagar BBMP-01", "Women's · Cubicle 1", 54.8f, 2373.8f, 73.3f, 29.5f, "Flushed", "Needs Cleaning"),
        MaintenanceHistoryRecord("HIS-05", "25 Aug, 09:00 pm", "Jayanagar BBMP-01", "Women's · Cubicle 2", 52.9f, 2192.2f, 60.9f, 28.9f, "Flushed", "Needs Cleaning"),
        MaintenanceHistoryRecord("HIS-06", "25 Aug, 09:00 pm", "Jayanagar BBMP-01", "Women's · Cubicle 3", 54.7f, 822.9f, 62.0f, 27.4f, "Not Flushed", "Needs Cleaning"),
        MaintenanceHistoryRecord("HIS-07", "25 Aug, 09:00 pm", "Jayanagar BBMP-02", "Men's · Cubicle 1", 41.9f, 2231.8f, 46.7f, 27.5f, "Flushed", "Needs Cleaning"),
        MaintenanceHistoryRecord("HIS-08", "25 Aug, 09:00 pm", "Jayanagar BBMP-02", "Men's · Cubicle 2", 45.1f, 849.5f, 69.9f, 26.2f, "Not Flushed", "Needs Cleaning")
    )
}
