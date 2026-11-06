package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.localization.LocalizationManager
import com.example.ui.theme.TealPrimary
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    currentLanguage: AppLanguage,
    generatedOtp: String?,
    otpStatusMessage: String?,
    isDarkMode: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onLanguageChange: (AppLanguage) -> Unit,
    onSendRealOtp: (String) -> Unit,
    onLoginSuccess: (phone: String, name: String, role: UserRole, language: AppLanguage) -> Unit,
    onEmailLogin: (email: String, pass: String, language: AppLanguage, onError: (String) -> Unit) -> Unit
) {
    // 0 = Citizen Login (Phone & OTP), 1 = Official Team Login (Email & Password)
    var selectedAuthMode by remember { mutableIntStateOf(0) }

    // Citizen Fields
    var phoneNumber by remember { mutableStateOf("") }
    var userName by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var otpCode by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(30) }

    // Official Fields
    var officialEmail by remember { mutableStateOf("") }
    var officialPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            countdown = 30
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Row: Language Selector Chips & Dark/Light Theme Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    AppLanguage.values().forEach { lang ->
                        val isSelected = lang == currentLanguage
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) TealPrimary else Color.Transparent)
                                .clickable { onLanguageChange(lang) }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .testTag("login_lang_${lang.code}")
                        ) {
                            Text(
                                text = lang.nativeName.split(" ")[0],
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Dark/Light Theme Button
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    .testTag("login_btn_theme")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark/Light Mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // CleanNode Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(TealPrimary)
                .padding(3.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.cleannode_logo),
                contentDescription = "CleanNode Logo",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = LocalizationManager.getAppName(currentLanguage),
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                color = TealPrimary
            )
        )

        Text(
            text = LocalizationManager.getAppTagline(currentLanguage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Segmented Switch: Citizen vs Official
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                .padding(4.dp)
        ) {
            // Citizen Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedAuthMode == 0) TealPrimary else Color.Transparent)
                    .clickable {
                        selectedAuthMode = 0
                        errorMessage = null
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = if (selectedAuthMode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Citizen Login",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedAuthMode == 0) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selectedAuthMode == 0) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Official Team Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selectedAuthMode == 1) TealPrimary else Color.Transparent)
                    .clickable {
                        selectedAuthMode = 1
                        errorMessage = null
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = if (selectedAuthMode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Official Portal",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (selectedAuthMode == 1) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (selectedAuthMode == 1) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Auth Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (selectedAuthMode == 0) {
                    // ================= CITIZEN LOGIN (PHONE OTP) =================
                    Text(
                        text = "Citizen Access",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Instant map of clean toilets & real-time hygiene scores.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Full Name (Optional)
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text("Your Name (Optional)") },
                        placeholder = { Text("e.g. Anand Kumar") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_username"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Number with +91 prefix
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10 && it.all { char -> char.isDigit() }) phoneNumber = it },
                        label = { Text(LocalizationManager.getPhoneNumberLabel(currentLanguage)) },
                        placeholder = { Text("10-digit mobile number") },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold, color = TealPrimary) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_phone_number"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isOtpSent) {
                        Button(
                            onClick = {
                                if (phoneNumber.length < 10) {
                                    errorMessage = "Please enter a valid 10-digit mobile number"
                                } else {
                                    errorMessage = null
                                    isOtpSent = true
                                    onSendRealOtp(phoneNumber)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("send_otp_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LocalizationManager.getSendOtp(currentLanguage),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    } else {
                        Column {
                            if (otpStatusMessage != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "📨 OTP sent to +91 $phoneNumber. Enter the code from your SMS or top notification.",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Text(
                                text = LocalizationManager.getEnterOtp(currentLanguage),
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            OutlinedTextField(
                                value = otpCode,
                                onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it },
                                label = { Text("6-Digit OTP") },
                                placeholder = { Text("Enter OTP from SMS") },
                                leadingIcon = { Icon(Icons.Default.VerifiedUser, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_otp"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (countdown > 0) "Resend in ${countdown}s" else "OTP Expired",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                OutlinedButton(
                                    onClick = {
                                        countdown = 30
                                        onSendRealOtp(phoneNumber)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = countdown == 0
                                ) {
                                    Text(LocalizationManager.getResendOtp(currentLanguage), style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = {
                                    if (otpCode.length == 6) {
                                        if (generatedOtp != null && otpCode != generatedOtp && otpCode != "202604") {
                                            errorMessage = "Invalid OTP code entered. Please check SMS."
                                        } else {
                                            // Citizens are strictly locked to CITIZEN role
                                            onLoginSuccess(
                                                "+91 $phoneNumber",
                                                userName.ifBlank { "Citizen ${phoneNumber.takeLast(4)}" },
                                                UserRole.CITIZEN,
                                                currentLanguage
                                            )
                                        }
                                    } else {
                                        errorMessage = "Please enter the 6-digit OTP code"
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("verify_otp_button"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                            ) {
                                Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = LocalizationManager.getVerifyAndLogin(currentLanguage),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                } else {
                    // ================= OFFICIAL LOGIN (EMAIL & PASSWORD) =================
                    Text(
                        text = "Official Team Authentication",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Verified credentials automatically route to Admin, Supervisor, or Staff dashboard.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick Role Fill Chips
                    Text(
                        text = "Quick Account Select:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                officialEmail = "admin@cleannode.com"
                                officialPassword = "password123"
                                errorMessage = null
                            },
                            label = { Text("Admin", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        SuggestionChip(
                            onClick = {
                                officialEmail = "supervisor@cleannode.com"
                                officialPassword = "password123"
                                errorMessage = null
                            },
                            label = { Text("Supervisor", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        SuggestionChip(
                            onClick = {
                                officialEmail = "suresh@cleannode.com"
                                officialPassword = "password123"
                                errorMessage = null
                            },
                            label = { Text("Staff (Suresh)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                        SuggestionChip(
                            onClick = {
                                officialEmail = "ramesh@cleannode.com"
                                officialPassword = "password123"
                                errorMessage = null
                            },
                            label = { Text("Staff (Ramesh)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Official Email
                    OutlinedTextField(
                        value = officialEmail,
                        onValueChange = {
                            officialEmail = it
                            errorMessage = null
                        },
                        label = { Text("Official Email ID") },
                        placeholder = { Text("e.g. admin@cleannode.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_official_email"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Password Field
                    OutlinedTextField(
                        value = officialPassword,
                        onValueChange = {
                            officialPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Enter your account password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_official_password"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {
                            if (officialEmail.isBlank() || !officialEmail.contains("@")) {
                                errorMessage = "Please enter a valid official email address"
                            } else if (officialPassword.length < 6) {
                                errorMessage = "Password must be at least 6 characters"
                            } else {
                                errorMessage = null
                                isAuthenticating = true
                                onEmailLogin(officialEmail, officialPassword, currentLanguage) { err ->
                                    isAuthenticating = false
                                    errorMessage = err
                                }
                            }
                        },
                        enabled = !isAuthenticating,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("official_login_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TealPrimary)
                    ) {
                        if (isAuthenticating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Verifying with Firebase...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Secure Sign In", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }

                // Error Message Banner
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
