package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiSanitationService
import com.example.data.local.CleanNodeDatabase
import com.example.data.model.AppLanguage
import com.example.data.model.CitizenReportEntity
import com.example.data.model.CleaningDutyEntity
import com.example.data.model.EscalationEntity
import com.example.data.model.FacilityEntity
import com.example.data.model.ReportCategory
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.repository.CleanNodeRepository
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

data class ChatMessage(
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class CleanNodeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = CleanNodeDatabase.getDatabase(application, viewModelScope)
    private val repository = CleanNodeRepository(db.cleanNodeDao())

    val activeUser: StateFlow<UserEntity?> = repository.activeUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val facilities: StateFlow<List<FacilityEntity>> = repository.smartRankedFacilities
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duties: StateFlow<List<CleaningDutyEntity>> = repository.allDuties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val escalations: StateFlow<List<EscalationEntity>> = repository.allEscalations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<CitizenReportEntity>> = repository.allReports
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts = com.example.data.repository.CleanNodeOperationsProvider.alerts
    val liveCubicles = com.example.data.repository.CleanNodeOperationsProvider.liveCubicles
    val predictiveMaintenanceList = com.example.data.repository.CleanNodeOperationsProvider.predictiveMaintenanceList
    val anomaliesList = com.example.data.repository.CleanNodeOperationsProvider.anomaliesList
    val fieldStaffList = com.example.data.repository.CleanNodeOperationsProvider.fieldStaffList
    val maintenanceHistory = com.example.data.repository.CleanNodeOperationsProvider.maintenanceHistory

    fun handleAlert(alertId: String) {
        com.example.data.repository.CleanNodeOperationsProvider.handleAlert(alertId)
    }

    fun manualAssignDuty(
        facilityId: String,
        facilityName: String,
        cubicle: String,
        staffName: String,
        staffPhone: String,
        priority: String,
        remarks: String
    ) {
        viewModelScope.launch {
            repository.manualAssignDuty(
                getApplication(),
                facilityId,
                facilityName,
                cubicle,
                staffName,
                staffPhone,
                priority,
                remarks
            )
        }
    }

    fun reassignDuty(dutyId: String, newStaffName: String, newStaffPhone: String) {
        viewModelScope.launch {
            repository.reassignDuty(getApplication(), dutyId, newStaffName, newStaffPhone)
        }
    }

    // Persistent SharedPreferences for language and user preferences
    private val prefs = application.getSharedPreferences("cleannode_prefs", android.content.Context.MODE_PRIVATE)

    // Language state initialized from persistent storage
    private val _currentLanguage = MutableStateFlow(
        try {
            val saved = prefs.getString("preferred_language", null)
            if (saved != null) AppLanguage.valueOf(saved) else AppLanguage.ENGLISH
        } catch (_: Exception) {
            AppLanguage.ENGLISH
        }
    )
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    // Dark/Light Theme state
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Selected facility for detail modal/sheet
    private val _selectedFacility = MutableStateFlow<FacilityEntity?>(null)
    val selectedFacility: StateFlow<FacilityEntity?> = _selectedFacility.asStateFlow()

    // Real OTP code generated dynamically
    private val _generatedOtpCode = MutableStateFlow<String?>(null)
    val generatedOtpCode: StateFlow<String?> = _generatedOtpCode.asStateFlow()

    private val _otpStatusMessage = MutableStateFlow<String?>(null)
    val otpStatusMessage: StateFlow<String?> = _otpStatusMessage.asStateFlow()

    // AI Chat history
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "ai",
                text = "Welcome to CleanNode AI! Ask me about nearby clean public restrooms, real-time sensor parameters, or sanitation standards in Kannada, Hindi, or English."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // AI diagnostic dialog content
    private val _aiDiagnosticResult = MutableStateFlow<String?>(null)
    val aiDiagnosticResult: StateFlow<String?> = _aiDiagnosticResult.asStateFlow()

    init {
        // Initialize Notification Channels
        NotificationHelper.createChannels(application)

        // Observe language preference from logged in user if not explicitly overridden
        viewModelScope.launch {
            activeUser.collect { user ->
                val saved = prefs.getString("preferred_language", null)
                if (saved != null) {
                    try {
                        _currentLanguage.value = AppLanguage.valueOf(saved)
                    } catch (_: Exception) {}
                } else if (user != null) {
                    _currentLanguage.value = user.preferredLanguage
                }
            }
        }

        // Automated Background Task Dispatch Loop (SRS v2.0 Specification FR15.1 - FR15.6)
        // Continuously monitors facilities, assigns duty automatically when Danger Zone occurs,
        // and checks 10-minute acceptance windows.
        viewModelScope.launch {
            while (true) {
                try {
                    repository.checkAndAutoDispatchDangerZoneDuties(getApplication())
                } catch (_: Exception) {}
                delay(12000L) // Scan every 12 seconds
            }
        }
    }

    fun selectFacility(facility: FacilityEntity?) {
        _selectedFacility.value = facility
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        try {
            prefs.edit().putString("preferred_language", language.name).apply()
        } catch (_: Exception) {}
        viewModelScope.launch {
            repository.updateLanguage(language, activeUser.value)
        }
    }

    fun switchRole(newRole: UserRole) {
        viewModelScope.launch {
            repository.switchRole(newRole, activeUser.value)
        }
    }

    /**
     * Real OTP Generation & System Notification / SMS delivery
     */
    fun sendRealOtp(phoneNumber: String) {
        val code = (100000..999999).random().toString()
        _generatedOtpCode.value = code
        _otpStatusMessage.value = "OTP sent to +91 $phoneNumber via SMS. Please enter the code."

        // Send real Android heads-up notification and real SMS
        NotificationHelper.sendOtpNotification(getApplication(), phoneNumber, code)
    }

    fun loginWithOtp(phone: String, name: String, role: UserRole, language: AppLanguage) {
        viewModelScope.launch {
            repository.loginUser(phone, name, role, language)
            _currentLanguage.value = language
        }
    }

    fun loginWithEmail(
        email: String,
        pass: String,
        language: AppLanguage,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = repository.loginWithOfficialEmail(email, pass, language)
            result.onSuccess {
                _currentLanguage.value = language
            }.onFailure { err ->
                val errorMsg = when {
                    err.message?.contains("password", ignoreCase = true) == true -> "Incorrect password entered. Please try again."
                    err.message?.contains("no user", ignoreCase = true) == true || err.message?.contains("user-not-found", ignoreCase = true) == true -> "Official account not found for this email address."
                    err.message?.contains("network", ignoreCase = true) == true -> "Network connection error. Check your internet."
                    else -> err.message ?: "Authentication failed. Please verify credentials."
                }
                onError(errorMsg)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    // --- Duty / Escalation Actions ---

    fun acceptDuty(dutyId: String) {
        viewModelScope.launch {
            repository.acceptDuty(dutyId)
        }
    }

    fun startDutyForFacility(facility: FacilityEntity, onStarted: (CleaningDutyEntity) -> Unit) {
        viewModelScope.launch {
            val user = activeUser.value
            val staffName = user?.name ?: "Staff (Suresh)"
            val staffPhone = user?.phoneNumber ?: "+91 98765 43210"
            val duty = repository.startDutyForFacility(
                context = getApplication(),
                facilityId = facility.id,
                facilityName = facility.name,
                staffName = staffName,
                staffPhone = staffPhone
            )
            onStarted(duty)
        }
    }

    fun declineDuty(dutyId: String) {
        viewModelScope.launch {
            repository.declineDuty(getApplication(), dutyId)
        }
    }

    fun simulateTimeoutAndEscalate(dutyId: String) {
        viewModelScope.launch {
            repository.escalateDutyToSupervisor(getApplication(), dutyId, "10-minute acceptance window expired")
        }
    }

    fun escalateToAdmin(escId: String) {
        viewModelScope.launch {
            repository.escalateToAdmin(getApplication(), escId)
        }
    }

    fun submitProofOfCleaning(
        dutyId: String,
        photoUri: String,
        notes: String,
        floorMopped: Boolean,
        bowlDisinfected: Boolean,
        waterChecked: Boolean,
        soapRefilled: Boolean,
        trashEmptied: Boolean
    ) {
        viewModelScope.launch {
            val eval = GeminiSanitationService.verifyCleaningProof(
                notes, floorMopped, bowlDisinfected, waterChecked, soapRefilled, trashEmptied
            )
            repository.submitProofOfCleaning(
                dutyId = dutyId,
                photoUri = photoUri,
                notes = notes,
                floorMopped = floorMopped,
                bowlDisinfected = bowlDisinfected,
                waterSupplyChecked = waterChecked,
                soapRefilled = soapRefilled,
                trashEmptied = trashEmptied,
                aiScore = eval.pciScore
            )
        }
    }

    fun submitCitizenReport(
        facilityId: String,
        facilityName: String,
        rating: Int,
        category: ReportCategory,
        comment: String,
        photoUri: String? = null
    ) {
        val phone = activeUser.value?.phoneNumber ?: "+91 99000 00000"
        viewModelScope.launch {
            repository.submitCitizenReport(
                facilityId = facilityId,
                facilityName = facilityName,
                phone = phone,
                rating = rating,
                category = category,
                comment = comment,
                photoUri = photoUri
            )
        }
    }

    fun updateReportStatus(reportId: String, status: String) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status)
        }
    }

    fun simulateSensorSpike(facilityId: String) {
        viewModelScope.launch {
            repository.simulateSensorAnomaly(getApplication(), facilityId)
        }
    }

    fun requestAiDiagnostics(facility: FacilityEntity) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val analysis = GeminiSanitationService.explainPciScore(
                facilityName = facility.name,
                pci = facility.pciScore,
                ammonia = facility.ammoniaPpm,
                humidity = facility.humidityPct,
                occupancy = facility.occupancyCount,
                flowRate = facility.flushFlowRate,
                language = _currentLanguage.value
            )
            _aiDiagnosticResult.value = analysis
            _isAiThinking.value = false
        }
    }

    fun clearAiDiagnostics() {
        _aiDiagnosticResult.value = null
    }

    fun sendAiChatMessage(userText: String) {
        if (userText.isBlank()) return
        val currentList = _chatMessages.value.toMutableList()
        currentList.add(ChatMessage(sender = "user", text = userText))
        _chatMessages.value = currentList
        _isAiThinking.value = true

        viewModelScope.launch {
            val contextInfo = "CleanNode Bengaluru: ${facilities.value.size} facilities monitored. " +
                    "Jayanagar PCI: 92 (Clean), Indiranagar PCI: 78 (Moderate), Majestic PCI: 24 (Danger Zone)."
            val reply = GeminiSanitationService.askSanitationAssistant(
                query = userText,
                language = _currentLanguage.value,
                contextInfo = contextInfo
            )
            val updated = _chatMessages.value.toMutableList()
            updated.add(ChatMessage(sender = "ai", text = reply))
            _chatMessages.value = updated
            _isAiThinking.value = false
        }
    }
}
