package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.AppLanguage
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.DutyStatus
import com.example.data.model.EscalationEntity
import com.example.data.model.EscalationStage
import com.example.data.model.FacilityEntity
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary

@Composable
fun EscalationLadderView(
    currentStage: EscalationStage,
    lang: AppLanguage,
    escalations: List<EscalationEntity> = emptyList(),
    duties: List<CleaningDutyEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("escalation_ladder_view"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Escalation",
                    tint = DangerRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (lang) {
                        AppLanguage.KANNADA -> "ಅಪಾಯ ವಲಯ ಏರಿಕೆ ಹಂತಗಳು (FR15.1 - FR15.6)"
                        AppLanguage.HINDI -> "खतरा क्षेत्र स्वचालित वृद्धि सोपान (FR15.1 - FR15.6)"
                        AppLanguage.ENGLISH -> "Danger Zone Automated Escalation Ladder"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step 1: Staff Dispatched
            LadderStepItem(
                stepNumber = 1,
                title = when (lang) {
                    AppLanguage.KANNADA -> "ಹಂತ ೧: ಹತ್ತಿರದ ಸ್ವಚ್ಛತಾ ಸಿಬ್ಬಂದಿಗೆ ರವಾನೆ (೧೦ ನಿಮಿಷ ಗಡುವು)"
                    AppLanguage.HINDI -> "चरण 1: निकटतम कर्मचारी को कार्य आवंटन (10 मिनट विंडो)"
                    AppLanguage.ENGLISH -> "Stage 1: Nearest Staff Dispatched (10-min acceptance window)"
                },
                subtitle = "Pushed to Ramesh Babu (+91 98860 11223) via Distance Matrix ranking",
                icon = Icons.Default.Person,
                isActive = currentStage == EscalationStage.STAFF_DISPATCHED,
                isCompleted = currentStage != EscalationStage.STAFF_DISPATCHED,
                isLast = false
            )

            // Step 2: Supervisor Escalated
            LadderStepItem(
                stepNumber = 2,
                title = when (lang) {
                    AppLanguage.KANNADA -> "ಹಂತ ೨: ಮೇಲ್ವಿಚಾರಕರಿಗೆ ತುರ್ತು ಸೂಚನೆ"
                    AppLanguage.HINDI -> "चरण 2: पर्यवेक्षक को चेतावनी प्रेषित"
                    AppLanguage.ENGLISH -> "Stage 2: Toilet Supervisor Auto-Notified"
                },
                subtitle = "Facility Incharge Dr. Prakash Gowda (+91 98452 34567)",
                icon = Icons.Default.SupervisorAccount,
                isActive = currentStage == EscalationStage.SUPERVISOR_NOTIFIED,
                isCompleted = currentStage == EscalationStage.ADMIN_ESCALATED || currentStage == EscalationStage.RESOLVED,
                isLast = false
            )

            // Step 3: Admin Escalated
            LadderStepItem(
                stepNumber = 3,
                title = when (lang) {
                    AppLanguage.KANNADA -> "ಹಂತ ೩: ಬಿಬಿಎಂಪಿ ಆಡಳಿತ ಡ್ಯಾಶ್‌ಬೋರ್ಡ್ ಅಗ್ರ ಆದ್ಯತೆ"
                    AppLanguage.HINDI -> "चरण 3: बीबीएमपी नगर निगम नियंत्रण कक्ष में सर्वोच्च प्राथमिकता"
                    AppLanguage.ENGLISH -> "Stage 3: BBMP Central Admin Dashboard (Top Priority)"
                },
                subtitle = "Audit logged at /escalations with full supervisor audit trail",
                icon = Icons.Default.AdminPanelSettings,
                isActive = currentStage == EscalationStage.ADMIN_ESCALATED,
                isCompleted = currentStage == EscalationStage.RESOLVED,
                isLast = false
            )

            // Step 4: Resolved
            LadderStepItem(
                stepNumber = 4,
                title = when (lang) {
                    AppLanguage.KANNADA -> "ಹಂತ ೪: ಸ್ವಚ್ಛತೆ ಪರಿಶೀಲನೆ ಮತ್ತು ಫೋಟೋ ಪುರಾವೆ (ರೆಸಲ್ಯೂಷನ್)"
                    AppLanguage.HINDI -> "चरण 4: सफाई सत्यापन एवं फोटो प्रमाण द्वारा निवारण"
                    AppLanguage.ENGLISH -> "Stage 4: Photo Proof Verified & Restroom Cleared"
                },
                subtitle = "PCI restored back to Clean condition (>80)",
                icon = Icons.Default.CheckCircle,
                isActive = currentStage == EscalationStage.RESOLVED,
                isCompleted = currentStage == EscalationStage.RESOLVED,
                isLast = true
            )

            // Dropdown / Accordion Section for Active Toilet Blocks under Alert
            if (escalations.isNotEmpty() || duties.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Active Alert Toilet Blocks & Inspection Accordion",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val alertBlocks = if (escalations.isNotEmpty()) {
                    escalations.map { esc ->
                        val duty = duties.firstOrNull { it.facilityId == esc.facilityId }
                        esc to duty
                    }
                } else {
                    duties.map { duty ->
                        EscalationEntity(
                            id = "ESC-${duty.facilityId}",
                            alertId = "ALT-${duty.facilityId}",
                            facilityId = duty.facilityId,
                            facilityName = duty.facilityName,
                            initialPci = 24,
                            triggerCause = duty.triggerReason,
                            stage = EscalationStage.STAFF_DISPATCHED,
                            staffNotifiedTime = duty.dispatchTime,
                            auditLog = "Automatic escalation generated from active duty"
                        ) to duty
                    }
                }

                alertBlocks.forEach { (esc, duty) ->
                    ToiletBlockAlertAccordion(
                        escalation = esc,
                        duty = duty,
                        lang = lang
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun ToiletBlockAlertAccordion(
    escalation: EscalationEntity,
    duty: CleaningDutyEntity?,
    lang: AppLanguage
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = escalation.facilityName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val cleanCause = escalation.triggerCause
                        .replace("(SRS v2.0)", "")
                        .replace("SRS v2.0", "")
                        .replace("SRS V2.O", "")
                        .replace("SRS v2.O", "")
                        .replace("  ", " ")
                        .trim()
                    Text(
                        text = "Trigger: ${if (cleanCause.isNotBlank()) cleanCause else "Routine Sanitation Spike"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = DangerRed,
                        maxLines = 1
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when (escalation.stage) {
                                    EscalationStage.STAFF_DISPATCHED -> Color(0xFFFFECB3)
                                    EscalationStage.SUPERVISOR_NOTIFIED -> DangerRed.copy(alpha = 0.18f)
                                    EscalationStage.ADMIN_ESCALATED -> Color(0xFFB71C1C).copy(alpha = 0.2f)
                                    EscalationStage.RESOLVED -> CleanGreen.copy(alpha = 0.18f)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = escalation.stage.displayName.split(":")[0],
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (escalation.stage) {
                                EscalationStage.STAFF_DISPATCHED -> Color(0xFFE65100)
                                EscalationStage.SUPERVISOR_NOTIFIED, EscalationStage.ADMIN_ESCALATED -> DangerRed
                                EscalationStage.RESOLVED -> CleanGreen
                            }
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle Accordion",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(8.dp))

                    // Assigned Duty Info
                    if (duty != null) {
                        Text(
                            text = "Assigned Cleaning Duty:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TealPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Staff: ${duty.assignedStaffName} (${duty.assignedStaffPhone})",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (duty.status) {
                                            DutyStatus.DISPATCHED -> DangerRed.copy(alpha = 0.15f)
                                            DutyStatus.ACCEPTED -> ModerateYellow.copy(alpha = 0.15f)
                                            DutyStatus.IN_PROGRESS -> TealPrimary.copy(alpha = 0.15f)
                                            DutyStatus.COMPLETED -> CleanGreen.copy(alpha = 0.15f)
                                            else -> DangerRed.copy(alpha = 0.15f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = duty.status.name,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (duty.status) {
                                        DutyStatus.DISPATCHED -> DangerRed
                                        DutyStatus.ACCEPTED -> ModerateYellow
                                        DutyStatus.IN_PROGRESS -> TealPrimary
                                        DutyStatus.COMPLETED -> CleanGreen
                                        else -> DangerRed
                                    }
                                )
                            }
                        }

                        Text(
                            text = "Cubicle: ${duty.cubicle} • Priority: ${duty.priority}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Review Uploaded Inspection Images
                        Text(
                            text = "Review Uploaded Inspection Images:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val hasValidPhoto = !duty.proofPhotoUri.isNullOrBlank() && 
                            (duty.proofPhotoUri.startsWith("http") || duty.proofPhotoUri.startsWith("content://") || duty.proofPhotoUri.startsWith("file://") || duty.proofPhotoUri.startsWith("data:image"))

                        if (hasValidPhoto) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.05f))
                            ) {
                                val imageModel = remember(duty.proofPhotoUri) {
                                    val uri = duty.proofPhotoUri
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
                                    contentDescription = "Uploaded Inspection Proof",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = "✓ Cleaning Verified by Staff",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (duty.status == DutyStatus.COMPLETED) "Inspection photo verified on server" else "Inspection photo upload pending (Duty in progress)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No duty assigned yet. Staff auto-dispatch active.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LadderStepItem(
    stepNumber: Int,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isActive: Boolean,
    isCompleted: Boolean,
    isLast: Boolean
) {
    val stepColor = when {
        isCompleted -> CleanGreen
        isActive -> DangerRed
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(stepColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(28.dp)
                        .background(if (isCompleted) CleanGreen else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else 14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isActive) DangerRed else if (isCompleted) CleanGreen else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
