# CleanNode — Project Context & Developer Reference Guide

> **Project Name:** CleanNode  
> **Type:** IoT-based Public Sanitation & Smart City Restroom Management System  
> **Target Region:** Bengaluru (BBMP - Bruhat Bengaluru Mahanagara Palike), Karnataka, India  
> **Languages Supported:** English, Kannada (ಕನ್ನಡ), Hindi (हिंदी)  
> **Key Technologies:** Native Android (Kotlin, Jetpack Compose, Room DB, Material 3, Firebase, OkHttp, Coroutines), Google Gemini 3.5 Flash AI

---

## 1. Executive Summary & Purpose

**CleanNode** is an intelligent public sanitation platform engineered to solve chronic hygiene, maintenance visibility, and accountability failures in urban public restrooms (e-toilets, BBMP public complexes, and transit hubs in Bengaluru).

The platform continuously consumes multi-sensor IoT telemetry, computes an automated **Predictive Cleanliness Index (PCI, 0–100)**, drives a **4-stage automated escalation ladder** (dispatching staff, alerting supervisors, and reporting to municipal command upon timeout), and empowers citizens with real-time cleanliness transparency, smart geospatial navigation, and multilingual AI assistance.

---

## 2. System Architecture & Tech Stack

The repository contains:
* **Production Native Android Application** (`/app`): Complete, full-featured native Android app built with modern Jetpack Compose, Room database, Firebase Realtime/Firestore/Auth/Storage/FCM, OkHttp Gemini client, and native Android telephony/notification engines.

### Native Android Stack (`app/`)
| Component | Technology | Details / Rationale |
| :--- | :--- | :--- |
| **Language & Tooling** | Kotlin 2.0+ / Gradle KTS | Modern null-safe, coroutine-native Android development |
| **UI Framework** | Jetpack Compose & Material 3 | Declarative UI, dynamic theming, smooth animations |
| **Local Persistence** | Android Room Database (v2) with KSP | Offline-first database storing users, facilities, duties, escalations, reports |
| **Architecture Pattern** | MVVM + Repository Pattern | `CleanNodeApp` -> `CleanNodeViewModel` -> `CleanNodeRepository` -> `CleanNodeDao` |
| **State Management** | Kotlin Coroutines `StateFlow` & `Flow` | Reactive stream-based state with `collectAsStateWithLifecycle` |
| **AI Engine** | Google Gemini 3.5 Flash | Direct REST API integration via OkHttp with intelligent multilingual fallbacks |
| **Mapping Engine** | Custom Hardware-Accelerated Compose Canvas | Custom rendering of Bengaluru arterial corridors, pulse GPS, pins, routes |
| **Notifications & SMS** | Android NotificationManager & SmsManager | Heads-up system notifications, channels, and real SMS dispatch |
| **Testing** | JUnit 4, Robolectric (SDK 36), Compose UI Test | Context verification, unit tests, and instrumented UI tests |

### React Native Stack (`react-native-cleannode/`)
| Component | Technology |
| :--- | :--- |
| **Core Framework** | React Native 0.74.2, React 18.2.0 |
| **Language** | TypeScript 5.0.4 |
| **Navigation** | `@react-navigation/native`, `@react-navigation/native-stack`, `@react-navigation/bottom-tabs` |
| **Localization** | `i18next`, `react-i18next` |
| **Storage & Device** | `@react-native-async-storage/async-storage`, `react-native-maps`, `react-native-camera` |

---

## 3. Core Domain Concepts & Business Rules

### 3.1 Predictive Cleanliness Index (PCI)
The PCI is a score from **0 to 100** computed from fused multi-sensor IoT telemetry:
* **80 – 100 (Clean / Excellent - Green):** Restroom in optimal condition. Regular monitoring.
* **50 – 79 (Moderate - Yellow):** Acceptable hygiene, moderate footfall.
* **30 – 49 (Needs Attention - Orange):** Odor or footfall accumulating; scheduled for maintenance.
* **< 30 (Danger Zone / Critical - Red):** Severe degradation. Triggers automated escalation.

### 3.2 Multi-Sensor IoT Telemetry Suite
1. **MQ-135 Gas Sensor:** Measures ammonia ($\text{NH}_3$) and foul air quality in ppm ($<3.0\text{ ppm}$ normal, $>6.0\text{ ppm}$ alert, $>8.0\text{ ppm}$ critical).
2. **YF-S201 Water Flow Sensor:** Measures flush volume and flow rate in L/min ($>4.5\text{ L/min}$ normal, $<1.5\text{ L/min}$ blockage suspected).
3. **PIR Sensor HC-SR501 & Door Reed Switch:** Detects physical occupancy and counts cumulative daily footfall.
4. **DHT22 Sensor:** Monitors ambient temperature (°C) and relative humidity (%).
5. **Fluid Level Sensors:** Monitors water reservoir tank level (%) and soap dispenser levels (%).

### 3.3 Public vs. Private Toilet Classification (FR14.2 & FR14.3)
* **Monitored Public Restrooms:** Display live IoT sensor telemetry, live PCI score, cleanliness band, and amenities.
* **Private / Commercial Restrooms (e.g. Malls, Cafes):** Clearly categorized as `Private (Unmonitored)`. Never assigned an inferred or fake score; marked with lock icon and `"Cleanliness data unavailable (Private unmonitored restroom)"`.

### 3.4 Smart Google Ranking Algorithm (FR14.5)
Candidate restrooms are sorted by walking distance. However, if two facilities are within a **50-meter tolerance band**, the algorithm tie-breaks in favor of the cleaner facility if its PCI is at least **10 points higher**.

### 3.5 Danger Zone Escalation Ladder (FR15.1 – FR15.6)
When a restroom enters the Danger Zone ($\text{PCI} < 30$, ammonia $>8.0\text{ ppm}$, or flush flow $<1.5\text{ L/min}$):
1. **Stage 1 (Staff Dispatched):** Automatically ranks and assigns the nearest on-duty sanitation staff member. A 10-minute acceptance countdown window begins. Real heads-up notification and telecom SMS are dispatched.
2. **Stage 2 (Supervisor Escalated):** If the staff does not accept within 10 minutes or explicitly declines, the system automatically escalates to the Toilet Supervisor (SMS + Push Notification).
3. **Stage 3 (Municipal Admin Escalated):** If the supervisor does not resolve the issue, the escalation is elevated to the BBMP City Central Admin Dashboard as top priority.
4. **Stage 4 (Resolved):** Cleared when maintenance staff submits photographic proof of cleaning and clears the 5-point hygiene checklist.

### 3.6 Proof of Cleaning & AI Verification (FR13.5)
Staff must verify a 5-point checklist:
- [x] Floor mopped
- [x] Toilet bowl disinfected
- [x] Water supply checked
- [x] Soap dispenser refilled
- [x] Trash bin emptied
Accompanied by photographic proof, verified by Gemini AI, which restores the facility's PCI back to 95+.

---

## 4. User Roles & Access Matrix

| Role | Target Persona | Key Capabilities |
| :--- | :--- | :--- |
| **`CITIZEN`** | General public in Bengaluru | Interactive map, live PCI lookup, amenities filter, walking navigation, AI chat assistant, issue reporting with 1-5 star ratings |
| **`STAFF`** | Field sanitation workers | Active cleaning duty roster, 10-minute accept/decline timer, checklist & camera photo proof upload, restroom map |
| **`SUPERVISOR`** | Zonal toilet supervisors | Real-time facility roster, active escalation ladder oversight, manual duty assignment & reassignment, staff workload tracking |
| **`ADMIN`** | BBMP Municipal Command | City-wide health analytics, predictive maintenance alerts, anomaly injection & stress testing, central audit logs |

---

## 5. Directory Structure & Key Files

```
cleannode (1)/
├── .env.example                               # Template for GEMINI_API_KEY
├── .gitignore                                 # Git ignore patterns
├── README.md                                  # AI Studio setup & quickstart
├── metadata.json                              # App manifest & capabilities
├── build.gradle.kts                           # Root Gradle build script
├── settings.gradle.kts                        # Gradle subproject configurations
├── gradle.properties                          # JVM memory & AndroidX settings
├── gradle/
│   ├── libs.versions.toml                     # Version catalog (Compose, Room, Firebase, etc.)
│   └── wrapper/
│       └── gradle-wrapper.properties          # Gradle 8.13 wrapper config
├── app/                                       # Native Android Application Module
│   ├── build.gradle.kts                       # Android build config, dependencies, KSP, signing
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml            # Permissions (Location, Camera, SMS, Notifications)
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt            # Entry activity & runtime permission requests
│   │   │   │   ├── ai/
│   │   │   │   │   └── GeminiSanitationService.kt # Gemini 3.5 Flash REST client & local fallbacks
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── CleanNodeDao.kt    # Room DAO interface with reactive Flows
│   │   │   │   │   │   ├── CleanNodeDatabase.kt # Room DB declaration & realistic Bengaluru seed data
│   │   │   │   │   │   └── Converters.kt      # Room TypeConverters for Enums
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── Entities.kt        # UserEntity, FacilityEntity, CleaningDutyEntity, etc.
│   │   │   │   │   │   ├── Enums.kt           # UserRole, AppLanguage, CleanlinessBand, DutyStatus, etc.
│   │   │   │   │   │   └── MonitoringModels.kt# CubicleLiveSensor, ThresholdAlert, PredictiveMaintenanceItem
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── CleanNodeOperationsProvider.kt # In-memory live operational sensor data & staff lists
│   │   │   │   │       └── CleanNodeRepository.kt # Central business logic, smart ranking, auto-dispatch loop
│   │   │   │   ├── localization/
│   │   │   │   │   └── LocalizationManager.kt # Full trilingual dictionary (English, Kannada, Hindi)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── CleanNodeApp.kt        # Root Composable, top app bar, role switcher, navigation
│   │   │   │   │   ├── CleanNodeViewModel.kt  # Central ViewModel, 12s monitoring loop, OTP, AI actions
│   │   │   │   │   ├── components/
│   │   │   │   │   │   ├── AiDiagnosticDialog.kt      # Dialog displaying AI root-cause analysis
│   │   │   │   │   │   ├── AssignDutyDialog.kt        # Dialog for manual assignment & staff dispatch
│   │   │   │   │   │   ├── BengaluruRestroomMap.kt    # Custom Canvas interactive map of Bengaluru
│   │   │   │   │   │   ├── EscalationLadderView.kt    # Visual 4-step vertical escalation tracker
│   │   │   │   │   │   ├── FacilityDetailSheet.kt     # Bottom sheet inspecting facility details
│   │   │   │   │   │   ├── LanguageSelectorDialog.kt  # Language selection modal
│   │   │   │   │   │   ├── PciGauge.kt                # Circular animated 0-100 PCI meter
│   │   │   │   │   │   ├── ProofUploadDialog.kt       # Camera proof & checklist submission modal
│   │   │   │   │   │   └── SensorCard.kt              # 2x2 grid displaying live IoT telemetry cards
│   │   │   │   │   ├── screens/
│   │   │   │   │   │   ├── AdminDashboardScreen.kt    # BBMP Command center & predictive maintenance
│   │   │   │   │   │   ├── AiAssistantScreen.kt       # Multilingual conversational sanitation assistant
│   │   │   │   │   │   ├── CitizenDashboardScreen.kt  # Map & list views, amenities filter, citizen reporting
│   │   │   │   │   │   ├── LoginScreen.kt             # Phone auth, dynamic OTP generation, role picker
│   │   │   │   │   │   ├── StaffDashboardScreen.kt    # Assigned duties, countdown timer, proof submission
│   │   │   │   │   │   └── SupervisorDashboardScreen.kt # Roster management, escalation tracking, duty dispatch
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt               # Palette definitions (CleanGreen, DangerRed, TealPrimary)
│   │   │   │   │       ├── Theme.kt               # Material 3 light/dark theme config
│   │   │   │   │       └── Type.kt                # Typography styles
│   │   │   │   └── util/
│   │   │   │       └── NotificationHelper.kt      # Notification channels, heads-up notifications, SMS sender
│   │   │   └── res/                               # Assets, mipmap icons, drawables, strings.xml
│   │   └── test/java/com/example/
│   │       ├── ExampleRobolectricTest.kt          # Robolectric context test
│   │       └── ExampleUnitTest.kt                 # Basic JUnit assertion test
└── react-native-cleannode/                        # Parallel React Native implementation
    ├── package.json                               # Dependencies & scripts
    ├── App.tsx                                    # Main RN application component
    ├── README.md                                  # RN project documentation
    └── src/
        ├── localization/translations.ts           # i18n translations for en, kn, hi
        └── types/index.ts                         # TypeScript domain interfaces
```

---

## 6. Real-Time Telemetry & Simulation Flow

```
[IoT Sensor Simulation / Field Telemetry]
     │ (Ammonia, Flow, Temp, Humidity, Occupancy)
     ▼
[CleanNodeRepository / OperationsProvider]
     │
     ├─► Computes PCI (0 - 100) & Band Classification
     │
     ├─► If PCI < 30 (Danger Zone):
     │     ├─► Auto-dispatches nearest staff (10-min window)
     │     ├─► Emits Heads-up Notification & SMS via NotificationHelper
     │     └─► Inserts EscalationRecord (Stage 1)
     │
     ├─► If 10 minutes expire without acceptance:
     │     ├─► Auto-escalates to Supervisor (Stage 2)
     │     └─► Notifies Dr. Prakash Gowda (BBMP) via SMS/Push
     │
     └─► If unhandled by Supervisor:
           └─► Escalates to BBMP Central Command (Stage 3)
```

---

## 7. Developer Quickstart

### Prerequisites
* Android Studio Ladybug (or newer)
* JDK 17 or JDK 21 (sourceCompatibility set to Java 11)
* Android SDK 36 (compileSdk 36, minSdk 24)

### Setup Steps
1. Create a `.env` file in the project root:
   ```properties
   GEMINI_API_KEY=your_actual_gemini_api_key_here
   ```
   *(If not supplied, the app automatically runs in intelligent mock/fallback mode without crashing).*
2. Open project in Android Studio.
3. In `app/build.gradle.kts`, ensure signing configuration points to your debug keystore or default Android debug key.
4. Run `./gradlew assembleDebug` or launch directly via Android Studio on an emulator or physical device.
