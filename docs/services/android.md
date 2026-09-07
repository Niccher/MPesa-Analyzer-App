# Android Service Handbook — Mpesa Analyzer App

Technical guide for Android engineers covering architecture patterns, network interceptors, and background sync workers.

---

## 1. Technical Stack & Build Properties

- **Language**: Kotlin 1.9+
- **SDK Targets**: `compileSdk = 35`, `targetSdk = 35`, `minSdk = 29` (Android 10.0+)
- **Architecture**: MVVM (ViewModel, LiveData, ViewBinding, Jetpack Compose)
- **Serialization**: Dual converter setup (Retrofit Gson + Moshi converters)

---

## 2. Ingestion & Foreground Synchronization

1. **Watermark SMS Scanning**: Queries `Telephony.Sms.CONTENT_URI` filtering by `date > last_upload_time` stored in `AppPrefs`.
2. **Foreground Service (`UploadService`)**: Runs with `dataSync` foreground service type to prevent Android OS process termination during long-running batch uploads.
3. **Scheduled Sync (`MpesaSyncWorker`)**: WorkManager periodic worker scheduled to run daily at 8:00 PM when network connectivity is unmetered.
4. **OkHttp Disk Cache**: 10 MB dedicated cache at `context.cacheDir/http_cache`.
   - **Online**: `Cache-Control: public, max-age=7200` (2-hour response cache).
   - **Offline**: `only-if-cached, max-stale=604800` (serves up to 7-day stale cache when disconnected).

---

## 3. UI Analytics & Presentation

- **Charts**: Uses `MPAndroidChart` for stacked bar charts, line graphs, and category distribution pie charts.
- **PDF Export**: Generates local transaction history reports using `iTextPDF`.
- **Token Scanning**: Scans access token QR codes using `ZXing` barcode scanner library.
