# Smart Financial SMS (Android Client)

[![Release](https://img.shields.io/badge/Release-v3.5.0-blue.svg)](https://github.com/Niccher/Smart-Finance-Android/releases)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Android](https://img.shields.io/badge/Android-API_29+-green.svg)](https://developer.android.com/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF.svg)](https://kotlinlang.org/)
[![Master Platform](https://img.shields.io/badge/Master_Platform-Smart--Finance--Platform-blue.svg)](https://github.com/Niccher/Smart-Finance-Platform)

Android mobile companion client that captures, encrypts, and synchronizes financial SMS alerts (M-Pesa, Commercial Banks, Digital Lenders) to the self-hosted **Smart Finance Platform** for financial analytics, ML classification, and real-time AI conversational finance.

Stack: Kotlin, Android SDK 35, Jetpack Compose, Retrofit 2, OkHttp 4, Room/WorkManager

**If you only need to run or test the mobile app, this page is enough.**  
Software engineers: [docs/README.md](docs/README.md).

---

## Master Platform Repository

This Android application serves as the **mobile edge client** within the Smart Finance ecosystem. The **master repository** orchestrating database storage, Redis caching, system telemetry, and machine learning intelligence is:

* 🌐 **Master Platform Repository**: [https://github.com/Niccher/Smart-Finance-Platform](https://github.com/Niccher/Smart-Finance-Platform)
* **Role of Master Platform**: Acts as the central master orchestrator running the CodeIgniter 4 WebApp, MySQL 8.4 database, Redis 7 session & cache engine, and the FastAPI multi-model LLM microservice (Qwen 2.5, DeepSeek, Gemini). It provides the `/api/v1/chat` and `/api/v1/sync` REST endpoints consumed by this companion client.

---

## What “Running” Looks Like

| Piece | How to Open / Verify | Expected State |
|-------|----------------------|----------------|
| **Android App** | Launch on phone or emulator | Splash screen loads; requests SMS permission |
| **Home Dashboard** | Main activity screen | Shows sync count, last upload timestamp, spending chart |
| **AI Financial Chat** | In-App Assistant / Ask My M-Pesa | Real-time conversational finance in English & Sheng |
| **API Connection** | Settings $\to$ Test Connection | Successfully reaches master backend on port 80 (`/api/v1`) |
| **Foreground Sync** | Tap "Fetch & Sync" | Encrypts & synchronizes SMS to master cloud backend |
| **Master Platform Backend** | [Smart-Finance-Platform](https://github.com/Niccher/Smart-Finance-Platform) | Master ingestion gateway, Redis cache & ML microservice |

---

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17+
- **Device**: Android phone or emulator running Android 10.0+ (API 29+)
- Running **Smart Finance Platform** backend at `http://10.0.2.2/` (emulator) or LAN/Cloud IP `http://<SERVER_IP>/`

---

## Installation: Build from Source (Zero-Trust Security)

> [!TIP]
> **Why Build from Source?** Financial SMS apps handle sensitive financial data. Building from source allows you to inspect the Kotlin code, audit permissions, and verify that encryption occurs entirely on your device with zero telemetry trackers.

1. Clone the repository:
   ```bash
   git clone https://github.com/Niccher/Smart-Finance-Android.git
   cd Smart-Finance-Android
   ```
2. Open the project in Android Studio or compile via Gradle CLI:
   ```bash
   ./gradlew installDebug
   ```
3. Launch the app and configure your self-hosted backend URL:
   - **For Emulator**: `http://10.0.2.2/`
   - **For Physical Device**: `http://<YOUR_LAN_OR_SERVER_IP>/`
4. Grant SMS permissions and tap **Fetch & Sync**.

*(Alternatively, pre-compiled binaries with published SHA-256 checksums are available on [GitHub Releases](https://github.com/Niccher/Smart-Finance-Android/releases)).*

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

- **Cannot reach backend on localhost**: Use `http://10.0.2.2/` on emulator instead of `localhost`.
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
- **Master Platform Architecture**: [Smart-Finance-Platform Architecture Docs](https://github.com/Niccher/Smart-Finance-Platform/tree/main/docs/architecture)
