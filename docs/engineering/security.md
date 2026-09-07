# Security & Cryptography — Mpesa Analyzer App

Details on client-side encryption, biometric authentication, and hardware fingerprinting.

---

## 1. Dynamic IV AES-128-CBC Protocol

Before network transmission, SMS messages are parsed into JSON and encrypted:

1. A 16-byte random IV is generated via `java.security.SecureRandom`.
2. Cipher is initialized with `Cipher.getInstance("AES/CBC/PKCS5Padding")` using `BuildConfig.MPESA_CRYPT_KEY` and the dynamic IV.
3. The dynamic IV is written directly into the binary output stream, immediately followed by the encrypted bytes.
4. Temporary plaintext and ciphertext cache files are deleted immediately after upload completes.

---

## 2. Window Security & Biometrics

- **`FLAG_SECURE`**: Enabled on `MainActivity` and `LockActivity` windows to prevent screenshots, screen recording, and exposure in the Android recent apps switcher.
- **Biometric Authentication**: Integrated via `androidx.biometric.BiometricPrompt`, allowing device credential fallback (PIN/Pattern) on API 30+.

---

## 3. Hardware Fingerprinting (`DeviceFingerprint.kt`)

On first login, 15 hardware attributes (`Build.DEVICE`, `Build.MODEL`, `Build.FINGERPRINT`, `Build.MANUFACTURER`, etc.) are hashed and sent to `POST /api/v1/device` to identify and bind the device to the server account.
