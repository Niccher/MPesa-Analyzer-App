# Security Policy

The **Smart Financial SMS** companion client captures, encrypts, and transmits financial SMS alerts directly from the user's mobile device to their self-hosted **Smart Finance Platform**. Because the app handles sensitive financial messages, we uphold a **zero-trust, security-first** philosophy.

---

## Supported Versions

| Version | Supported          |
| ------- | ------------------ |
| 3.5.x   | :white_check_mark: |
| < 3.5.0 | :x:                |

---

## On-Device Security Specifications

1. **Edge Cryptography**: All outgoing SMS transactions are encrypted on-device via hardware-backed AES-256-CBC with randomized IVs prior to network transmission.
2. **Biometric Security**: Hardware-backed `BiometricPrompt` protects access to on-device spending dashboards and sensitive views. Includes a 30-second token grace window to eliminate duplicate prompts while preventing screen-recording or unauthorized access.
3. **No Third-Party SDKs**: The Android application is completely free of third-party trackers, telemetry libraries, ad networks, or cloud analytics SDKs. Network traffic routes exclusively to the self-hosted backend URL configured by the user.
4. **Verifiable Builds**: Users can inspect all Kotlin source code, dependencies, and permissions (`READ_SMS`, `RECEIVE_SMS`), and compile locally using Android Studio to verify 100% data confidentiality.

---

## Reporting a Vulnerability

If you discover an exploit, reverse-engineering flaw, or cryptographic weakness, please report it responsibly:
* **Contact**: `domi777nicch@gmail.com`
* **Subject**: `[SECURITY VULNERABILITY] Smart Financial SMS Android`
* We respond within 48 hours and work with you to patch the issue before any public release.
