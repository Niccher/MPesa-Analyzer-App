# Setup and Run Guide — Mpesa Analyzer App

This guide walks through installing, configuring, and operating the Android client.

---

## 1. Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17+**
- Android device or emulator running **API 29+** (Android 10.0+)
- Running **M-Pesa Analyzer WebApp** backend

---

## 2. Step-by-Step Run Instructions

### Step 1: Open in Android Studio
1. Launch Android Studio.
2. Select **Open** and choose the `Mpesa_Analyzer_App` directory.
3. Allow Gradle to sync dependencies.

### Step 2: Build and Run on Target Device
1. Connect an Android physical device via USB (with USB Debugging enabled) or start an Android Virtual Device (AVD).
2. Press **Run** (`Shift+F10`) or execute from terminal:
   ```bash
   ./gradlew installDebug
   ```

### Step 3: Configure Backend URL
Upon first launch (or in **Settings**):
- **Android Emulator**: Set URL to `http://10.0.2.2:9002/`
- **Physical Device**: Set URL to your development machine's LAN IP, e.g. `http://192.168.1.50:9002/`

### Step 4: Grant Permissions & Sync
1. Grant SMS Read permission when prompted.
2. Tap **Fetch & Sync** on the Home Dashboard to trigger initial encrypted upload.
