<div align="center">

# Mpesa Analyzer App

**Automatically parse, encrypt, and sync your MPESA SMS to a secure cloud backend — powered by LLM-based financial classification.**

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Android](https://img.shields.io/badge/Android-API_29+-green.svg?logo=android)](https://developer.android.com/)
[![Backend](https://img.shields.io/badge/Backend-CodeIgniter_4-EF4223.svg?logo=codeigniter)](https://codeigniter.com/)
[![Database](https://img.shields.io/badge/Database-MySQL_8.4-4479A1.svg?logo=mysql)](https://www.mysql.com/)
[![LLM](https://img.shields.io/badge/LLM-Qwen2.5_1.5B-8A2BE2.svg)]()
[![Docker](https://img.shields.io/badge/Docker-Supported-2496ED.svg?logo=docker)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

</div>

---

## The Three-Repos Ecosystem

This Android app is the **data capture layer** of a three-part stack. It works together with two other repositories:

```
                    ┌──────────────────────┐
                    │  Android App          │ ◄── YOU ARE HERE
                    │  (This repo)          │
                    │                       │
                    │  Reads MPESA SMS      │
                    │  AES-128 encrypts     │
                    │  Uploads to backend   │
                    └─────────┬────────────┘
                              │ POST /process/upload
                              ▼
┌─────────────────────────────────────────────────────────────┐
│  CI4 Web Backend            ┌──────────────────────────────┐│
│  (Mpesa Analyzer WebApp)    │  Docker LLM Service          ││
│                              │  (Mpesa Analyser Docker)    ││
│  Decrypts payload           │                              ││
│  Stores in MySQL            │  Polls DB for unprocessed    ││
│  Serves web dashboard      │  Classifies senders via LLM  ││
│  Triggers LLM processing   │  Extracts transactions       ││
│  [PHP / CodeIgniter 4]     │  Writes back to DB           ││
│                             │  [Python / FastAPI / Qwen2.5]││
└─────────────────────────────┴──────────────────────────────┘
```

**Dependency chain:**
- **Android App** → sends data to **Web Backend**
- **Web Backend** → stores SMS, triggers **Docker LLM Service**
- **Docker LLM Service** → reads/writes the shared **MySQL database** used by the Web Backend

---

## How Hardcoded Scanning + LLM Classification Work Together

This app uses a **hybrid approach** that combines the best of both worlds:

### On-device: Hardcoded regex scanner (fast & private)
The Android app's `MpesaParser` uses hand-written regex patterns to parse SMS fields (amount, sender, transaction type). This happens instantly on the device with zero network calls. The raw base64-encoded SMS bodies are then encrypted and uploaded to the backend.

### Server-side: LLM-powered classification (smart & adaptive)
Once SMS reach the MySQL database, the Docker LLM service processes them:

| Task | What the LLM does that regex cannot |
|------|--------------------------------------|
| **Sender classification** | Identifies unknown senders, resolves "MPE802" as "KCB MPESA", categorises into 7 financial categories |
| **Direction inference** | Understands "You have received KSH500 from" vs "KSH500 sent to" — even with unconventional wording |
| **Amount extraction** | Handles "Ksh.500/=", "KES 500.00", "500 bob" — all parsed correctly |
| **Counterparty resolution** | Extracts business names, personal names, Till/PayBill numbers from free-form text |
| **Fuliza detection** | Identifies Fuliza limit changes, loan disbursements, and repayments within the same SMS |
| **Format resilience** | Survives SMS template changes by Safaricom, Airtel, or banks without app updates |

**Result:** The Android app stays lightweight and responsive. The heavy AI processing happens offline on your server, using a local LLM that costs nothing per query.

---

## Features

### SMS Parsing & Upload Pipeline

| Feature | Detail |
|---|---|
| **On-device SMS reading** | Queries `Telephony.Sms.CONTENT_URI` using a timestamp watermark so only new messages are processed |
| **Category detection** | Regex-based `MpesaParser` recognises 9 MPESA categories: Money Sent, PayBill, Buy Goods, Airtime, Withdrawal, Money Received, Fuliza, M-Shwari, Bank Transfer, Fees |
| **AES-128-CBC encryption** | Secure dynamic IV protocol — Raw JSON payload is encrypted using a session-unique random IV (`SecureRandom`) and prefixed to the stream; decrypted server-side by `CryptoHelper` |
| **Foreground service** | `UploadService` runs as a persistent foreground service with progress notifications (dataSync) |
| **Scheduled nightly sync** | `MpesaSyncWorker` (WorkManager) triggers daily at 8 PM |
| **Temp file cleanup** | Plaintext and encrypted temp files are deleted after a successful upload |
| **Upload progress tracking** | Custom `ProgressRequestBody` reports real-time percentage to the notification |

### Visual Analytics

| Screen | Features |
|---|---|
| **Home Dashboard** | SMS permission status, last upload timestamp, sync count, "Fetch & Sync" button, local PieChart for 10-day spending insights |
| **Graph (Financial Analytics)** | Stacked Bar Chart / Line Graph toggle, KPI cards showing total Received/Sent counts, historical data fetched from server |
| **Upload History** | RecyclerView of past uploads with Android built-in drawable icons (download/upload/help), clickable to drill into summary, CSV/PDF export via FAB (iTextPDF) |
| **Summary Info (Drill-down)** | Expandable card sections: General, Sent, Received, Balance, Fuliza, Errors — each metric is clickable and navigates to filtered transactions |
| **Transactions** | Local SMS search with type filter (Sent/Received/Paybill/Withdraw), MaterialDatePicker date range, SearchView text search, navigable from Summary with pre-applied category |
| **Settings** | Dark theme toggle, biometric lock toggle, backend URL configuration, logout, delete data, delete account |

### Profile & Account

| Feature | Detail |
|---|---|
| **Server-side profile** | Username, email, member-since date fetched from `JsonAuthUser.getUserInfo()` |
| **Upload stats** | Sync count (from local prefs), total uploads and last sync time (from server summaries) |
| **Device fingerprint registration** | 15 hardware/build fields (`Build.DEVICE`, `Build.MODEL`, `Build.FINGERPRINT`, etc.) sent to `/process/device` on first auth; returned `print_id` persisted for subsequent uploads |

### Security & Privacy

| Feature | Detail |
|---|---|
| **FLAG_SECURE** | Screenshots blocked on MainActivity and LockActivity |
| **Biometric lock** | AndroidX BiometricPrompt with BIOMETRIC_STRONG + DEVICE_CREDENTIAL (API 30+) support |
| **Token-based auth** | 12-character alphanumeric token scanned via QR or typed; verified server-side via SHA-256 |
| **On-device privacy** | All SMS parsing happens locally — raw SMS never transmitted (only parsed + encrypted) |
| **Data ownership** | Delete data and delete account endpoints available |

### Caching & Offline

| Mechanism | Detail |
|---|---|
| **OkHttp disk cache** | 10 MB dedicated cache at `context.cacheDir/http_cache` |
| **Online mode** | `Cache-Control: public, max-age=7200` (2-hour cache for API responses) |
| **Offline mode** | Up to 7-day stale cache served via `only-if-cached` + `max-stale=604800` interceptor |
| **SharedPreferences** | Auth token, device ID, sync count, last upload timestamp persisted across reboots |
| **Watermark sync** | `last_upload_time` prevents re-reading old SMS on subsequent uploads |

---

## Tech Stack

### Android Client

| Technology | Purpose |
|---|---|
| **Kotlin** | Primary language |
| **MVVM** | Architecture pattern (ViewModel + LiveData) |
| **Retrofit 2 + OkHttp 4** | REST API communication with interceptors (logging, caching, offline) |
| **Coroutines + LifecycleScope** | Async for background work |
| **ViewBinding** | Type-safe view access |
| **MPAndroidChart** | Bar, line, and pie charts |
| **AndroidX Biometric** | Fingerprint / face unlock |
| **iTextPDF** | Export history to PDF |
| **ZXing (barcodescanner)** | QR code token scanning |
| **Gson + Moshi** | JSON serialization (dual adapter) |
| **WorkManager** | Scheduled nightly sync worker |
| **AES-128-CBC** | On-device encryption before upload |

### Backend Ecosystem

| Repository | Technology | Role |
|-----------|------------|------|
| **CI4 Web App** | PHP 8.3, CodeIgniter 4, Shield | REST API, dashboard, MySQL storage, user management |
| **Docker LLM Service** | Python 3.12, FastAPI, llama.cpp, Qwen2.5 1.5B | Sender classification, transaction extraction, DB enrichment |

---

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17+ (recommended)
- **Docker Desktop** (for backend + LLM)
- **Git**

---

## Setup

### 1. Clone All Three Repositories

```bash
git clone https://github.com/YourOrg/Mpesa_Analyzer_App.git
git clone https://github.com/YourOrg/Mpesa_Analyzer_WebApp.git
git clone https://github.com/YourOrg/Mpesa_Analyser_Docker.git
```

### 2. Start the Backend Stack

```bash
# Start MySQL + Web App
cd "Mpesa Analyzer WebApp"
docker compose up --build -d

# Start LLM Service
cd "Mpesa Analyser Docker"
cp .env.example .env
# Download GGUF model into models/
wget -P models/ https://huggingface.co/Qwen/Qwen2.5-1.5B-Instruct-GGUF/resolve/main/qwen2.5-1.5b-instruct-q4_k_m.gguf
docker compose up --build -d
```

### 3. Configure Android App

Open the Android project in Android Studio. The backend URL is configured inside the app at first launch (SetupActivity), or via **Settings**.

- **For emulator**: `http://10.0.2.2:9002/`
- **For physical device**: `http://<YOUR_LAN_IP>:9002/`

### 4. Run

Select a target device and press **Run** (`Shift+F10`).

---

## API Endpoints Consumed

| Endpoint | Method | Purpose |
|---|---|---|
| `/process/verify_token` | POST | Verify login token |
| `/process/device` | POST | Register device fingerprint |
| `/process/upload` | POST | Upload encrypted SMS file |
| `/process/get/my_uploads` | POST | List upload summaries |
| `/process/get/my_summary_calculations` | POST | Detailed summary for one upload |
| `/process/get/my_uploads_count` | POST | Total sync count |
| `/process/get/my_uploads_graph` | POST | Graph data (last 3 uploads) |
| `/process/get/user_info` | POST | User profile data |
| `/process/delete_data` | POST | Delete user data |
| `/process/delete_account` | POST | Delete account |

---

## Contributing

1. Fork the Project
2. Create a Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## License

Distributed under the **MIT License**.

---

## Support

**Email**: [info@chegecache.co.ke](mailto:info@chegecache.co.ke)
**Website**: [chegecache.co.ke](https://chegecache.co.ke)
