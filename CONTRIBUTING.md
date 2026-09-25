# Contributing to Smart Financial SMS (Android)

Thank you for contributing to the **Smart Financial SMS** Android companion app! We welcome enhancements to our on-device SMS parsing, Jetpack Compose UI, BiometricPrompt security, and sync architecture.

---

## Code of Conduct

All contributors are expected to uphold our [Code of Conduct](CODE_OF_CONDUCT.md). Please read it before participating.

---

## Getting Started

1. **Prerequisites**:
   - **Android Studio**: Hedgehog (2023.1.1) or newer
   - **JDK**: 17+
   - Physical Android device (Android 10.0+ / API 29+) or Android Emulator
   - Running instance of [Smart-Finance-Platform](https://github.com/Niccher/Smart-Finance-Platform)
2. **Clone & Open**:
   ```bash
   git clone https://github.com/<your-username>/Smart-Finance-Android.git
   cd Smart-Finance-Android
   ```
   Open the project directory in Android Studio and let Gradle sync.
3. **Build & Install**:
   ```bash
   ./gradlew installDebug
   ```

---

## Zero-Trust Privacy Guidelines

> [!CAUTION]
> **No Personal SMS in PRs**: Never commit log dumps or test cases containing real SMS messages, actual bank codes, personal phone numbers, or account balances.

Use synthetic test SMS strings with fake names (`JOHN DOE`, `WANJIKU K`), dummy transaction codes (`QAB1234567`), and synthetic timestamps.

---

## Testing & Verification

Before submitting a Pull Request, verify that all local checks pass:

1. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```
2. **Run Android Lint**:
   ```bash
   ./gradlew lintDebug
   ```
3. **Run Documentation Quality Linter**:
   ```bash
   python3 scripts/lint-docs.py
   ```

---

## Pull Request Guidelines

1. Create a feature branch: `git checkout -b feat/add-biometric-timeout`
2. Follow standard Kotlin style guides and clean architecture patterns (MVVM / Repository).
3. Commit with [Conventional Commits](https://www.conventionalcommits.org/):
   - `feat(ui): add transaction breakdown carousel in home screen`
   - `fix(sync): handle network timeout retry with exponential backoff`
4. Open a Pull Request targeting `main`. Complete the PR checklist.
