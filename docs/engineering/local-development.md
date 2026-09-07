# Local Development Guide — Mpesa Analyzer App

Instructions for configuring Android Studio and building the client via command line.

---

## 1. Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 17** configured as the Gradle JVM
- Android SDK platforms 29 through 35 installed via SDK Manager

---

## 2. Command-Line Build & Test

Ensure `local.properties` exists in the repository root containing your Android SDK path:
```ini
sdk.dir=/path/to/Android/Sdk
```

### Common Gradle Tasks
```bash
# Compile debug APK
./gradlew assembleDebug

# Install debug APK on connected device
./gradlew installDebug

# Run unit tests
./gradlew test

# Clean build cache
./gradlew clean
```
