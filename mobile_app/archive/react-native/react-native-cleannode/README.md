# CleanNode Mobile — IoT-based Public Sanitation System (React Native)

**Capstone Project:** PES University, Department of Computer Applications (Jan 2026 – May 2026)  
**Project Team:** Adhya Pandey, Rishika Chowdhary B, Chinmayi C Gowda, Lavanya R  
**Faculty Guide:** Mr. Dilip Kumar Maripuri (Associate Professor, PES University)

---

## 🌟 Key Features Implemented

1. **Multilingual Preference at Login & Runtime (FR13.1)**:
   - Kannada (ಕನ್ನಡ)
   - Hindi (हिंदी)
   - English
   - Switchable at any time from top header.

2. **Phone Number Validation with OTP Authentication**:
   - Indian standard +91 10-digit mobile number format
   - Realistic 6-digit OTP verification with countdown timer and resend.

3. **Predictive Cleanliness Index (PCI 0–100) (FR1)**:
   - Real-time multi-sensor fusion:
     - MQ-135 Gas Sensor (Ammonia / odor ppm)
     - DHT22 (Temperature & Humidity)
     - PIR Sensor HC-SR501 & Reed Door Switch (Footfall & Occupancy)
     - YF-S201 Water Flow Sensor (Flush health, flow rate L/min)
   - 4 Cleanliness Bands:
     - 80–100: Clean / Excellent (Green)
     - 50–79: Moderate (Yellow)
     - 30–49: Needs Attention (Orange)
     - <30: Danger Zone / Critical (Red)

4. **Public vs Private Toilet Classification (FR14.2 & FR14.3)**:
   - Monitored Public restrooms show live IoT sensor telemetry and PCI score.
   - Private restrooms (malls, restaurants, cafes) are clearly marked "Private (Unmonitored)" and never show an inferred or fake score.

5. **Smart Google Ranking (Distance + PCI) (FR14.5)**:
   - Re-ranks candidate restrooms within a 50-meter tolerance band in favor of a significantly cleaner restroom.

6. **Danger Zone Escalation Ladder (FR15.1 – FR15.6)**:
   - Stage 1: Nearest on-duty staff dispatched with 10-minute acceptance window.
   - Stage 2: If timed out or declined, automatically escalates to Toilet Supervisor (App + SMS).
   - Stage 3: If unacknowledged, escalates to BBMP Municipal Authority Central Dashboard with full audit log (`/escalations/{alert_id}`).
   - Stage 4: Resolution via Proof of Cleaning.

7. **Interactive Restroom Map (Bengaluru Geospatial Navigation)**:
   - Visual interactive geospatial map with arterial corridors (Jayanagar, Indiranagar, Majestic, Koramangala, Whitefield).
   - Live pulsating user GPS location marker.
   - Restroom markers color-coded by real-time PCI score (Green: Clean, Yellow: Moderate, Red: Danger Zone, Grey: Private).
   - Tap-to-inspect card with walking route polyline and direct launch to Google Maps navigation (`geo:...` / `google.navigation`).
   - Seamless toggle between "🗺️ Map View" and "📋 List View".

8. **Automated Staff Duty Dispatch Engine (SRS v2.0 FR15.1 - FR15.6)**:
   - Continuous background telemetry monitoring scans for Danger Zone (<30 PCI) anomalies.
   - Automatically ranks nearest on-duty staff and assigns cleaning duty.
   - Active 10-minute acceptance countdown window.
   - Real Android heads-up system notifications + real SMS dispatch via `NotificationHelper` & `SmsManager`.
   - Automatic escalation to Supervisor and BBMP Municipal Command on timeout or refusal.

9. **Real Dynamic OTP Generation**:
   - Generates dynamic 6-digit cryptographic security code.
   - Real system notification chime/banner with verification code.
   - Real telecom SMS message sent to the entered phone number.
   - Auto-paste received OTP action button.

10. **Proof of Cleaning with Camera Upload (FR13.5)**:
   - Maintenance staff captures photo evidence upon duty completion.
   - Comprehensive checklist verification (floor mopped, bowl disinfected, water flow verified, soap refilled, trash emptied).
   - Restores restroom PCI back to 95+.

8. **Offline Support (FR13.4)**:
   - Local caching ensures the app remains usable in basement/low-signal restroom environments.

9. **AI Integration (Gemini 3.5 Flash)**:
   - Multi-sensor PCI root-cause diagnosis.
   - AI Cleaning proof verification inspector.
   - Multilingual conversational sanitation assistant in Kannada, Hindi, and English.
