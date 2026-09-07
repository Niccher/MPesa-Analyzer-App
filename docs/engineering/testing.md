# Testing Guide — Mpesa Analyzer App

Information on executing unit and UI instrumentation tests.

---

## 1. Running Unit Tests

Unit tests run locally on the JVM without requiring an attached Android device:

```bash
./gradlew testDebugUnitTest
```

Unit tests validate:
- SMS regex parsing logic in `MpesaParser` across different Kenyan carrier notification formats.
- Date and currency formatting helper functions.
- Viewmodel state mapping and LiveData emissions.

---

## 2. Running Instrumentation Tests

Instrumentation tests run on an active emulator or connected hardware device:

```bash
./gradlew connectedDebugAndroidTest
```
