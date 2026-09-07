# Configuration Guide — Mpesa Analyzer App

Configuration settings managed inside the Android application.

---

## 1. Application Settings (In-App)

| Setting | Location | Purpose | Default |
|---------|----------|---------|---------|
| **Backend URL** | Setup / Settings | Base URL for REST API calls | `http://10.0.2.2:9002/` |
| **Biometric Lock** | Settings | Enforce fingerprint / face unlock on launch | Disabled |
| **Dark Theme** | Settings | Switch between Light and Dark interface modes | System Default |
| **Sync Schedule** | Background | Nightly sync worker (`MpesaSyncWorker`) | 8:00 PM Daily |

---

## 2. Network Addresses for Local Development

- **Android Studio Emulator**: Must use `http://10.0.2.2:9002/`. `localhost` refers to the emulator itself and will fail.
- **Physical Phone**: Must use your computer's local Wi-Fi IP address (e.g. `http://192.168.x.x:9002/`). Ensure your computer firewall permits incoming connections on port 9002.
