# Developer Troubleshooting — Mpesa Analyzer App

Common build, Gradle, and IDE issues for Android developers.

---

## 1. Gradle Sync Failed: Unsupported Java Version

**Symptom**: `compileJava task failed with incompatible Java version`.

**Remedy**:
Ensure Android Studio is using **JDK 17**:
1. Open **Settings $\to$ Build, Execution, Deployment $\to$ Build Tools $\to$ Gradle**.
2. Set **Gradle JDK** to `jbr-17` or installed JDK 17+.

---

## 2. Unaccepted Android SDK Licenses

**Symptom**: `Failed to install the following Android SDK packages as some licences have not been accepted`.

**Remedy**:
Accept licenses via command line:
```bash
yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses
```

---

## 3. ADB Device Unauthorized or Offline

**Symptom**: `adb devices` shows device as `unauthorized`.

**Remedy**:
1. Reconnect USB cable.
2. Accept the "Allow USB Debugging" RSA prompt on the Android device screen.
3. Restart ADB server:
   ```bash
   adb kill-server && adb start-server
   ```
