# M-Pesa Analyzer App — Engineering Documentation

Welcome to the engineering documentation for **M-Pesa Analyzer App**, the Android mobile data capture client of the M-Pesa Analyzer ecosystem.

System-wide architecture and cross-repo sequence flows are anchored in the [ML Mpesa Analyzer Architecture Hub](https://github.com/Niccher/ML-Mpesa-Analyser/tree/main/docs/architecture).

---

## Documentation Index

| I want to… | Go here |
|------------|---------|
| **Install and test the debug APK** | [../README.md](../README.md) |
| **Understand Android architecture & MVVM components** | [services/android.md](services/android.md) |
| **Review Android Studio & Gradle dev environment** | [engineering/local-development.md](engineering/local-development.md) |
| **Inspect AES-128 streaming encryption & biometric lock** | [engineering/security.md](engineering/security.md) |
| **Add new UI screens, charts, or sync routines** | [engineering/making-changes.md](engineering/making-changes.md) |
| **Run unit & instrumentation tests** | [engineering/testing.md](engineering/testing.md) |
| **Troubleshoot emulator networking or Gradle issues** | [engineering/troubleshooting.md](engineering/troubleshooting.md) |
| **ADR 0001: Dynamic IV AES-128 payload encryption** | [adr/0001-client-side-aes128-dynamic-iv.md](adr/0001-client-side-aes128-dynamic-iv.md) |
| **Follow contribution & pull request guidelines** | [engineering/contributing.md](engineering/contributing.md) |

---

## Ecosystem Sibling Repositories

- **Web Dashboard & Gateway**: [Niccher/MPesa-Analyzer-WebApp](https://github.com/Niccher/MPesa-Analyzer-WebApp) (CodeIgniter 4, MySQL 8.4, Shield)
- **ML Intelligence Microservice**: [Niccher/ML-Mpesa-Analyser](https://github.com/Niccher/ML-Mpesa-Analyser) (Python FastAPI, llama.cpp, Qwen2.5)
