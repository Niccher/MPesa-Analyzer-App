# ADR 0001: Client-Side Dynamic IV AES-128-CBC Payload Encryption

- **Status**: Accepted
- **Date**: 2026-09-06
- **Context**: Android Client & Mobile Ingestion Gateway

---

## Context and Problem Statement

The Android client reads SMS notifications directly from the local telephony provider (`Telephony.Sms.CONTENT_URI`) and synchronizes them to the self-hosted CodeIgniter 4 backend via HTTP POST requests (`/api/v1/upload`).

Because local dev environments and private LAN deployments may not always enforce HTTPS with signed public TLS certificates (or rely on cleartext HTTP over local Wi-Fi / emulator `10.0.2.2`), sending raw JSON SMS payloads over the wire poses a high risk of eavesdropping or interception on local network gateways.

Furthermore, using a static initialization vector (IV) allows pattern recognition and rainbow table attacks across repeated uploads.

---

## Decision

Enforce client-side **AES-128-CBC** encryption using dynamic session IVs generated per-upload:

1. Prior to transmission, the Android app generates a cryptographically secure 16-byte random IV using `java.security.SecureRandom`.
2. The payload JSON is encrypted using the shared symmetric key (`MPESA_CRYPT_KEY`).
3. The 16-byte dynamic IV is prefixed directly to the upload binary stream (`loot_[uuid].enc`).
4. The server-side WebApp extracts the first 16 bytes from the stream, assigns it as the decryption IV, and passes the remainder to `openssl_decrypt()`.

---

## Consequences

### Positive
- **Defense in Depth**: Protects payload privacy even when network connections traverse unencrypted local Wi-Fi or cleartext development bridges.
- **Replay & Rainbow Immunity**: Every upload stream uses a distinct IV, producing completely unique ciphertext even for identical SMS contents.
- **Zero Heap Spikes**: The binary payload is piped via streaming byte arrays and temporary scratch files that are purged immediately following upload confirmation.

### Negative
- Requires synchronized configuration of symmetric keys (`MPESA_CRYPT_KEY`) between Android `buildConfigField` and server `.env`.
