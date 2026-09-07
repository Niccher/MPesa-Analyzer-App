# Contributing Guidelines — Mpesa Analyzer App

Guidelines for submitting pull requests and code modifications to the Android application.

---

## 1. Branching Strategy

- Develop all features and bug fixes on dedicated branches (`feature/chart-legend-filter`, `fix/sms-date-parsing`).
- Open pull requests against `main`.

---

## 2. Pull Request Checklist

1. Verify the project builds cleanly:
   ```bash
   ./gradlew assembleDebug
   ```
2. Ensure unit tests pass:
   ```bash
   ./gradlew test
   ```
3. Run the documentation quality linter:
   ```bash
   python3 /home/niccher/Downloads/readme-docs-skill/readme-docs-skill/scripts/lint-docs.py .
   ```
4. Verify no API keys, private tokens, or credentials are hardcoded.
