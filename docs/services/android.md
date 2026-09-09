# Android Service Handbook — Mpesa Analyzer App

Technical guide for Android engineers covering architecture patterns, CameraX QR scanning, vector assets, and background sync workers.

---

## 1. Technical Stack & Build Properties

- **Language**: Kotlin 1.9+
- **SDK Targets**: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26` (Android 8.0+)
- **Architecture**: MVVM (ViewModel, LiveData, ViewBinding, Jetpack Compose)
- **Serialization**: Dual converter setup (Retrofit Gson + Moshi converters)
- **Networking**: OkHttp 4 with certificate pinning support and dynamic IV AES interceptor

---

## 2. Ingestion & Synchronization Pipeline

1. **Watermark SMS Scanning**: Queries `Telephony.Sms.CONTENT_URI` filtering by `date > last_upload_time` stored in persistent preferences.
2. **Local SQLite Staging**: Records are staged in an on-device SQLite database prior to transmission to prevent transaction loss during network outages.
3. **AES-256 Dynamic IV Encryption**: Payloads are encrypted client-side using a dynamically generated 16-byte IV prefixed to the payload stream.
4. **Foreground & Background Workers**:
   - `UploadService`: Foreground service with `dataSync` type for reliable large batch historical imports.
   - `MpesaSyncWorker`: Jetpack WorkManager periodic worker scheduled during opportunistic network and charging windows adhering to Android Doze limits.
5. **OkHttp Disk Cache**: 10 MB dedicated cache at `context.cacheDir/http_cache`.
   - **Online**: `Cache-Control: public, max-age=7200` (2-hour response cache).
   - **Offline**: `only-if-cached, max-stale=604800` (serves up to 7-day stale cache when disconnected).

---

## 3. CameraX Vertical QR Scanner & Pairing

The pairing flow uses an integrated CameraX barcode analysis pipeline:

- **Vertical Orientation Locking**: Viewfinder constraint prevents unwanted rotation distortion on mobile devices.
- **Auto-Focus & Light Sensing**: Automatically adjusts camera exposure and triggers flash toggle under low-light conditions.
- **Direct Token Parsing**: Decodes server URL and device token payload, automatically performing handshake with WebApp API (`POST /api/v1/device`).

---

## 4. Adaptive Vector Asset Suite

The app features a modernized asset suite:

- **Vector Drawables**: All icons and brand marks utilize lossless XML vector drawables, reducing APK size and ensuring sharp rendering across `xxhdpi` and `xxxhdpi` displays.
- **Theme Awareness**: Vector tints automatically inherit Material 3 and custom Ace theme color tokens in both light and dark display modes.\n