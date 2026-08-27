# Changelog — MPesa Analyzer Android App

All notable changes to this project will be documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).
Versioning follows [Semantic Versioning](https://semver.org/).

---

## [3.3.1] — 2026-08-27

### Fixed
- **Critical package namespace correction** — `ServiceGenerator.kt` package
  declaration was still pointing at the legacy namespace
  `com.niccher.mpesa_analyzer.helpers` left over from the Kotlin naming-convention
  refactor (34da3c0). All consumers now correctly import from
  `com.niccher.mpesa_analyzer_app.helpers`.
- Fixed stale imports in 10 classes that imported `ServiceGenerator` from the
  old package:
  - `auth/TokenAuthActivity.kt`
  - `fragments/Graph/GraphFragment.kt`
  - `fragments/History/HistoryFragment.kt`
  - `fragments/History/SummaryFragment.kt`
  - `fragments/Home/HomeFragment.kt`
  - `fragments/Profile/ProfileFragment.kt`
  - `fragments/Settings/SettingsFragment.kt`
  - `helpers/DeviceFingerprint.kt`
  - `services/UploadService.kt`
  - `splash/SetupActivity.kt`
- Fixed Material3 theme attribute typo: `color_primary` → `colorPrimary` in
  both `values/themes.xml` and `values-night/themes.xml` for
  `Theme.MPesaAnalyzer` and `Theme.MPesaAnalyzer_NoAction`.
- Aligned transaction badge colors in `adapter/TransactionsAdapter.kt` with
  the centralised design-system color tokens:
  - `paybill`  → `R.color.cat_sacco`     (was `R.color.purple_700`)
  - `withdraw` → `R.color.semantic_info` (was `R.color.teal_700`)

---

## [3.3.0] — 2026-08-26

### Added
- Per-user category rules engine on the server side with exact/contains
  matching, hit telemetry, and isolated retroactive backfill.
- Dynamic server-version check in `AppInfo` screen — displays an update
  banner when the installed APK is behind the server's published version.
- Retrofit integration for the system version API (`/api/v1/system/version`).
- Secure dynamic IV generation for AES-128-CBC encryption exchange.

### Changed
- Full Kotlin naming-convention refactor across all packages (camelCase →
  PascalCase for files, uniform `_app` suffix in package names).
- `README.md` refreshed to document the complete three-repo ecosystem.

---

## [3.2.0] — 2026-08-25

### Added
- Parallelized LLM calls in the ML backend (`asyncio.gather`).
- Blocked-sender bypass — processing jobs auto-skip senders on the blocklist.
- Carrier and dual-SIM logging in the SMS uploader.
- Upgraded parser regexes for KES currency formatting.

---

## [3.1.0] — 2026-08-11

### Added
- PIN lock, profile management, manual sync, and data export in Settings.
- AES crypto keys delivered via `BuildConfig` fields (no hardcoded strings).

---

## [3.0.0] — 2026-08-05

### Added
- Permission-free 18-signal device fingerprint with composite SHA-256 hash.
- Batched inbox-only SMS upload with `_id` cursor watermark and `SyncSession`.
- Complete financial dashboard overhaul with server-driven analytics,
  category tracking, and interactive MPAndroidChart charts.
- Logout, account deletion, and data deletion flows.
- SMS upload synchronization pipeline fix.
