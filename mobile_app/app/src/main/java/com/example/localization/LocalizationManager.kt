package com.example.localization

import com.example.data.model.AppLanguage
import com.example.data.model.CleanlinessBand
import com.example.data.model.UserRole

object LocalizationManager {

    fun getAppName(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಕ್ಲೀನ್‌ನೋಡ್ (CleanNode)"
        AppLanguage.HINDI -> "क्लीननोड (CleanNode)"
        AppLanguage.ENGLISH -> "CleanNode"
    }

    fun getAppTagline(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಐಒಟಿ ಆಧಾರಿತ ಸಾರ್ವಜನಿಕ ನೈರ್ಮಲ್ಯ ಮತ್ತು ಸ್ವಚ್ಛತಾ ಮೇಲ್ವಿಚಾರಣೆ"
        AppLanguage.HINDI -> "आईओटी आधारित सार्वजनिक स्वच्छता निगरानी प्रणाली"
        AppLanguage.ENGLISH -> "IoT-based Public Sanitation & Hygiene System"
    }

    // Auth / Login
    fun getLoginTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಕ್ಲೀನ್‌ನೋಡ್‌ಗೆ ಸುಸ್ವಾಗತ"
        AppLanguage.HINDI -> "क्लीननोड में आपका स्वागत है"
        AppLanguage.ENGLISH -> "Welcome to CleanNode"
    }

    fun getLoginSubtitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮುಂದುವರಿಯಲು ಮೊಬೈಲ್ ಸಂಖ್ಯೆ ನಮೂದಿಸಿ"
        AppLanguage.HINDI -> "जारी रखने के लिए मोबाइल नंबर दर्ज करें"
        AppLanguage.ENGLISH -> "Enter your mobile number to continue"
    }

    fun getPhoneNumberLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮೊಬೈಲ್ ಸಂಖ್ಯೆ (+91)"
        AppLanguage.HINDI -> "मोबाइल नंबर (+91)"
        AppLanguage.ENGLISH -> "Mobile Number (+91)"
    }

    fun getSendOtp(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಒಟಿಪಿ ಕಳುಹಿಸಿ"
        AppLanguage.HINDI -> "ओटीपी भेजें"
        AppLanguage.ENGLISH -> "Send OTP"
    }

    fun getEnterOtp(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "6 ಅಂಕಿಯ ಒಟಿಪಿ ನಮೂದಿಸಿ"
        AppLanguage.HINDI -> "6 अंकों का ओटीपी दर्ज करें"
        AppLanguage.ENGLISH -> "Enter 6-digit OTP"
    }

    fun getVerifyAndLogin(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಪರಿಶೀಲಿಸಿ ಮತ್ತು ಲಾಗಿನ್ ಆಗಿ"
        AppLanguage.HINDI -> "सत्यापित करें और लॉगिन करें"
        AppLanguage.ENGLISH -> "Verify & Login"
    }

    fun getResendOtp(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮತ್ತೆ ಒಟಿಪಿ ಕಳುಹಿಸಿ"
        AppLanguage.HINDI -> "ओटीपी पुनः भेजें"
        AppLanguage.ENGLISH -> "Resend OTP"
    }

    fun getSelectRole(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ನಿಮ್ಮ ಪಾತ್ರವನ್ನು ಆಯ್ಕೆಮಾಡಿ"
        AppLanguage.HINDI -> "अपनी भूमिका का चयन करें"
        AppLanguage.ENGLISH -> "Select Your Role"
    }

    fun getRoleName(role: UserRole, lang: AppLanguage): String = when (role) {
        UserRole.CITIZEN -> when (lang) {
            AppLanguage.KANNADA -> "ಸಾರ್ವಜನಿಕ ನಾಗರಿಕ (Citizen)"
            AppLanguage.HINDI -> "नागरिक (Citizen)"
            AppLanguage.ENGLISH -> "Citizen / Public"
        }
        UserRole.STAFF -> when (lang) {
            AppLanguage.KANNADA -> "ಸ್ವಚ್ಛತಾ ಸಿಬ್ಬಂದಿ (Cleaning Staff)"
            AppLanguage.HINDI -> "सफाई कर्मचारी (Staff)"
            AppLanguage.ENGLISH -> "Maintenance Staff"
        }
        UserRole.SUPERVISOR -> when (lang) {
            AppLanguage.KANNADA -> "ಶೌಚಾಲಯ ಮೇಲ್ವಿಚಾರಕ (Supervisor)"
            AppLanguage.HINDI -> "शौचालय पर्यवेक्षक (Supervisor)"
            AppLanguage.ENGLISH -> "Toilet Supervisor"
        }
        UserRole.ADMIN -> when (lang) {
            AppLanguage.KANNADA -> "ಬಿಬಿಎಂಪಿ ಪಾಲಿಕೆ ಆಡಳಿತ (BBMP Admin)"
            AppLanguage.HINDI -> "नगर निगम प्राधिकरण (BBMP Admin)"
            AppLanguage.ENGLISH -> "Municipal Authority (BBMP)"
        }
    }

    fun getSelectLanguage(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಭಾಷೆ ಆಯ್ಕೆ"
        AppLanguage.HINDI -> "भाषा का चयन"
        AppLanguage.ENGLISH -> "Choose Language"
    }

    // Cleanliness Bands & PCI
    fun getPciTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮುನ್ಸೂಚಕ ಸ್ವಚ್ಛತಾ ಸೂಚ್ಯಂಕ (PCI)"
        AppLanguage.HINDI -> "पूर्वानुमानित स्वच्छता सूचकांक (PCI)"
        AppLanguage.ENGLISH -> "Predictive Cleanliness Index (PCI)"
    }

    fun getBandLabel(band: CleanlinessBand, lang: AppLanguage): String = when (band) {
        CleanlinessBand.CLEAN -> when (lang) {
            AppLanguage.KANNADA -> "ಅತ್ಯುತ್ತಮ / ಸ್ವಚ್ಛ"
            AppLanguage.HINDI -> "उत्कृष्ट / स्वच्छ"
            AppLanguage.ENGLISH -> "Clean / Excellent"
        }
        CleanlinessBand.MODERATE -> when (lang) {
            AppLanguage.KANNADA -> "ಮಧ್ಯಮ ಸ್ಥಿತಿ"
            AppLanguage.HINDI -> "मध्यम स्थिति"
            AppLanguage.ENGLISH -> "Moderate"
        }
        CleanlinessBand.NEEDS_ATTENTION -> when (lang) {
            AppLanguage.KANNADA -> "ಗಮನ ಅಗತ್ಯವಿದೆ"
            AppLanguage.HINDI -> "ध्यान देने योग्य"
            AppLanguage.ENGLISH -> "Needs Attention"
        }
        CleanlinessBand.DANGER_ZONE -> when (lang) {
            AppLanguage.KANNADA -> "ಅಪಾಯ ವಲಯ (ತುರ್ತು ಸ್ವಚ್ಛತೆ!)"
            AppLanguage.HINDI -> "खतरा क्षेत्र (आपातकालीन सफाई!)"
            AppLanguage.ENGLISH -> "Danger Zone (Critical)"
        }
    }

    // Dashboard & Navigation
    fun getDashboard(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಡ್ಯಾಶ್‌ಬೋರ್ಡ್"
        AppLanguage.HINDI -> "डैशबोर्ड"
        AppLanguage.ENGLISH -> "Dashboard"
    }

    fun getNearbyRestrooms(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಹತ್ತಿರದ ಶೌಚಾಲಯಗಳು"
        AppLanguage.HINDI -> "निकटतम शौचालय"
        AppLanguage.ENGLISH -> "Nearby Restrooms"
    }

    fun getSmartRankingNotice(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸ್ಮಾರ್ಟ್ ಶ್ರೇಯಾಂಕ: 50 ಮೀಟರ್ ವ್ಯಾಪ್ತಿಯಲ್ಲಿ ಸ್ವಚ್ಛ ಶೌಚಾಲಯಕ್ಕೆ ಮೊದಲ ಆದ್ಯತೆ"
        AppLanguage.HINDI -> "स्मार्ट रैंकिंग: 50 मीटर के दायरे में स्वच्छ शौचालय को पहली प्राथमिकता"
        AppLanguage.ENGLISH -> "Smart Ranking: Recommends cleaner option within 50m tolerance band"
    }

    fun getMonitoredPublicBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಪರಿಶೀಲಿತ ಸಾರ್ವಜನಿಕ (ಲೈವ್ ಐಒಟಿ)"
        AppLanguage.HINDI -> "निगरानीकृत सार्वजनिक (लाइव आईओटी)"
        AppLanguage.ENGLISH -> "Public (Monitored)"
    }

    fun getUnmonitoredPrivateBadge(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಖಾಸಗಿ (ಮಾಲ್/ರೆಸ್ಟೋರೆಂಟ್ - ಡೇಟಾ ಇಲ್ಲ)"
        AppLanguage.HINDI -> "निजी (मॉल/होटल - डेटा उपलब्ध नहीं)"
        AppLanguage.ENGLISH -> "Private (Unmonitored)"
    }

    fun getCleanlinessDataUnavailable(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸ್ವಚ್ಛತಾ ಸೂಚ್ಯಂಕ ಲಭ್ಯವಿಲ್ಲ (ಖಾಸಗಿ ಆವರಣ)"
        AppLanguage.HINDI -> "स्वच्छता डेटा अनुपलब्ध (निजी परिसर)"
        AppLanguage.ENGLISH -> "Cleanliness data unavailable"
    }

    // Sensors
    fun getAmmoniaOdor(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಅಮೋನಿಯಾ ಅನಿಲ (MQ-135)"
        AppLanguage.HINDI -> "अमोनिया गैस (MQ-135)"
        AppLanguage.ENGLISH -> "Ammonia Gas (MQ-135)"
    }

    fun getWaterFlow(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಫ್ಲಶ್ ಹರಿವು (YF-S201)"
        AppLanguage.HINDI -> "फ्लश जल प्रवाह (YF-S201)"
        AppLanguage.ENGLISH -> "Flush Flow (YF-S201)"
    }

    fun getOccupancyLabel(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ದೈನಂದಿನ ಬಳಕೆದಾರರು (PIR)"
        AppLanguage.HINDI -> "दैनिक उपयोगकर्ता (PIR)"
        AppLanguage.ENGLISH -> "Daily Footfall (PIR)"
    }

    fun getTempHumidity(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ತಾಪಮಾನ ಮತ್ತು ತೇವಾಂಶ (DHT22)"
        AppLanguage.HINDI -> "तापमान और आर्द्रता (DHT22)"
        AppLanguage.ENGLISH -> "Temp & Humidity (DHT22)"
    }

    // Actions
    fun getReportIssue(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ದೂರು ನೀಡಿ / ರೇಟಿಂಗ್"
        AppLanguage.HINDI -> "शिकायत दर्ज करें / रेटिंग"
        AppLanguage.ENGLISH -> "Report Issue / Rating"
    }

    fun getProofOfCleaning(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸ್ವಚ್ಛತೆಯ ಫೋಟೋ ಪುರಾವೆ"
        AppLanguage.HINDI -> "सफाई का फोटो प्रमाण"
        AppLanguage.ENGLISH -> "Proof of Cleaning"
    }

    fun getEscalationLadder(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಅಪಾಯ ವಲಯ ಏರಿಕೆ ಹಂತಗಳು"
        AppLanguage.HINDI -> "खतरा क्षेत्र वृद्धि सोपान"
        AppLanguage.ENGLISH -> "Danger Zone Escalation"
    }

    fun getAiSanitationAssistant(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಜೆಮಿನಿ ಎಐ ಸ್ವಚ್ಛತಾ ಮಾರ್ಗದರ್ಶಿ"
        AppLanguage.HINDI -> "जेमिनी एआई स्वच्छता सहायक"
        AppLanguage.ENGLISH -> "Gemini AI Sanitation Guide"
    }

    fun getOfflineBanner(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಆಫ್‌ಲೈನ್ ಮೋಡ್: ಸ್ಥಳೀಯವಾಗಿ ಸಂಗ್ರಹಿಸಲಾಗಿದೆ"
        AppLanguage.HINDI -> "ऑफलाइन मोड: स्थानीय रूप से सहेजा गया डेटा"
        AppLanguage.ENGLISH -> "Offline Mode: Cached locally on device"
    }

    fun getLogout(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಲಾಗ್‌ಔಟ್"
        AppLanguage.HINDI -> "लॉग आउट"
        AppLanguage.ENGLISH -> "Logout"
    }

    // Navigation Tabs
    fun getTabTitle(tabKey: String, lang: AppLanguage): String = when (tabKey) {
        "Overview" -> when (lang) {
            AppLanguage.KANNADA -> "ಅವಲೋಕನ"
            AppLanguage.HINDI -> "अवलोकन"
            AppLanguage.ENGLISH -> "Overview"
        }
        "Toilets" -> when (lang) {
            AppLanguage.KANNADA -> "ಶೌಚಾಲಯಗಳು"
            AppLanguage.HINDI -> "शौचालय"
            AppLanguage.ENGLISH -> "Toilets"
        }
        "Duties" -> when (lang) {
            AppLanguage.KANNADA -> "ಕರ್ತವ್ಯಗಳು"
            AppLanguage.HINDI -> "कर्तव्य"
            AppLanguage.ENGLISH -> "Duties"
        }
        "Alerts" -> when (lang) {
            AppLanguage.KANNADA -> "ಎಚ್ಚರಿಕೆಗಳು"
            AppLanguage.HINDI -> "चेतावनी"
            AppLanguage.ENGLISH -> "Alerts"
        }
        "Feedback" -> when (lang) {
            AppLanguage.KANNADA -> "ಪ್ರತಿಕ್ರಿಯೆ"
            AppLanguage.HINDI -> "प्रतिक्रिया"
            AppLanguage.ENGLISH -> "Feedback"
        }
        "Map" -> when (lang) {
            AppLanguage.KANNADA -> "ನಕ್ಷೆ"
            AppLanguage.HINDI -> "मानचित्र"
            AppLanguage.ENGLISH -> "Map"
        }
        else -> tabKey
    }

    // Admin Dashboard Strings
    fun getAdminHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಬೃಹತ್ ಬೆಂಗಳೂರು ಮಹಾನಗರ ಪಾಲಿಕೆ (ಬಿಬಿಎಂಪಿ)"
        AppLanguage.HINDI -> "बृहत बेंगलुरु महानगर पालिका (बीबीएमपी)"
        AppLanguage.ENGLISH -> "Bruhat Bengaluru Mahanagara Palike (BBMP)"
    }

    fun getAdminSubHeader(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸ್ಮಾರ್ಟ್ ನೈರ್ಮಲ್ಯ ಆಜ್ಞೆ ಮತ್ತು ಮುನ್ಸೂಚಕ ವಿಶ್ಲೇಷಣೆ"
        AppLanguage.HINDI -> "स्मार्ट स्वच्छता कमान एवं पूर्वानुमानात्मक विश्लेषण"
        AppLanguage.ENGLISH -> "Smart Sanitation Command & Predictive Analytics"
    }

    fun getMonitored(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಪರಿಶೀಲಿತ ಘಟಕಗಳು"
        AppLanguage.HINDI -> "सक्रिय आईओटी इकाइयां"
        AppLanguage.ENGLISH -> "Monitored"
    }

    fun getAvgCityPci(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ನಗರ ಸರಾಸರಿ PCI"
        AppLanguage.HINDI -> "नगर औसत PCI"
        AppLanguage.ENGLISH -> "Avg City PCI"
    }

    fun getPristineClean(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಅತ್ಯಂತ ಸ್ವಚ್ಛ"
        AppLanguage.HINDI -> "अत्यंत स्वच्छ"
        AppLanguage.ENGLISH -> "Pristine Clean"
    }

    fun getDangerZoneTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಅಪಾಯ ವಲಯ"
        AppLanguage.HINDI -> "खतरा क्षेत्र"
        AppLanguage.ENGLISH -> "Danger Zone"
    }

    fun getExaminerLiveDemoTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಪರೀಕ್ಷಕರ / ಲೈವ್ ಡೆಮೊ ಟೂಲ್"
        AppLanguage.HINDI -> "परीक्षक / लाइव डेमो टूल"
        AppLanguage.ENGLISH -> "Examiner / Live Demo Tool"
    }

    fun getExaminerLiveDemoDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಜಯನಗರ ಇ-ಟಾಯ್ಲೆಟ್‌ನಲ್ಲಿ ಸಂವೇದಕ ಏರಿಕೆಯನ್ನು ಅನುಕರಿಸಿ, ತಕ್ಷಣದ ಎಸ್‌ಎಲ್‌ಎ ರವಾನೆಯನ್ನು ಪರೀಕ್ಷಿಸಿ"
        AppLanguage.HINDI -> "जयनगर ई-टॉयलेट में सेंसर वृद्धि का अनुकरण करें और तत्काल वृद्धि का परीक्षण करें"
        AppLanguage.ENGLISH -> "Simulate live sensor spike in Jayanagar e-Toilet to test instant Danger Zone escalation and auto-dispatch"
    }

    fun getTriggerSpike(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸ್ಪೈಕ್ ಪ್ರಚೋದಿಸಿ"
        AppLanguage.HINDI -> "स्पाइक ट्रिगर करें"
        AppLanguage.ENGLISH -> "Trigger Spike"
    }

    fun getDirectDispatchTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ನೇರ ಸಿಬ್ಬಂದಿ ಕರ್ತವ್ಯ ನಿಯೋಜನೆ"
        AppLanguage.HINDI -> "प्रत्यक्ष सफाई कर्मचारी कार्य आवंटन"
        AppLanguage.ENGLISH -> "Direct Staff Duty Dispatch"
    }

    fun getDirectDispatchDesc(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಯಾವುದೇ ಸಾರ್ವಜನಿಕ ಶೌಚಾಲಯಕ್ಕೆ ರಿಯಲ್-ಟೈಮ್ ಎಚ್ಚರಿಕೆ ಮತ್ತು ೧೦ ನಿಮಿಷಗಳ ಎಸ್‌ಎಲ್‌ಎ ಟೈಮರ್‌ನೊಂದಿಗೆ ಸಿಬ್ಬಂದಿಯನ್ನು ನಿಯೋಜಿಸಿ"
        AppLanguage.HINDI -> "10-मिनट एसएलए टाइमर के साथ किसी भी सार्वजनिक सुविधा में सफाई कर्मचारी आवंटित करें"
        AppLanguage.ENGLISH -> "Assign sanitation staff to any public facility with real-time alert and 10-min SLA timer."
    }

    fun getDispatchDuty(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಕರ್ತವ್ಯ ನಿಯೋಜಿಸಿ"
        AppLanguage.HINDI -> "ड्यूटी आवंटित करें"
        AppLanguage.ENGLISH -> "Dispatch Duty"
    }

    fun getSanitationBoardTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ನೈರ್ಮಲ್ಯ ಸೌಲಭ್ಯಗಳ ಪ್ರದರ್ಶನ ಫಲಕ"
        AppLanguage.HINDI -> "स्वच्छता सुविधाएं प्रदर्शन बोर्ड"
        AppLanguage.ENGLISH -> "Sanitation Facilities Display Board"
    }

    fun getViewOnMap(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ನಕ್ಷೆಯಲ್ಲಿ ವೀಕ್ಷಿಸಿ"
        AppLanguage.HINDI -> "मानचित्र पर देखें"
        AppLanguage.ENGLISH -> "View on Map"
    }

    // Supervisor Dashboard Strings
    fun getSupervisorDesk(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮೇಲ್ವಿಚಾರಕ ಡೆಸ್ಕ್"
        AppLanguage.HINDI -> "पर्यवेक्षक डेस्क"
        AppLanguage.ENGLISH -> "Supervisor Desk"
    }

    fun getSupervisorJurisdiction(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ವ್ಯಾಪ್ತಿ: ಮಧ್ಯ ಮತ್ತು ದಕ್ಷಿಣ ಬೆಂಗಳೂರಿನ ಸಾರ್ವಜನಿಕ ಶೌಚಾಲಯಗಳು"
        AppLanguage.HINDI -> "अधिकार क्षेत्र: मध्य एवं दक्षिण बेंगलुरु के सार्वजनिक शौचालय"
        AppLanguage.ENGLISH -> "Jurisdiction: Public Toilets in Central & South Bengaluru"
    }

    fun getActiveCleaningDuties(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸಕ್ರಿಯ ಸ್ವಚ್ಛತಾ ಕರ್ತವ್ಯಗಳು"
        AppLanguage.HINDI -> "सक्रिय सफाई कार्य"
        AppLanguage.ENGLISH -> "Active Cleaning Duties"
    }

    fun getPublicToiletsUnderSupervision(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮೇಲ್ವಿಚಾರಣೆಯಲ್ಲಿರುವ ಸಾರ್ವಜನಿಕ ಶೌಚಾಲಯಗಳು"
        AppLanguage.HINDI -> "पर्यवेक्षणाधीन सार्वजनिक शौचालय"
        AppLanguage.ENGLISH -> "Public Toilets Under Supervision"
    }

    fun getAssignStaff(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸಿಬ್ಬಂದಿ ನಿಯೋಜಿಸಿ"
        AppLanguage.HINDI -> "कर्मचारी आवंटित करें"
        AppLanguage.ENGLISH -> "Assign Staff"
    }

    fun getReassignStaff(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಮರುನಿಯೋಜನೆ"
        AppLanguage.HINDI -> "पुनः आवंटित करें"
        AppLanguage.ENGLISH -> "Reassign Staff"
    }

    fun getEscalateToBbmp(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಬಿಬಿಎಂಪಿಗೆ ಏರಿಸಿ"
        AppLanguage.HINDI -> "बीबीएमपी को प्रेषित करें"
        AppLanguage.ENGLISH -> "Escalate to BBMP"
    }

    fun getActionRequiredTimeout(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ತುರ್ತು ಕ್ರಮ ಅಗತ್ಯ: ಸಿಬ್ಬಂದಿ ಸ್ವೀಕಾರ ಸಮಯ ಮೀರಿದೆ!"
        AppLanguage.HINDI -> "कार्रवाई आवश्यक: कर्मचारी स्वीकृति समय समाप्त!"
        AppLanguage.ENGLISH -> "Action Required: Staff Acceptance Timeout!"
    }

    fun getActiveCleanlinessAlerts(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಸಕ್ರಿಯ ಸ್ವಚ್ಛತಾ ಎಚ್ಚರಿಕೆಗಳು"
        AppLanguage.HINDI -> "सक्रिय स्वच्छता चेतावनियां"
        AppLanguage.ENGLISH -> "Active Cleanliness Alerts"
    }

    fun getCitizenReportsTitle(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ನಾಗರಿಕ ಪ್ರತಿಕ್ರಿಯೆ ಮತ್ತು ವರದಿಗಳು"
        AppLanguage.HINDI -> "नागरिक प्रतिक्रिया एवं रिपोर्ट"
        AppLanguage.ENGLISH -> "Citizen Feedback & Reports"
    }

    fun getAcceptDuty(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಕರ್ತವ್ಯ ಸ್ವೀಕರಿಸಿ"
        AppLanguage.HINDI -> "ड्यूटी स्वीकार करें"
        AppLanguage.ENGLISH -> "Accept Duty"
    }

    fun getDeclineDuty(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ತಿರಸ್ಕರಿಸಿ"
        AppLanguage.HINDI -> "अस्वीकार करें"
        AppLanguage.ENGLISH -> "Decline"
    }

    fun getResolve(lang: AppLanguage): String = when (lang) {
        AppLanguage.KANNADA -> "ಪರಿಹರಿಸಿ"
        AppLanguage.HINDI -> "समाधान करें"
        AppLanguage.ENGLISH -> "Resolve"
    }
}
