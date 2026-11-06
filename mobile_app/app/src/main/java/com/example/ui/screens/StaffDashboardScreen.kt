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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.CleanlinessBand
import com.example.data.model.DutyStatus
import com.example.data.model.FacilityEntity
import com.example.localization.LocalizationManager
import com.example.ui.components.BengaluruRestroomMap
import com.example.ui.components.PciGauge
import com.example.ui.components.ProofUploadDialog
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay

@Composable
fun StaffDashboardScreen(
    duties: List<CleaningDutyEntity>,
    facilities: List<FacilityEntity>,
    lang: AppLanguage,
    selectedTab: Int = 0, // 0 = DUTIES, 1 = MAP
    onAcceptDuty: (String) -> Unit,
    onDeclineDuty: (String) -> Unit,
    onSimulateTimeout: (String) -> Unit,
    onSubmitProof: (dutyId: String, photoUri: String, notes: String, mopped: Boolean, disinfected: Boolean, waterChecked: Boolean, soapRefilled: Boolean, trashEmptied: Boolean) -> Unit,
    onStartDutyForFacility: (facility: FacilityEntity, onStarted: (CleaningDutyEntity) -> Unit) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var activeDutyForProof by remember { mutableStateOf<CleaningDutyEntity?>(null) }
    var selectedFacilityOnMap by remember { mutableStateOf<FacilityEntity?>(null) }

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

    // Always-working Start Duty handler
    val handleStartDuty = { fac: FacilityEntity ->
        val existing = duties.firstOrNull {
            it.facilityId == fac.id &&
            it.status != DutyStatus.COMPLETED &&
            it.status != DutyStatus.REJECTED
        }
        if (existing != null) {
            if (existing.status == DutyStatus.DISPATCHED) {
                onAcceptDuty(existing.id)
            }
            activeDutyForProof = existing
        } else {
            onStartDutyForFacility(fac) { createdDuty ->
                activeDutyForProof = createdDuty
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTab == 1) {
            // Tab 1: Leaflet Interactive Bengaluru Map with in-app routing
            BengaluruRestroomMap(
                facilities = facilities,
                selectedFacility = selectedFacilityOnMap,
                lang = lang,
                onSelectFacility = { selectedFacilityOnMap = it },
                onNavigate = { launchGoogleMaps(it) },
                userRole = com.example.data.model.UserRole.STAFF,
                onAcceptDuty = { fac -> handleStartDuty(fac) },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("staff_dashboard_list")
            ) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))

                    // Offline Support Banner (SRS v2.0 FR13.4)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanGreen.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDone,
                                contentDescription = "Offline Cache",
                                tint = CleanGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = LocalizationManager.getOfflineBanner(lang),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CleanGreen
                                )
                                Text(
                                    text = "Offline Mode Enabled • Work in basements or zero-connectivity restrooms",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Active Duties Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${LocalizationManager.getActiveCleaningDuties(lang)} (${duties.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (duties.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(TealPrimary.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${duties.count { it.status == DutyStatus.DISPATCHED }} Pending",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TealPrimary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                // If duties exist, show duty cards
                if (duties.isNotEmpty()) {
                    items(duties, key = { it.id }) { duty ->
                        val matchingFacility = facilities.firstOrNull { it.id == duty.facilityId }
                        StaffDutyCard(
                            duty = duty,
                            facility = matchingFacility,
                            lang = lang,
                            onAccept = {
                                onAcceptDuty(duty.id)
                                activeDutyForProof = duty.copy(status = DutyStatus.IN_PROGRESS)
                            },
                            onDecline = { onDeclineDuty(duty.id) },
                            onSimulateTimeout = { onSimulateTimeout(duty.id) },
                            onStartProof = { activeDutyForProof = duty },
                            onNavigate = { fac -> launchGoogleMaps(fac) }
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // OFFLINE / STANDBY FACILITY CARDS
                // If device is in offline mode or has zero active duties, show the assigned facility cards
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (duties.isEmpty()) "Assigned Restrooms (Offline Maintenance Roster)" else "Assigned Restroom Facilities",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "You can navigate and start cleaning on-demand even in offline mode",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(facilities.take(if (duties.isEmpty()) 5 else 2), key = { "offline_fac_${it.id}" }) { fac ->
                    StaffFacilityCard(
                        facility = fac,
                        lang = lang,
                        onStartCleaning = { handleStartDuty(fac) },
                        onNavigate = { launchGoogleMaps(fac) }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Proof of Cleaning Upload Dialog
        if (activeDutyForProof != null) {
            ProofUploadDialog(
                facilityName = activeDutyForProof!!.facilityName,
                lang = lang,
                onConfirm = { photoUri, notes, mopped, disinfected, waterChecked, soapRefilled, trashEmptied ->
                    onSubmitProof(
                        activeDutyForProof!!.id,
                        photoUri,
                        notes,
                        mopped,
                        disinfected,
                        waterChecked,
                        soapRefilled,
                        trashEmptied
                    )
                    activeDutyForProof = null
                },
                onDismiss = { activeDutyForProof = null }
            )
        }
    }
}

/**
 * Offline Mode / Standby Facility Card for Field Staff.
 * Displays restroom data, unified navigation button, and Start Cleaning button.
 */
@Composable
fun StaffFacilityCard(
    facility: FacilityEntity,
    lang: AppLanguage,
    onStartCleaning: () -> Unit,
    onNavigate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Name & Offline Standby Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                    Text(
                        text = facility.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${facility.zone} • ${facility.address}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(TealPrimary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Standby / Offline",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Telemetry Streams
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (facility.isMonitoredPublic) {
                    PciGauge(score = facility.pciScore, size = 42.dp, strokeWidth = 4.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    if (facility.isMonitoredPublic) {
                        Text(
                            text = "Ammonia: ${facility.ammoniaPpm} ppm • Flow: ${facility.flushFlowRate} L/m • Tank: ${facility.waterTankLevelPct}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (facility.pciScore < 30) DangerRed else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(
                            text = "Standard Restroom • Distance: ~${facility.distanceMeters}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "Assigned In-Charge: ${facility.assignedStaffName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

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
                    text = "📍 Address: ${facility.address}, ${facility.zone}",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Unified Outlined Navigation Button
                OutlinedButton(
                    onClick = onNavigate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TealPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp), tint = TealPrimary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Navigate (Maps)", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                }

                // Start Cleaning / Duty Button
                Button(
                    onClick = onStartCleaning,
                    modifier = Modifier.weight(1.1f).testTag("btn_staff_start_offline_${facility.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CleanGreen)
                ) {
                    Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Start Cleaning", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                }
            }
        }
    }
}

@Composable
fun StaffDutyCard(
    duty: CleaningDutyEntity,
    facility: FacilityEntity? = null,
    lang: AppLanguage,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onSimulateTimeout: () -> Unit,
    onStartProof: () -> Unit,
    onNavigate: (FacilityEntity) -> Unit = {}
) {
    var remainingSeconds by remember { mutableIntStateOf(420) }

    LaunchedEffect(duty.status) {
        if (duty.status == DutyStatus.DISPATCHED) {
            while (remainingSeconds > 0) {
                delay(1000L)
                remainingSeconds--
            }
        }
    }

    val statusColor = when (duty.status) {
        DutyStatus.DISPATCHED -> DangerRed
        DutyStatus.ACCEPTED, DutyStatus.IN_PROGRESS -> ModerateYellow
        DutyStatus.COMPLETED -> CleanGreen
        DutyStatus.REJECTED, DutyStatus.ESCALATED -> DangerRed
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("duty_card_${duty.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Status: ${duty.status.name}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = statusColor
                    )
                }

                if (duty.status == DutyStatus.DISPATCHED) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Timer",
                            tint = DangerRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        Text(
                            text = "%02d:%02d left".format(mins, secs),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = DangerRed
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "${duty.facilityName} (${duty.cubicle})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            // Direct Unified Google Maps Navigation Button
            if (facility != null) {
                Spacer(modifier = Modifier.height(8.dp))

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
                        text = "📍 Address: ${facility.address}, ${facility.zone}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onNavigate(facility) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TealPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp), tint = TealPrimary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Navigate to Facility (Google Maps)", fontSize = 12.sp, color = TealPrimary, fontWeight = FontWeight.Bold)
                }
            }

            if (duty.status == DutyStatus.DISPATCHED) {
                Spacer(modifier = Modifier.height(8.dp))
                val displayReason = duty.triggerReason
                    .replace("(SRS v2.0)", "")
                    .replace("SRS v2.0", "")
                    .replace("SRS V2.O", "")
                    .replace("SRS v2.O", "")
                    .replace("  ", " ")
                    .trim()
                Text(
                    text = "Reason: $displayReason",
                    style = MaterialTheme.typography.bodySmall,
                    color = DangerRed
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons according to state
            when (duty.status) {
                DutyStatus.DISPATCHED -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onAccept,
                                modifier = Modifier.weight(1.2f).testTag("btn_accept_duty"),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CleanGreen)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Start & Accept", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = onDecline,
                                modifier = Modifier.weight(0.8f).testTag("btn_decline_duty"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(LocalizationManager.getDeclineDuty(lang))
                            }
                        }

                        // Simulation button for examiner demo (simulate 10m timeout -> supervisor)
                        OutlinedButton(
                            onClick = onSimulateTimeout,
                            modifier = Modifier.fillMaxWidth().testTag("btn_simulate_timeout"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = DangerRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simulate 10-Min Timeout (Escalate to Supervisor)", fontSize = 11.sp, color = DangerRed)
                        }
                    }
                }

                DutyStatus.ACCEPTED, DutyStatus.IN_PROGRESS -> {
                    Button(
                        onClick = onStartProof,
                        modifier = Modifier.fillMaxWidth().testTag("btn_open_proof_upload"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = LocalizationManager.getProofOfCleaning(lang),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                DutyStatus.COMPLETED -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CleanGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Cleaning verified & photo proof attached. Restroom PCI restored to ${duty.aiSanitationScore ?: 96}.",
                            style = MaterialTheme.typography.bodySmall,
                            color = CleanGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                DutyStatus.REJECTED, DutyStatus.ESCALATED -> {
                    Text(
                        text = "Escalated to Toilet Supervisor (Alert sent)",
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
