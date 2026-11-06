package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.FacilityEntity
import com.example.data.model.UserRole
import com.example.localization.LocalizationManager
import com.example.ui.components.AiDiagnosticDialog
import com.example.ui.components.FacilityDetailSheet
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CitizenDashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.StaffDashboardScreen
import com.example.ui.screens.SupervisorDashboardScreen
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.TealPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanNodeApp(viewModel: CleanNodeViewModel = viewModel()) {
    val activeUser by viewModel.activeUser.collectAsStateWithLifecycle()
    val facilities by viewModel.facilities.collectAsStateWithLifecycle()
    val duties by viewModel.duties.collectAsStateWithLifecycle()
    val escalations by viewModel.escalations.collectAsStateWithLifecycle()
    val reports by viewModel.reports.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val selectedFacility by viewModel.selectedFacility.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val aiDiagnosticResult by viewModel.aiDiagnosticResult.collectAsStateWithLifecycle()
    val generatedOtp by viewModel.generatedOtpCode.collectAsStateWithLifecycle()
    val otpStatusMessage by viewModel.otpStatusMessage.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()

    val context = LocalContext.current
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

    if (activeUser == null || !activeUser!!.isLoggedIn) {
        LoginScreen(
            currentLanguage = currentLanguage,
            generatedOtp = generatedOtp,
            otpStatusMessage = otpStatusMessage,
            isDarkMode = isDarkMode,
            onToggleTheme = { viewModel.toggleDarkMode() },
            onLanguageChange = { viewModel.setLanguage(it) },
            onSendRealOtp = { viewModel.sendRealOtp(it) },
            onLoginSuccess = { phone, name, role, lang ->
                viewModel.loginWithOtp(phone, name, role, lang)
            },
            onEmailLogin = { email, pass, lang, onError ->
                viewModel.loginWithEmail(email, pass, lang, onError)
            }
        )
    } else {
        val user = activeUser!!

        LaunchedEffect(user.role) {
            selectedTab = 0
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 2.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cleannode_logo),
                                contentDescription = "CleanNode",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CleanNode",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = TealPrimary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    ),
                    actions = {
                        // Verified Role Badge (read-only, authenticated)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.22f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("topbar_role_pill")
                        ) {
                            Text(
                                text = when (user.role) {
                                    UserRole.CITIZEN -> "✓ Citizen"
                                    UserRole.STAFF -> "✓ Staff"
                                    UserRole.SUPERVISOR -> "✓ Supervisor"
                                    UserRole.ADMIN -> "✓ Admin"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        // Dark/Light Theme Toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.size(32.dp).testTag("btn_topbar_theme")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle Dark/Light Mode",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Language Switch Button
                        IconButton(
                            onClick = { showLanguageDialog = true },
                            modifier = Modifier.size(32.dp).testTag("btn_topbar_lang")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = "Language",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Logout
                        IconButton(
                            onClick = { viewModel.logout() },
                            modifier = Modifier.size(32.dp).testTag("btn_topbar_logout")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Logout",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                )
            },
            bottomBar = {
                val roleTabs = when (user.role) {
                    UserRole.CITIZEN -> listOf(
                        CleanNodeTab("Map", Icons.Default.Map),
                        CleanNodeTab("List", Icons.Default.List)
                    )
                    UserRole.STAFF -> listOf(
                        CleanNodeTab("Duties", Icons.Default.CleaningServices),
                        CleanNodeTab("Map", Icons.Default.Map)
                    )
                    UserRole.SUPERVISOR -> listOf(
                        CleanNodeTab("Toilets", Icons.Default.SupervisorAccount),
                        CleanNodeTab("Alerts", Icons.Default.Warning),
                        CleanNodeTab("Feedback", Icons.Default.Feedback),
                        CleanNodeTab("Map", Icons.Default.Map)
                    )
                    UserRole.ADMIN -> listOf(
                        CleanNodeTab("Overview", Icons.Default.LocationCity),
                        CleanNodeTab("Alerts", Icons.Default.Warning),
                        CleanNodeTab("Feedback", Icons.Default.Feedback),
                        CleanNodeTab("Map", Icons.Default.Map)
                    )
                }

                val safeSelectedTab = selectedTab.coerceIn(0, (roleTabs.size - 1).coerceAtLeast(0))

                NavigationBar(
                    modifier = Modifier.navigationBarsPadding(),
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    roleTabs.forEachIndexed { index, tab ->
                        val translatedTitle = LocalizationManager.getTabTitle(tab.title, currentLanguage)
                        NavigationBarItem(
                            selected = safeSelectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tab.icon, contentDescription = translatedTitle) },
                            label = { Text(translatedTitle, maxLines = 1, softWrap = false, fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_tab_${tab.title.replace(" ", "_").lowercase()}")
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val safeSelectedTab = selectedTab.coerceIn(0, 3)
                Crossfade(targetState = user.role, label = "screenTransition") { role ->
                    when (role) {
                        UserRole.CITIZEN -> {
                            CitizenDashboardScreen(
                                facilities = facilities,
                                selectedFacility = selectedFacility,
                                lang = currentLanguage,
                                selectedTab = safeSelectedTab,
                                onSelectFacility = { viewModel.selectFacility(it) },
                                onNavigateToMap = { fac ->
                                    launchGoogleMaps(fac)
                                },
                                onSubmitReport = { id, name, rating, cat, comment, photoUri ->
                                    viewModel.submitCitizenReport(id, name, rating, cat, comment, photoUri)
                                },
                                onRequestAiDiagnostics = { viewModel.requestAiDiagnostics(it) }
                            )
                        }
                        UserRole.STAFF -> {
                            StaffDashboardScreen(
                                duties = duties,
                                facilities = facilities,
                                lang = currentLanguage,
                                selectedTab = safeSelectedTab,
                                onAcceptDuty = { viewModel.acceptDuty(it) },
                                onDeclineDuty = { viewModel.declineDuty(it) },
                                onSimulateTimeout = { viewModel.simulateTimeoutAndEscalate(it) },
                                onSubmitProof = { id, uri, notes, m, d, w, s, t ->
                                    viewModel.submitProofOfCleaning(id, uri, notes, m, d, w, s, t)
                                },
                                onStartDutyForFacility = { fac, onStarted ->
                                    viewModel.startDutyForFacility(fac, onStarted)
                                }
                            )
                        }
                        UserRole.SUPERVISOR -> {
                            SupervisorDashboardScreen(
                                facilities = facilities,
                                escalations = escalations,
                                duties = duties,
                                reports = reports,
                                lang = currentLanguage,
                                selectedTab = safeSelectedTab,
                                onSelectTab = { selectedTab = it },
                                onEscalateToAdmin = { viewModel.escalateToAdmin(it) },
                                onResolveReport = { viewModel.updateReportStatus(it, "RESOLVED") },
                                onDispatchDuty = { fId, fName, cubicle, sName, sPhone, prio, remarks ->
                                    viewModel.manualAssignDuty(fId, fName, cubicle, sName, sPhone, prio, remarks)
                                }
                            )
                        }
                        UserRole.ADMIN -> {
                            AdminDashboardScreen(
                                facilities = facilities,
                                escalations = escalations,
                                reports = reports,
                                duties = duties,
                                lang = currentLanguage,
                                selectedTab = safeSelectedTab,
                                onSelectTab = { selectedTab = it },
                                onSimulateAnomaly = { viewModel.simulateSensorSpike(it) },
                                onRequestAiDiagnostics = { viewModel.requestAiDiagnostics(it) },
                                onResolveReport = { viewModel.updateReportStatus(it, "RESOLVED") },
                                onDispatchDuty = { fId, fName, cubicle, sName, sPhone, prio, remarks ->
                                    viewModel.manualAssignDuty(fId, fName, cubicle, sName, sPhone, prio, remarks)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Language Selector Dialog
    if (showLanguageDialog) {
        LanguageSelectorDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { viewModel.setLanguage(it) },
            onDismiss = { showLanguageDialog = false }
        )
    }


    // Facility Detail Sheet
    if (selectedFacility != null) {
        FacilityDetailSheet(
            facility = selectedFacility!!,
            lang = currentLanguage,
            userRole = activeUser?.role ?: UserRole.CITIZEN,
            onDismiss = { viewModel.selectFacility(null) },
            onNavigate = { fac ->
                viewModel.selectFacility(null)
                launchGoogleMaps(fac)
            }
        )
    }

    // AI Diagnostics Dialog
    if (aiDiagnosticResult != null) {
        AiDiagnosticDialog(
            diagnosticText = aiDiagnosticResult!!,
            onDismiss = { viewModel.clearAiDiagnostics() }
        )
    }
}

private data class NavigationTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun RoleSwitchDialog(
    currentRole: UserRole,
    lang: AppLanguage,
    onSelectRole: (UserRole) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = LocalizationManager.getSelectRole(lang), fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Switch active demo profile to test role-specific workflows (Staff dispatch, Supervisor escalation ladder, or BBMP Admin control):",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                UserRole.values().forEach { role ->
                    val isSelected = role == currentRole
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onSelectRole(role) }
                            .testTag("switch_role_${role.name}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) TealPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (role) {
                                    UserRole.CITIZEN -> Icons.Default.Person
                                    UserRole.STAFF -> Icons.Default.CleaningServices
                                    UserRole.SUPERVISOR -> Icons.Default.SupervisorAccount
                                    UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                },
                                contentDescription = null,
                                tint = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = LocalizationManager.getRoleName(role, lang),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) TealPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

private data class CleanNodeTab(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
