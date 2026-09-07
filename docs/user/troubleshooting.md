# Troubleshooting Guide (Operator) — Mpesa Analyzer App

Common runtime issues when running the Android client.

---

## 1. App Cannot Reach Backend

**Symptom**: Network timeout or `Failed to connect to /127.0.0.1:9002`.

**Cause**: `localhost` refers to the Android device itself.

**Remedy**:
- If using an emulator, set backend URL to `http://10.0.2.2:9002/`.
- If using a physical phone, ensure phone and host PC are on the same Wi-Fi network and use your PC's LAN IP address.

---

## 2. Cleartext HTTP Error

**Symptom**: `CLEARTEXT communication to 10.0.2.2 not permitted by network security policy`.

**Remedy**:
Ensure `android:usesCleartextTraffic="true"` is present in `AndroidManifest.xml` for debug development builds.

---

## 3. SMS Not Ingested

**Symptom**: Dashboard displays 0 messages found.

**Remedy**:
1. Check that SMS permission is enabled in device system settings (**Settings $\to$ Apps $\to$ M-Pesa Analyzer $\to$ Permissions**).
2. Note that the app tracks a timestamp watermark: only SMS received after the last sync watermark will be read on subsequent runs.
