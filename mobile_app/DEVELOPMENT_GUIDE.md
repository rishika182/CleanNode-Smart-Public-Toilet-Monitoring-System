# CleanNode — Developer Onboarding & Operation Guide

This guide contains everything required to configure, build, run, test, and debug the CleanNode project.

---

## 1. Environment & Tooling Prerequisites

### Android Native Environment (Primary)
* **IDE:** [Android Studio Ladybug (2024.2.1+)](https://developer.android.com/studio) or newer
* **JDK:** OpenJDK 17 or OpenJDK 21 (configured as Gradle JDK)
* **Android SDK:**
  * `compileSdk`: 36 (Android 16 / Vanilla Ice Cream)
  * `targetSdk`: 36
  * `minSdk`: 24 (Android 7.0 Nougat)
* **Build System:** Gradle 8.13 with Kotlin DSL (`build.gradle.kts`)

### React Native Environment (Companion)
* **Node.js:** v18.x or v20.x
* **Yarn / npm:** npm 9+ or yarn 1.22+
* **React Native CLI:** 0.74.2

---

## 2. Environment Configuration (`.env`)

CleanNode utilizes the **Secrets Gradle Plugin** to read configuration at compile time from a `.env` file in the project root.

1. Create a `.env` file in the project root (adjacent to `.env.example`):
   ```bash
   cp .env.example .env
   ```
2. Set your Gemini API key:
   ```properties
   GEMINI_API_KEY=AIzaSy...your_gemini_api_key_here
   ```
   > **Note:** If no key is provided, the application will build cleanly and use domain-expert heuristic fallbacks for root-cause analysis and conversational queries without errors.

---

## 3. Building & Running the Native Android App

### Option A: From Android Studio (Recommended)
1. Open Android Studio.
2. Select **File > Open...** and navigate to `cleannode (1)/`.
3. Allow Gradle to sync dependencies.
4. Verify signing configuration:
   * In `app/build.gradle.kts`, the debug build type is configured with `debugConfig` (keystore bundled at `${rootDir}/debug.keystore` or default).
5. Select a device or emulator running API 26–36.
6. Click **Run 'app'** (`Shift + F10`).

### Option B: From Command Line (PowerShell)
```powershell
# Clean build
.\gradlew.bat clean

# Assemble Debug APK
.\gradlew.bat assembleDebug

# Install on connected device/emulator
.\gradlew.bat installDebug
```
The output APK is generated at:  
`app/build/outputs/apk/debug/app-debug.apk`

---

## 4. Running Automated Tests

CleanNode includes unit tests and Robolectric context tests.

### Running Unit & Robolectric Tests
```powershell
.\gradlew.bat testDebugUnitTest
```
Tests are located in:
* `app/src/test/java/com/example/ExampleRobolectricTest.kt`: Verifies Android application context and resource strings.
* `app/src/test/java/com/example/ExampleUnitTest.kt`: Unit test baseline.

---

## 5. Working with the React Native Companion (`react-native-cleannode/`)

If developing or testing the cross-platform React Native companion app:

```powershell
cd react-native-cleannode

# Install dependencies
npm install

# Start Metro bundler
npm run start

# Launch on Android Emulator (in a separate terminal)
npm run android
```

---

## 6. Testing & Debugging Interactive Features

CleanNode includes built-in operational test triggers to facilitate end-to-end demonstrations:

### 1. Dynamic OTP Quick-Fill
* On the **Login Screen**, entering any 10-digit phone number and tapping **"Send Real OTP"** triggers dynamic code generation.
* The generated code appears in an Android heads-up system notification and a status banner.
* Tap **"Auto-Paste Received OTP"** to populate the input field instantly.

### 2. Role Switching & Demo Roster
* Once logged in, tap the **role badge or profile avatar** in the top-right corner to switch between **CITIZEN**, **STAFF**, **SUPERVISOR**, and **ADMIN** at any time.

### 3. Sensor Anomaly Simulation & Danger Zone Escalation
* Switch to the **ADMIN** role.
* Locate **Jayanagar BBMP-01** or **Majestic KSRTC Terminal**.
* Tap **"Simulate Sensor Spike"** or **"Trigger Anomaly"**.
* Observe:
  1. PCI drops to 22 (Danger Zone).
  2. Ammonia level spikes to 11.4 ppm.
  3. A new duty is automatically generated.
  4. Heads-up notification & SMS are triggered.
  5. The 10-minute acceptance timer starts ticking on the Staff and Supervisor screens.

### 4. Resolving Escalations with Cleaning Proof
* Switch to the **STAFF** role.
* Tap on an urgent duty and select **"Upload Proof of Cleaning"**.
* Check off the 5 hygiene tasks and tap **"Submit Proof"**.
* Observe the AI sanitation score assessment and the facility's PCI returning to 95+ (Clean).
