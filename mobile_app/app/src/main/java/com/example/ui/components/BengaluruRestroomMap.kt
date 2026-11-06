package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.WheelchairPickup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.AppLanguage
import com.example.data.model.CleanlinessBand
import com.example.data.model.FacilityEntity
import com.example.data.model.UserRole
import com.example.ui.theme.CleanGreen
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.TealPrimary
import org.json.JSONArray
import org.json.JSONObject

/**
 * Native Leaflet-powered Bengaluru Restroom Map Component.
 * Uses local asset map.html for clean, fast, zero-iframe in-app map rendering.
 * Supports native in-app walking route rendering and full external Google Maps turn-by-turn.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BengaluruRestroomMap(
    facilities: List<FacilityEntity>,
    selectedFacility: FacilityEntity?,
    lang: AppLanguage,
    onSelectFacility: (FacilityEntity?) -> Unit,
    onNavigate: (FacilityEntity) -> Unit = {},
    userRole: UserRole = UserRole.CITIZEN,
    onReportIssue: ((FacilityEntity) -> Unit)? = null,
    onAcceptDuty: ((FacilityEntity) -> Unit)? = null,
    onAssignDuty: ((FacilityEntity) -> Unit)? = null,
    onViewTelemetry: ((FacilityEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var currentFocusedFacility by remember(selectedFacility) {
        mutableStateOf(selectedFacility ?: facilities.firstOrNull())
    }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isRouteDrawnInApp by remember { mutableStateOf(false) }

    val currentFac = currentFocusedFacility ?: facilities.firstOrNull()

    fun injectFacilities(wv: WebView?) {
        if (wv == null || facilities.isEmpty()) return
        val jsonArray = JSONArray()
        facilities.forEach { fac ->
            val obj = JSONObject().apply {
                put("id", fac.id)
                put("name", fac.name)
                put("address", fac.address)
                put("lat", fac.latitude)
                put("lng", fac.longitude)
                put("isMonitored", fac.isMonitoredPublic)
                put("pci", fac.pciScore)
                put("color", when (fac.cleanlinessBand) {
                    CleanlinessBand.CLEAN -> "#2E7D32"
                    CleanlinessBand.MODERATE -> "#F57C00"
                    CleanlinessBand.NEEDS_ATTENTION -> "#E65100"
                    CleanlinessBand.DANGER_ZONE -> "#D32F2F"
                })
                put("distanceMeters", fac.distanceMeters)
            }
            jsonArray.put(obj)
        }
        wv.evaluateJavascript("if (window.updateFacilities) { window.updateFacilities($jsonArray); }", null)
    }

    LaunchedEffect(facilities) {
        webViewRef?.let { injectFacilities(it) }
    }

    LaunchedEffect(currentFocusedFacility) {
        val fac = currentFocusedFacility
        if (fac != null && webViewRef != null) {
            webViewRef?.evaluateJavascript(
                "if (window.selectAndFocusFacility) { window.selectAndFocusFacility('${fac.id}', ${fac.latitude}, ${fac.longitude}); }",
                null
            )
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Local Leaflet Map WebView (No external iframe, high performance, custom styled pins)
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    setBackgroundColor(0xFFE8ECEF.toInt())

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = true
                    settings.allowContentAccess = true
                    @Suppress("DEPRECATION")
                    settings.allowFileAccessFromFileURLs = true
                    @Suppress("DEPRECATION")
                    settings.allowUniversalAccessFromFileURLs = true
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    settings.userAgentString = "CleanNodeApp/1.0 (MobileRestroomDashboard; Android)"
                    settings.setSupportZoom(true)
                    settings.builtInZoomControls = true
                    settings.displayZoomControls = false
                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT

                    class AndroidBridge {
                        @JavascriptInterface
                        fun onSelectFacility(facilityId: String) {
                            post {
                                val match = facilities.firstOrNull { it.id == facilityId }
                                if (match != null) {
                                    currentFocusedFacility = match
                                    onSelectFacility(match)
                                }
                            }
                        }

                        @JavascriptInterface
                        fun onRouteDrawn(facId: String) {
                            post {
                                isRouteDrawnInApp = true
                            }
                        }

                        @JavascriptInterface
                        fun onMapReady() {
                            post {
                                injectFacilities(this@apply)
                                if (currentFac != null) {
                                    evaluateJavascript("if (window.selectAndFocusFacility) { window.selectAndFocusFacility('${currentFac.id}', ${currentFac.latitude}, ${currentFac.longitude}); }", null)
                                }
                                evaluateJavascript("if (window.map) { window.map.invalidateSize(true); }", null)
                            }
                        }
                    }

                    addJavascriptInterface(AndroidBridge(), "AndroidBridge")

                    webChromeClient = object : android.webkit.WebChromeClient() {
                        override fun onConsoleMessage(message: android.webkit.ConsoleMessage?): Boolean {
                            android.util.Log.d("CleanNodeMap", "${message?.message()} -- line ${message?.lineNumber()}")
                            return true
                        }
                    }

                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            injectFacilities(view)
                            view?.postDelayed({
                                view.evaluateJavascript("if (window.map) { window.map.invalidateSize(true); }", null)
                            }, 150)
                            view?.postDelayed({
                                view.evaluateJavascript("if (window.map) { window.map.invalidateSize(true); }", null)
                            }, 500)
                        }
                    }

                    loadUrl("file:///android_asset/map.html")
                    webViewRef = this
                }
            },
            update = { wv ->
                webViewRef = wv
                injectFacilities(wv)
                wv.post {
                    wv.evaluateJavascript("if (window.map) { window.map.invalidateSize(true); }", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Restroom Quick Selector Pills
        if (facilities.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(facilities, key = { it.id }) { fac ->
                    val isSelected = currentFocusedFacility?.id == fac.id
                    val statusColor = when (fac.cleanlinessBand) {
                        CleanlinessBand.CLEAN -> CleanGreen
                        CleanlinessBand.MODERATE -> ModerateYellow
                        CleanlinessBand.NEEDS_ATTENTION -> Color(0xFFF57C00)
                        CleanlinessBand.DANGER_ZONE -> DangerRed
                    }
                    Card(
                        modifier = Modifier
                            .clickable {
                                currentFocusedFacility = fac
                                onSelectFacility(fac)
                                webViewRef?.evaluateJavascript(
                                    "if (window.selectAndFocusFacility) { window.selectAndFocusFacility('${fac.id}', ${fac.latitude}, ${fac.longitude}); }",
                                    null
                                )
                            }
                            .testTag("map_pill_${fac.id}"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TealPrimary else MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) Color.White else statusColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = fac.name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Floating Bottom Card for Selected Facility
        if (currentFac != null) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header: Thumbnail + Name + Cleanliness Label
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusColor = when (currentFac.cleanlinessBand) {
                            CleanlinessBand.CLEAN -> CleanGreen
                            CleanlinessBand.MODERATE -> ModerateYellow
                            CleanlinessBand.NEEDS_ATTENTION -> Color(0xFFF57C00)
                            CleanlinessBand.DANGER_ZONE -> DangerRed
                        }
                        val statusLabel = when (currentFac.cleanlinessBand) {
                            CleanlinessBand.CLEAN -> "Clean / Spotless"
                            CleanlinessBand.MODERATE -> "Moderate Cleanliness"
                            CleanlinessBand.NEEDS_ATTENTION -> "Needs Attention"
                            CleanlinessBand.DANGER_ZONE -> "Dirty / Urgent"
                        }

                        // Status Color Indicator Box
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(statusColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userRole != UserRole.CITIZEN && currentFac.isMonitoredPublic) "${currentFac.pciScore}" else "🚻",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentFac.name,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(statusColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = statusColor
                                )
                                if (currentFac.amenities.contains("Wheelchair", ignoreCase = true) || currentFac.amenities.contains("Access", ignoreCase = true)) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(Icons.Default.WheelchairPickup, contentDescription = "Accessible", tint = TealPrimary, modifier = Modifier.size(14.dp))
                                }
                            }
                        }

                        // Close button
                        IconButton(
                            onClick = {
                                currentFocusedFacility = null
                                webViewRef?.evaluateJavascript("if (window.clearWalkingRoute) { window.clearWalkingRoute(); }", null)
                                isRouteDrawnInApp = false
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Address & Distance
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentFac.address,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 1
                        )
                        Text(
                            text = "🚶 ${currentFac.distanceMeters}m (~${(currentFac.distanceMeters / 70).coerceAtLeast(1)} min)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = TealPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons based on User Role
                    when (userRole) {
                        UserRole.CITIZEN -> {
                            // Citizen View: Direct Google Maps Navigation (as seen in Google Maps) + Report Issue
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onNavigate(currentFac) },
                                    modifier = Modifier
                                        .weight(1.3f)
                                        .testTag("map_btn_google_maps_navigate"),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                                ) {
                                    Icon(
                                        Icons.Default.Navigation,
                                        contentDescription = "Navigate to Facility (Google Maps)",
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Navigate (Google Maps)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }

                                if (onReportIssue != null) {
                                    OutlinedButton(
                                        onClick = { onReportIssue(currentFac) },
                                        modifier = Modifier
                                            .weight(0.7f)
                                            .testTag("map_btn_report"),
                                        shape = RoundedCornerShape(10.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Default.ReportProblem, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Report", fontSize = 11.sp, color = MaterialTheme.colorScheme.error, maxLines = 1)
                                    }
                                }
                            }
                        }
                        UserRole.STAFF -> {
                            // Staff View: Navigate + Start Duty
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { onNavigate(currentFac) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("map_btn_google_maps_navigate"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, TealPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                                ) {
                                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp), tint = TealPrimary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Navigate (Maps)", fontSize = 11.sp, color = TealPrimary, fontWeight = FontWeight.Bold, maxLines = 1)
                                }

                                if (onAcceptDuty != null) {
                                    Button(
                                        onClick = { onAcceptDuty(currentFac) },
                                        modifier = Modifier
                                            .weight(1.1f)
                                            .testTag("map_btn_start_duty"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CleanGreen)
                                    ) {
                                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Start Duty", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                    }
                                }
                            }
                        }
                        UserRole.SUPERVISOR, UserRole.ADMIN -> {
                            // Supervisor / Admin View: Assign Staff + Unified Navigation
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = {
                                            isRouteDrawnInApp = true
                                            webViewRef?.evaluateJavascript(
                                                "if (window.drawWalkingRoute) { window.drawWalkingRoute(${currentFac.latitude}, ${currentFac.longitude}, '${currentFac.id}'); }",
                                                null
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = if (isRouteDrawnInApp) CleanGreen else TealPrimary)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.DirectionsWalk,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isRouteDrawnInApp) "Route Active" else "In-App Route",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }

                                    if (onAssignDuty != null) {
                                        Button(
                                            onClick = { onAssignDuty(currentFac) },
                                            modifier = Modifier.weight(1.1f).testTag("map_btn_assign_duty"),
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00685F))
                                        ) {
                                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(15.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Assign Staff", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedButton(
                                    onClick = { onNavigate(currentFac) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("map_btn_google_maps_navigate"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, TealPrimary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TealPrimary)
                                ) {
                                    Icon(
                                        Icons.Default.Navigation,
                                        contentDescription = "Navigate to Facility (Google Maps)",
                                        modifier = Modifier.size(16.dp),
                                        tint = TealPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Navigate to Facility (Google Maps)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TealPrimary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
