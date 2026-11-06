package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CleanlinessBand
import com.example.data.model.FacilityEntity
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary

data class StaffContact(
    val name: String,
    val phone: String,
    val zone: String,
    val status: String
)

val defaultStaffList = listOf(
    StaffContact("Ramesh Babu", "+91 98860 11223", "South Zone", "Available"),
    StaffContact("Fatima", "+91 98862 33003", "South Zone", "Available"),
    StaffContact("Girish", "+91 98453 44004", "South Zone", "On Duty"),
    StaffContact("Manjunath K", "+91 98455 66778", "Central Zone", "Available"),
    StaffContact("Ravi Teja", "+91 98456 77889", "Central Zone", "Available")
)

@Composable
fun AssignDutyDialog(
    facilities: List<FacilityEntity>,
    initialFacility: FacilityEntity? = null,
    initialInstructions: String? = null,
    lang: AppLanguage,
    onDismiss: () -> Unit,
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
    val monitoredFacilities = remember(facilities) {
        facilities.filter { it.isMonitoredPublic }
    }

    var selectedFacility by remember {
        mutableStateOf(initialFacility ?: monitoredFacilities.firstOrNull())
    }

    var selectedCubicle by remember { mutableStateOf("All Bays / Full Facility") }
    val cubicleOptions = listOf(
        "All Bays / Full Facility",
        "Cubicle 1 (Urinal Bay)",
        "Cubicle 2 (Indian Stall)",
        "Cubicle 3 (Western Stall)"
    )

    var selectedStaff by remember {
        mutableStateOf(
            if (initialFacility != null) {
                StaffContact(
                    name = initialFacility.assignedStaffName,
                    phone = initialFacility.assignedStaffPhone,
                    zone = initialFacility.zone,
                    status = "Cluster Assigned"
                )
            } else {
                defaultStaffList[0]
            }
        )
    }

    var priority by remember {
        mutableStateOf(
            if (initialFacility?.cleanlinessBand == CleanlinessBand.DANGER_ZONE) "CRITICAL" else "HIGH"
        )
    }

    var instructions by remember {
        mutableStateOf(
            initialInstructions ?: if (initialFacility?.cleanlinessBand == CleanlinessBand.DANGER_ZONE) {
                "URGENT: PCI dropped below 30. Deep disinfect bowls, mop floor, and verify flush flow."
            } else {
                "Regular sanitization checklist: mop floor, refill soap, verify water supply."
            }
        )
    }

    var isDispatchedFeedback by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("assign_duty_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealPrimary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CleaningServices,
                            contentDescription = null,
                            tint = TealPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dispatch Staff Duty",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Admin & Supervisor Command Action",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                if (isDispatchedFeedback) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = CleanGreen.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, CleanGreen)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CleanGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Duty Dispatched Successfully!",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CleanGreen
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Duty alert dispatched to ${selectedStaff.name} (${selectedStaff.phone}). 10-minute acceptance SLA active.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // 1. Select Facility
                Text(
                    text = "1. Target Restroom Facility",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    monitoredFacilities.take(5).forEach { facility ->
                        val isSelected = facility.id == selectedFacility?.id
                        val isDanger = facility.cleanlinessBand == CleanlinessBand.DANGER_ZONE

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedFacility = facility
                                    selectedStaff = StaffContact(
                                        name = facility.assignedStaffName,
                                        phone = facility.assignedStaffPhone,
                                        zone = facility.zone,
                                        status = "Cluster Assigned"
                                    )
                                    if (facility.cleanlinessBand == CleanlinessBand.DANGER_ZONE) {
                                        priority = "CRITICAL"
                                        instructions = "URGENT: PCI dropped to ${facility.pciScore}/100. Disinfect bowls, check ammonia, clear blockages."
                                    }
                                }
                                .testTag("duty_facility_${facility.id}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) TealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, TealPrimary) else if (isDanger) BorderStroke(1.dp, DangerRed) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = facility.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (isDanger) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DangerRed)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text("DANGER", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${facility.zone} • ${facility.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when (facility.cleanlinessBand) {
                                                CleanlinessBand.CLEAN -> CleanGreen
                                                CleanlinessBand.MODERATE -> ModerateYellow
                                                CleanlinessBand.NEEDS_ATTENTION -> Color(0xFFFB8C00)
                                                CleanlinessBand.DANGER_ZONE -> DangerRed
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${facility.pciScore}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Cubicle / Bay Selection
                Text(
                    text = "2. Specific Cubicle / Bay",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cubicleOptions.take(2).forEach { option ->
                        val isSelected = selectedCubicle == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { selectedCubicle = option }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.split(" ")[0],
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    cubicleOptions.drop(2).forEach { option ->
                        val isSelected = selectedCubicle == option
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) TealPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { selectedCubicle = option }
                                .padding(vertical = 8.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Assign Staff Member
                Text(
                    text = "3. Assign Maintenance Staff (Receives Duty Alert)",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    defaultStaffList.forEach { staff ->
                        val isSelected = selectedStaff.name == staff.name
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedStaff = staff }
                                .testTag("staff_select_${staff.name.replace(" ", "_")}"),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) TealPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            ),
                            border = if (isSelected) BorderStroke(1.5.dp, TealPrimary) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = staff.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "📱 ${staff.phone} • ${staff.zone}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (staff.status == "Available") CleanGreen.copy(alpha = 0.15f) else ModerateYellow.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = staff.status,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (staff.status == "Available") CleanGreen else ModerateYellow
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Priority Level
                Text(
                    text = "4. Priority Level",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val priorities = listOf("CRITICAL", "HIGH", "ROUTINE", "REFILL")
                    priorities.forEach { p ->
                        val isSelected = priority == p
                        val color = when (p) {
                            "CRITICAL" -> DangerRed
                            "HIGH" -> Color(0xFFFB8C00)
                            "ROUTINE" -> CleanGreen
                            else -> TealPrimary
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) color else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .clickable { priority = p }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Instructions / Checklist
                Text(
                    text = "5. Instructions & Cleaning Notes",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = instructions,
                    onValueChange = { instructions = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_duty_instructions"),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(14.dp))

                // SMS & SLA Notice Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📨", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Duty notification will be dispatched immediately to ${selectedStaff.phone}. The 10-minute SLA acceptance countdown timer will begin automatically.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val facility = selectedFacility ?: return@Button
                    onDispatchDuty(
                        facility.id,
                        facility.name,
                        selectedCubicle,
                        selectedStaff.name,
                        selectedStaff.phone,
                        priority,
                        instructions
                    )
                    isDispatchedFeedback = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_confirm_dispatch_duty")
            ) {
                Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Dispatch Duty", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Close")
            }
        }
    )
}
