# M-Pesa Analyzer App

Android mobile application that captures, encrypts, and synchronizes financial M-Pesa SMS messages to a self-hosted cloud backend for financial analytics and AI classification.

Stack: Kotlin, Android SDK 35, Jetpack Compose, Retrofit 2, OkHttp 4, Room/WorkManager

**If you only need to run or test the mobile app, this page is enough.**  
Software engineers: [docs/README.md](docs/README.md).

---

## What “Running” Looks Like

| Piece | How to Open / Verify | Expected State |
|-------|----------------------|----------------|
| **Debug App** | Launch from Android Studio or APK | Splash screen loads; requests SMS permission |
| **Home Dashboard** | Main activity screen | Shows sync count, last upload timestamp, spending chart |
| **API Connection** | Settings $\to$ Test Connection | Successfully reaches backend on port 9002 |
| **Foreground Sync** | Tap "Fetch & Sync" | Shows progress notification during batch encryption |

---

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17+
- **Device**: Android phone or emulator running Android 10.0+ (API 29+)
- Running **M-Pesa Analyzer WebApp** server at `http://10.0.2.2:9002` (emulator) or LAN IP (phone)

---

## Setup and Run

1. Clone the repository:
   ```bash
   git clone https://github.com/Niccher/MPesa-Analyzer-App.git
   cd MPesa-Analyzer-App
   ```
2. Open the project in Android Studio and let Gradle sync dependencies.
3. Connect a physical Android device via USB (with USB debugging enabled) or start an emulator.
4. Run the debug build:
   ```bash
   ./gradlew installDebug
   ```
5. Launch the app on your device and configure the backend URL:
   - **For Emulator**: `http://10.0.2.2:9002/`
   - **For Physical Device**: `http://<YOUR_LAN_IP>:9002/`
6. Grant SMS permissions and tap **Fetch & Sync**.

---

## Configuration Users May Change

| Setting | In-App Location | Purpose |
|---------|-----------------|---------|
| `Backend URL` | Setup / Settings | Server address for API ingestion |
| `Biometric Lock` | Settings | Require fingerprint or face unlock |
| `Dark Theme` | Settings | Toggle dark/light appearance |

Full configuration details: [docs/user/configuration.md](docs/user/configuration.md).

---

## Something Went Wrong?

- **Cannot reach backend on localhost**: Use `http://10.0.2.2:9002/` on emulator instead of `localhost`.
- **Cleartext HTTP blocked**: Verify development uses cleartext traffic permissions.
- **SMS messages not detected**: Verify SMS permissions are granted in system settings.
- Operational troubleshooting: [docs/user/troubleshooting.md](docs/user/troubleshooting.md).

---

## Software Engineers

- **Android Service Architecture**: [docs/services/android.md](docs/services/android.md)
- **Local Dev & Gradle Tasks**: [docs/engineering/local-development.md](docs/engineering/local-development.md)
- **Cryptography & Security**: [docs/engineering/security.md](docs/engineering/security.md)
- **Making Changes & Tasks**: [docs/engineering/making-changes.md](docs/engineering/making-changes.md)
- **Testing Guide**: [docs/engineering/testing.md](docs/engineering/testing.md)
- **System Architecture Anchor**: [ML Mpesa Analyzer Architecture](https://github.com/Niccher/ML-Mpesa-Analyser/tree/main/docs/architecture)
