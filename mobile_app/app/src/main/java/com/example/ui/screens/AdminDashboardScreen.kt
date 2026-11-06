package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.CitizenReportEntity
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.CleanlinessBand
import com.example.data.model.DutyStatus
import com.example.data.model.EscalationEntity
import com.example.data.model.EscalationStage
import com.example.data.model.FacilityEntity
import com.example.ui.components.AssignDutyDialog
import com.example.ui.components.BengaluruRestroomMap
import com.example.ui.components.EscalationLadderView
import com.example.ui.components.PciGauge
import com.example.localization.LocalizationManager
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary

@Composable
fun AdminDashboardScreen(
    facilities: List<FacilityEntity>,
    escalations: List<EscalationEntity>,
    reports: List<CitizenReportEntity>,
    duties: List<CleaningDutyEntity> = emptyList(),
    lang: AppLanguage,
    selectedTab: Int = 0, // 0 = OVERVIEW, 1 = ALERTS, 2 = FEEDBACK, 3 = MAP
    onSelectTab: (Int) -> Unit = {},
    onSimulateAnomaly: (String) -> Unit,
    onRequestAiDiagnostics: (FacilityEntity) -> Unit,
    onResolveReport: (String) -> Unit = {},
    onDispatchDuty: (
        facilityId: String,
        facilityName: String,
        cubicle: String,
        staffName: String,
        staffPhone: String,
        priority: String,
        remarks: String
    ) -> Unit
) {
    val context = LocalContext.current
    val monitoredFacilities = remember(facilities) { facilities.filter { it.isMonitoredPublic } }
    val dangerCount = monitoredFacilities.count { it.cleanlinessBand == CleanlinessBand.DANGER_ZONE }
    val cleanCount = monitoredFacilities.count { it.cleanlinessBand == CleanlinessBand.CLEAN }
    val avgPci = if (monitoredFacilities.isNotEmpty()) monitoredFacilities.map { it.pciScore }.average().toInt() else 0

    var showAssignDutyDialog by remember { mutableStateOf(false) }
    var targetFacilityForDuty by remember { mutableStateOf<FacilityEntity?>(null) }
    var dutyInitialInstructions by remember { mutableStateOf<String?>(null) }
    var selectedFacilityOnMap by remember { mutableStateOf<FacilityEntity?>(null) }
    var showSuccessToast by remember { mutableStateOf<String?>(null) }
    var reportFilter by remember { mutableStateOf("ALL") }

    val launchGoogleMaps = { fac: FacilityEntity ->
        val gmmIntentUri = Uri.parse("google.navigation:q=${fac.latitude},${fac.longitude}&mode=w")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${fac.latitude},${fac.longitude}&travelmode=walking")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Success feedback banner
            if (showSuccessToast != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = CleanGreen.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CleanGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = showSuccessToast ?: "",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            color = CleanGreen
                        )
                    }
                }
            }

            when (selectedTab) {
                3 -> {
                    // Tab 3: Interactive In-App Map (Places UI Kit & Compute Routes)
                    BengaluruRestroomMap(
                        facilities = monitoredFacilities,
                        selectedFacility = selectedFacilityOnMap,
                        lang = lang,
                        onSelectFacility = { selectedFacilityOnMap = it },
                        onNavigate = { launchGoogleMaps(it) },
                        userRole = com.example.data.model.UserRole.ADMIN,
                        onAssignDuty = { fac ->
                            targetFacilityForDuty = fac
                            showAssignDutyDialog = true
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                1 -> {
                    // Tab 1: Alerts / Escalations
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("admin_escalations_list")
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            val activeEscalation = escalations.firstOrNull { it.stage != EscalationStage.RESOLVED }
                            EscalationLadderView(
                                currentStage = activeEscalation?.stage ?: EscalationStage.STAFF_DISPATCHED,
                                lang = lang,
                                escalations = escalations,
                                duties = duties
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Danger Zone Escalation Audit Trail (/escalations)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        if (escalations.isEmpty()) {
                            item {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Box(modifier = Modifier.padding(16.dp)) {
                                        Text("No active escalations recorded. All city sectors optimal.")
                                    }
                                }
                            }
                        } else {
                            items(escalations, key = { it.id }) { esc ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = esc.facilityName,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(DangerRed.copy(alpha = 0.15f))
                                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = esc.stage.displayName.split(":")[0],
                                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = DangerRed
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "Cause: ${esc.triggerCause}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = DangerRed
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = esc.auditLog,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                modifier = Modifier.padding(8.dp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Button(
                                            onClick = {
                                                val fac = facilities.firstOrNull { it.id == esc.facilityId }
                                                targetFacilityForDuty = fac
                                                showAssignDutyDialog = true
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                        ) {
                                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Reassign / Dispatch Staff", fontSize = 11.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                2 -> {
                    // Tab 2: Feedback / Citizen Reports Triage & NLP Prioritization
                    val criticalCount = reports.count { it.aiTriagePriority == "CRITICAL" }
                    val pendingCount = reports.count { it.status != "RESOLVED" }
                    val resolvedCount = reports.count { it.status == "RESOLVED" }

                    val filteredReports = when (reportFilter) {
                        "CRITICAL" -> reports.filter { it.aiTriagePriority == "CRITICAL" }
                        "PENDING" -> reports.filter { it.status != "RESOLVED" }
                        "RESOLVED" -> reports.filter { it.status == "RESOLVED" }
                        else -> reports
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("admin_feedback_list")
                    ) {
                        item {
                            // Sleek Top Header Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp, bottom = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(TealPrimary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Feedback, contentDescription = null, tint = TealPrimary, modifier = Modifier.size(18.dp))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Citizen Reports Triage",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${reports.size} total reports • $pendingCount pending",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (criticalCount > 0) DangerRed.copy(alpha = 0.12f) else CleanGreen.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = if (criticalCount > 0) "$criticalCount Critical" else "Stable",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (criticalCount > 0) DangerRed else CleanGreen,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            // Unified Interactive KPI Filter Pill Bar (Horizontal Arrangement)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val filterItems = listOf(
                                    Triple("ALL", "All", reports.size),
                                    Triple("CRITICAL", "Critical", criticalCount),
                                    Triple("PENDING", "Pending", pendingCount),
                                    Triple("RESOLVED", "Resolved", resolvedCount)
                                )

                                filterItems.forEach { (key, label, count) ->
                                    val isSelected = reportFilter == key
                                    val activeColor = when (key) {
                                        "CRITICAL" -> DangerRed
                                        "PENDING" -> Color(0xFFF57F17)
                                        "RESOLVED" -> CleanGreen
                                        else -> TealPrimary
                                    }

                                    Surface(
                                        onClick = { reportFilter = key },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                        border = if (!isSelected) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)) else null
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "$label ($count)",
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }

                        if (filteredReports.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CleanGreen, modifier = Modifier.size(36.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = if (reportFilter == "ALL") "No Citizen Reports Logged" else "No $reportFilter Reports",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (reportFilter == "ALL") "All city sanitation facilities are operating normally." else "All citizen reports in this category have been addressed.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                        if (reportFilter != "ALL") {
                                            Spacer(modifier = Modifier.height(10.dp))
                                            OutlinedButton(
                                                onClick = { reportFilter = "ALL" },
                                                shape = RoundedCornerShape(8.dp),
                                                border = BorderStroke(1.dp, TealPrimary),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("Show All Reports (${reports.size})", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            items(filteredReports, key = { it.id }) { report ->
                                val facility = facilities.firstOrNull { it.id == report.facilityId }
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    border = BorderStroke(
                                        1.dp,
                                        if (report.aiTriagePriority == "CRITICAL" && report.status != "RESOLVED") DangerRed.copy(alpha = 0.4f)
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        // Header Row: Facility Name + Priority & Status Pills
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = report.facilityName,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${report.category.displayName} • ${report.rating}★",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = when (report.aiTriagePriority) {
                                                        "CRITICAL" -> DangerRed.copy(alpha = 0.15f)
                                                        "HIGH" -> ModerateYellow.copy(alpha = 0.25f)
                                                        else -> TealPrimary.copy(alpha = 0.12f)
                                                    }
                                                ) {
                                                    Text(
                                                        text = report.aiTriagePriority,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = when (report.aiTriagePriority) {
                                                            "CRITICAL" -> DangerRed
                                                            "HIGH" -> Color(0xFFE65100)
                                                            else -> TealPrimary
                                                        }
                                                    )
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (report.status == "RESOLVED") CleanGreen.copy(alpha = 0.15f) else Color(0xFFF57F17).copy(alpha = 0.15f)
                                                ) {
                                                    Text(
                                                        text = report.status,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (report.status == "RESOLVED") CleanGreen else Color(0xFFE65100)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Citizen Comment & Photo Evidence Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 10.dp, vertical = 7.dp)
                                            ) {
                                                Text(
                                                    text = "\"${report.comment}\"",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (!report.photoUri.isNullOrEmpty()) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                                ) {
                                                    val imageModel = remember(report.photoUri) {
                                                        val uri = report.photoUri
                                                        if (uri != null && uri.startsWith("data:image")) {
                                                            try {
                                                                val base64 = uri.substringAfter("base64,")
                                                                val bytes = android.util.Base64.decode(base64, android.util.Base64.DEFAULT)
                                                                android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                                            } catch (_: Exception) {
                                                                uri
                                                            }
                                                        } else {
                                                            uri
                                                        }
                                                    }
                                                    AsyncImage(
                                                        model = imageModel,
                                                        contentDescription = "Citizen Evidence",
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        // Compact 1-Row Action Bar
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // Assign Duty Button
                                            Button(
                                                onClick = {
                                                    targetFacilityForDuty = facility
                                                    dutyInitialInstructions = "[Citizen Report - ${report.category.displayName}] ${report.comment}"
                                                    showAssignDutyDialog = true
                                                },
                                                modifier = Modifier.weight(1.1f).height(34.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(13.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("⚡ Assign", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                            }

                                            // Resolve Button
                                            if (report.status != "RESOLVED") {
                                                OutlinedButton(
                                                    onClick = {
                                                        onResolveReport(report.id)
                                                        showSuccessToast = "Report #${report.id} marked as RESOLVED"
                                                    },
                                                    modifier = Modifier.weight(0.95f).height(34.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, CleanGreen),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                                ) {
                                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(13.dp), tint = CleanGreen)
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Resolve", fontSize = 11.sp, maxLines = 1, color = CleanGreen, fontWeight = FontWeight.Bold)
                                                }
                                            }

                                            // Navigate Button
                                            if (facility != null) {
                                                OutlinedButton(
                                                    onClick = { launchGoogleMaps(facility) },
                                                    modifier = Modifier.weight(1.05f).height(34.dp),
                                                    shape = RoundedCornerShape(8.dp),
                                                    border = BorderStroke(1.dp, TealPrimary),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                                ) {
                                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(13.dp), tint = TealPrimary)
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text("Navigate", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                                }
                                            }

                                            // Staff Call Button
                                            if (facility != null && facility.assignedStaffPhone.isNotBlank()) {
                                                Surface(
                                                    onClick = {
                                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${facility.assignedStaffPhone}"))
                                                        try { context.startActivity(dialIntent) } catch (_: Exception) {}
                                                    },
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = TealPrimary.copy(alpha = 0.1f),
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Box(contentAlignment = Alignment.Center) {
                                                        Icon(Icons.Default.Phone, contentDescription = "Call Staff", tint = TealPrimary, modifier = Modifier.size(15.dp))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }
                else -> {
                    // Tab 0: Overview / Command Desk
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .testTag("admin_dashboard_list")
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))

                            // BBMP Central Command Header
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = TealPrimary)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(Color.White.copy(alpha = 0.2f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = Color.White)
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = LocalizationManager.getAdminHeader(lang),
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = Color.White
                                            )
                                            Text(
                                                text = LocalizationManager.getAdminSubHeader(lang),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Spacious 2x2 KPI Grid - No squished or overlapping rows
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AdminStatCard(
                                            title = LocalizationManager.getMonitored(lang),
                                            value = "${monitoredFacilities.size}",
                                            subtitle = "Active IoT Units",
                                            icon = Icons.Default.Sensors,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatCard(
                                            title = LocalizationManager.getAvgCityPci(lang),
                                            value = "$avgPci/100",
                                            subtitle = if (avgPci >= 70) "City Healthy" else "Under Alert",
                                            icon = Icons.Default.AutoAwesome,
                                            color = if (avgPci >= 70) CleanGreen else ModerateYellow,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        AdminStatCard(
                                            title = LocalizationManager.getPristineClean(lang),
                                            value = "$cleanCount",
                                            subtitle = "Optimal (PCI >= 80)",
                                            icon = Icons.Default.CheckCircle,
                                            color = CleanGreen,
                                            modifier = Modifier.weight(1f)
                                        )
                                        AdminStatCard(
                                            title = LocalizationManager.getDangerZoneTitle(lang),
                                            value = "$dangerCount",
                                            subtitle = if (dangerCount > 0) "Immediate Attention" else "Zero Criticals",
                                            icon = Icons.Default.Warning,
                                            color = if (dangerCount > 0) DangerRed else Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Examiner / Live Demo Anomaly Spike Tool
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = LocalizationManager.getExaminerLiveDemoTitle(lang),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = LocalizationManager.getExaminerLiveDemoDesc(lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            onSimulateAnomaly("CN-BLR-01")
                                            showSuccessToast = "Sensor spike simulated! Duty auto-dispatched."
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("btn_simulate_spike")
                                    ) {
                                        Icon(Icons.Default.FlashOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(LocalizationManager.getTriggerSpike(lang), fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Direct Staff Duty Dispatch Action Banner
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = TealPrimary.copy(alpha = 0.08f)),
                                border = BorderStroke(1.dp, TealPrimary.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                        Text(
                                            text = LocalizationManager.getDirectDispatchTitle(lang),
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = TealPrimary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = LocalizationManager.getDirectDispatchDesc(lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            targetFacilityForDuty = null
                                            showAssignDutyDialog = true
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                    ) {
                                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(LocalizationManager.getDispatchDuty(lang), fontSize = 11.sp, maxLines = 1)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Sanitation Facilities Display Board
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = LocalizationManager.getSanitationBoardTitle(lang),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Live telemetry & multi-stage escalation matrix",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(TealPrimary.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${facilities.size} Units",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TealPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Facility Cards with Stage 1 / Stage 2 badges on the right side
                        items(facilities, key = { it.id }) { fac ->
                            val activeEsc = escalations.firstOrNull { it.facilityId == fac.id && it.stage != EscalationStage.RESOLVED }
                            val activeDuty = duties.firstOrNull { it.facilityId == fac.id && it.status != DutyStatus.COMPLETED }

                            val stageBadgeText: String
                            val stageSubText: String
                            val stageBgColor: Color
                            val stageTextColor: Color

                            when {
                                activeEsc != null -> {
                                    when (activeEsc.stage) {
                                        EscalationStage.STAFF_DISPATCHED -> {
                                            stageBadgeText = "Stage 1"
                                            stageSubText = "Staff Dispatched"
                                            stageBgColor = Color(0xFFFFECB3)
                                            stageTextColor = Color(0xFFE65100)
                                        }
                                        EscalationStage.SUPERVISOR_NOTIFIED -> {
                                            stageBadgeText = "Stage 2"
                                            stageSubText = "Supervisor Escalated"
                                            stageBgColor = DangerRed.copy(alpha = 0.18f)
                                            stageTextColor = DangerRed
                                        }
                                        EscalationStage.ADMIN_ESCALATED -> {
                                            stageBadgeText = "Stage 3"
                                            stageSubText = "BBMP Central Alert"
                                            stageBgColor = Color(0xFFB71C1C).copy(alpha = 0.22f)
                                            stageTextColor = Color(0xFFB71C1C)
                                        }
                                        else -> {
                                            stageBadgeText = "Stage 1"
                                            stageSubText = "Active SLA"
                                            stageBgColor = ModerateYellow.copy(alpha = 0.2f)
                                            stageTextColor = Color(0xFFF57F17)
                                        }
                                    }
                                }
                                activeDuty != null -> {
                                    stageBadgeText = "Stage 1"
                                    stageSubText = "Duty: ${activeDuty.status.name}"
                                    stageBgColor = TealPrimary.copy(alpha = 0.15f)
                                    stageTextColor = TealPrimary
                                }
                                !fac.isMonitoredPublic -> {
                                    stageBadgeText = "Standard"
                                    stageSubText = "Non-IoT Civic"
                                    stageBgColor = Color(0xFFECEFF1)
                                    stageTextColor = Color(0xFF546E7A)
                                }
                                fac.cleanlinessBand == CleanlinessBand.DANGER_ZONE -> {
                                    stageBadgeText = "Stage 1"
                                    stageSubText = "Critical SLA Trigger"
                                    stageBgColor = DangerRed.copy(alpha = 0.18f)
                                    stageTextColor = DangerRed
                                }
                                fac.cleanlinessBand == CleanlinessBand.CLEAN -> {
                                    stageBadgeText = "Optimal"
                                    stageSubText = "Pristine Clean"
                                    stageBgColor = CleanGreen.copy(alpha = 0.18f)
                                    stageTextColor = CleanGreen
                                }
                                else -> {
                                    stageBadgeText = "Stable"
                                    stageSubText = "Moderate PCI"
                                    stageBgColor = ModerateYellow.copy(alpha = 0.2f)
                                    stageTextColor = Color(0xFFF57F17)
                                }
                            }

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    // Top Row: Facility name on left, Stage 1 / Stage 2 on right side
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                                            Text(
                                                text = fac.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "${fac.zone} • ${fac.address}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }

                                        // RIGHT SIDE: Prominent Single Horizontal Stage Display Badge (no vertical stacking or split lines)
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(stageBgColor)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            val fullBadgeText = if (stageBadgeText.startsWith("Stage")) "$stageBadgeText: $stageSubText" else stageSubText
                                            Text(
                                                text = fullBadgeText,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = stageTextColor,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Middle Row: PCI Gauge & Telemetry Streams
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        if (fac.isMonitoredPublic) {
                                            PciGauge(score = fac.pciScore, size = 44.dp, strokeWidth = 4.5.dp)
                                            Spacer(modifier = Modifier.width(12.dp))
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            if (fac.isMonitoredPublic) {
                                                Text(
                                                    text = "Ammonia: ${fac.ammoniaPpm} ppm • Flow: ${fac.flushFlowRate} L/m • Tank: ${fac.waterTankLevelPct}%",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = if (fac.pciScore < 30) DangerRed else MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            } else {
                                                Text(
                                                    text = "Standard Municipal Facility • No Live Sensor Scoring",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = "Staff: ${fac.assignedStaffName} (${fac.assignedStaffPhone})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Address in Words Banner (Before Navigation Bar)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                                            .padding(horizontal = 10.dp, vertical = 7.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Address",
                                            tint = TealPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "📍 Address: ${fac.address}, ${fac.zone}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Bottom Row: Turn-by-Turn Maps Navigation + Direct Dispatch
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedButton(
                                            onClick = { launchGoogleMaps(fac) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, TealPrimary),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                                        ) {
                                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp), tint = TealPrimary)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Navigate (Maps)", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }

                                        Button(
                                            onClick = {
                                                targetFacilityForDuty = fac
                                                dutyInitialInstructions = null
                                                showAssignDutyDialog = true
                                            },
                                            modifier = Modifier.weight(1.1f),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                        ) {
                                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(LocalizationManager.getDispatchDuty(lang), fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }

    if (showAssignDutyDialog) {
        AssignDutyDialog(
            facilities = facilities,
            initialFacility = targetFacilityForDuty,
            initialInstructions = dutyInitialInstructions,
            lang = lang,
            onDismiss = {
                showAssignDutyDialog = false
                dutyInitialInstructions = null
            },
            onDispatchDuty = { fId, fName, cubicle, sName, sPhone, prio, remarks ->
                onDispatchDuty(fId, fName, cubicle, sName, sPhone, prio, remarks)
                showAssignDutyDialog = false
                dutyInitialInstructions = null
                showSuccessToast = "Duty dispatched to $sName ($sPhone). Alert sent!"
            }
        )
    }
}

@Composable
private fun AdminStatCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White.copy(alpha = 0.9f)
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = Color.White.copy(alpha = 0.8f),
                maxLines = 1
            )
        }
    }
}
