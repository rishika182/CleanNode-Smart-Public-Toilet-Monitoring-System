package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Accessible
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import java.io.File
import java.io.FileOutputStream
import com.example.data.model.AppLanguage
import com.example.data.model.CleanlinessBand
import com.example.data.model.FacilityEntity
import com.example.data.model.ReportCategory
import com.example.localization.LocalizationManager
import com.example.ui.components.BengaluruRestroomMap
import com.example.ui.components.PciGauge
import com.example.ui.components.SensorGrid
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary

@Composable
fun CitizenDashboardScreen(
    facilities: List<FacilityEntity>,
    selectedFacility: FacilityEntity?,
    lang: AppLanguage,
    selectedTab: Int = 0, // 0 = MAP, 1 = LIST
    onSelectFacility: (FacilityEntity) -> Unit,
    onSubmitReport: (facilityId: String, name: String, rating: Int, category: ReportCategory, comment: String, photoUri: String?) -> Unit,
    onRequestAiDiagnostics: (FacilityEntity) -> Unit = {},
    onNavigateToMap: (FacilityEntity) -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") }
    var reportingFacility by remember { mutableStateOf<FacilityEntity?>(null) }

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

    val filteredList = remember(facilities, selectedFilter) {
        when (selectedFilter) {
            "PUBLIC" -> facilities.filter { it.isMonitoredPublic }
            "CLEAN" -> facilities.filter { it.isMonitoredPublic && it.pciScore >= 80 }
            "ACCESSIBLE" -> facilities.filter { it.amenities.contains("Wheelchair", true) || it.amenities.contains("Access", true) }
            else -> facilities
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedTab == 0) {
            BengaluruRestroomMap(
                facilities = filteredList,
                selectedFacility = selectedFacility,
                lang = lang,
                onSelectFacility = { fac -> if (fac != null) onSelectFacility(fac) },
                onNavigate = { launchGoogleMaps(it) },
                userRole = com.example.data.model.UserRole.CITIZEN,
                onReportIssue = { reportingFacility = it },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .testTag("citizen_dashboard_list")
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Filter Chips horizontally scrollable with compact padding
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == "ALL",
                            onClick = { selectedFilter = "ALL" },
                            label = { Text("All", fontSize = 12.sp, maxLines = 1) },
                            modifier = Modifier.testTag("filter_all")
                        )
                        FilterChip(
                            selected = selectedFilter == "PUBLIC",
                            onClick = { selectedFilter = "PUBLIC" },
                            label = { Text("Public IoT", fontSize = 12.sp, maxLines = 1) },
                            modifier = Modifier.testTag("filter_public")
                        )
                        FilterChip(
                            selected = selectedFilter == "CLEAN",
                            onClick = { selectedFilter = "CLEAN" },
                            label = { Text("Clean (80+)", fontSize = 12.sp, maxLines = 1) },
                            modifier = Modifier.testTag("filter_clean")
                        )
                        FilterChip(
                            selected = selectedFilter == "ACCESSIBLE",
                            onClick = { selectedFilter = "ACCESSIBLE" },
                            label = { Text("Accessible", fontSize = 12.sp, maxLines = 1) },
                            modifier = Modifier.testTag("filter_accessible")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(filteredList, key = { it.id }) { facility ->
                    FacilityCard(
                        facility = facility,
                        lang = lang,
                        onCardClick = { onSelectFacility(facility) },
                        onNavigate = { launchGoogleMaps(facility) },
                        onReport = { reportingFacility = facility }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Citizen Feedback / Report Dialog
    if (reportingFacility != null) {
        CitizenFeedbackDialog(
            facility = reportingFacility!!,
            lang = lang,
            onSubmit = { rating, cat, comment, photoUri ->
                onSubmitReport(reportingFacility!!.id, reportingFacility!!.name, rating, cat, comment, photoUri)
                reportingFacility = null
            },
            onDismiss = { reportingFacility = null }
        )
    }
}

@Composable
fun FacilityCard(
    facility: FacilityEntity,
    lang: AppLanguage,
    onCardClick: () -> Unit,
    onNavigate: () -> Unit,
    onReport: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("facility_card_${facility.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Type Badge + Distance ETA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cleanlinessColor = when (facility.cleanlinessBand) {
                    CleanlinessBand.CLEAN -> CleanGreen
                    CleanlinessBand.MODERATE -> ModerateYellow
                    CleanlinessBand.NEEDS_ATTENTION -> Color(0xFFF57C00)
                    CleanlinessBand.DANGER_ZONE -> DangerRed
                }
                val cleanlinessLabel = when (facility.cleanlinessBand) {
                    CleanlinessBand.CLEAN -> "Clean / Spotless"
                    CleanlinessBand.MODERATE -> "Moderate Cleanliness"
                    CleanlinessBand.NEEDS_ATTENTION -> "Needs Attention"
                    CleanlinessBand.DANGER_ZONE -> "Dirty / Urgent"
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(cleanlinessColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(cleanlinessColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (facility.isMonitoredPublic) cleanlinessLabel else "Public Restroom",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = cleanlinessColor
                        )
                    }
                }

                // Distance & Walking ETA
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val walkMinutes = (facility.distanceMeters / 70).coerceAtLeast(1)
                    Text(
                        text = "${facility.distanceMeters}m (~$walkMinutes min)",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Body Row: Facility Name + PCI Gauge (or Unmonitored placeholder)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = facility.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = facility.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Hours: ${facility.openingHours}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                if (facility.isMonitoredPublic) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        PciGauge(score = facility.pciScore, size = 68.dp, strokeWidth = 7.dp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = LocalizationManager.getBandLabel(facility.cleanlinessBand, lang),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            ),
                            color = when (facility.cleanlinessBand) {
                                CleanlinessBand.CLEAN -> CleanGreen
                                CleanlinessBand.MODERATE -> ModerateYellow
                                CleanlinessBand.NEEDS_ATTENTION -> Color(0xFFF57C00)
                                CleanlinessBand.DANGER_ZONE -> DangerRed
                            }
                        )
                    }
                } else {
                    // Strictly required by FR14.3: Unmonitored private restrooms NEVER show a score!
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LocalizationManager.getCleanlinessDataUnavailable(lang),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Amenities tag row
            if (facility.amenities.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Amenities: ${facility.amenities}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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

            // Action Buttons (Wide Navigate + Compact Report, no clustering)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onNavigate,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("btn_navigate_${facility.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, TealPrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate to Facility (Google Maps)",
                        modifier = Modifier.size(16.dp),
                        tint = TealPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Navigate to Facility (Google Maps)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TealPrimary,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                OutlinedButton(
                    onClick = onReport,
                    modifier = Modifier
                        .weight(0.7f)
                        .testTag("btn_report_${facility.id}"),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = "Report Issue",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Report",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }
}

@Composable
fun CitizenFeedbackDialog(
    facility: FacilityEntity,
    lang: AppLanguage,
    onSubmit: (rating: Int, category: ReportCategory, comment: String, photoUri: String?) -> Unit,
    onDismiss: () -> Unit
) {
    var rating by remember { mutableIntStateOf(4) }
    var selectedCategory by remember { mutableStateOf(ReportCategory.ODOR) }
    var comment by remember { mutableStateOf("") }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessingImage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            isProcessingImage = true
            coroutineScope.launch {
                capturedBitmap = bitmap
                isProcessingImage = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {}
        }
    }

    fun launchCameraSafe() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {}
        } else {
            try {
                permissionLauncher.launch(android.Manifest.permission.CAMERA)
            } catch (_: Exception) {}
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            isProcessingImage = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            capturedBitmap = bitmap
                        }
                        isProcessingImage = false
                    }
                } catch (_: Exception) {
                    withContext(Dispatchers.Main) {
                        isProcessingImage = false
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "${LocalizationManager.getReportIssue(lang)}: ${facility.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Your feedback and photo alerts BBMP Supervisor & Admin",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Star Rating
                Text(
                    text = "Rate Cleanliness: $rating ★ ${when (rating) {
                        1 -> "(Very Poor / Dirty)"
                        2 -> "(Poor / Needs Attention)"
                        3 -> "(Fair / Average)"
                        4 -> "(Good / Clean)"
                        else -> "(Excellent / Spotless)"
                    }}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(onClick = { rating = star }) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= rating) ModerateYellow else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Issue Category:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(ReportCategory.ODOR, ReportCategory.CLOG, ReportCategory.WATER_SHORTAGE).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(ReportCategory.CLEANLINESS, ReportCategory.SAFETY).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat.displayName, fontSize = 10.sp, maxLines = 1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Describe the issue (e.g. odor, tap broken)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Photo Upload Section
                Text(
                    text = "Attach Photo Evidence:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (isProcessingImage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = TealPrimary,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Processing photo preview...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else if (capturedBitmap != null) {
                    // Preview captured image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Image(
                            bitmap = capturedBitmap!!.asImageBitmap(),
                            contentDescription = "Report Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        IconButton(
                            onClick = { capturedBitmap = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp)
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchCameraSafe() },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Take Photo", fontSize = 11.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    galleryLauncher.launch("image/*")
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("From Gallery", fontSize = 11.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var photoUriStr: String? = null
                    if (capturedBitmap != null) {
                        try {
                            val file = File(context.cacheDir, "report_${System.currentTimeMillis()}.jpg")
                            val stream = FileOutputStream(file)
                            capturedBitmap!!.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                            stream.flush()
                            stream.close()
                            photoUriStr = "file://${file.absolutePath}"
                        } catch (_: Exception) {}
                    }
                    onSubmit(rating, selectedCategory, comment, photoUriStr)
                },
                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
            ) {
                Text("Submit Feedback")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
