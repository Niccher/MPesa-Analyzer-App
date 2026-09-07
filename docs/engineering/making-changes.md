# Making Changes — Engineering Guide

Guide for adding features, modifying network DTOs, and writing tests in the Android app.

---

## 1. Task Navigator

| If you want to… | Files to modify |
|-----------------|-----------------|
| **Add a new API endpoint call** | 1. Define method in appropriate interface in `api/`<br/>2. Create response DTO data class in `models/`<br/>3. Wire network call through `helpers/ServiceGenerator.kt` |
| **Add or update an analytics chart** | 1. Update chart binding in `fragments/Graph/GraphFragment.kt`<br/>2. Adjust viewmodel data mapping in `viewmodels/` |
| **Modify SMS regex parsing patterns** | Edit `helpers/MpesaParser.kt` and add unit test cases in `app/src/test/` |
| **Change scheduled sync interval or constraints** | Edit WorkRequest constraints in `workers/MpesaSyncWorker.kt` |

---

## 2. Definition of Done (DoD)

Before opening a pull request:

- [ ] Project compiles with `./gradlew assembleDebug` with 0 errors.
- [ ] Unit tests pass via `./gradlew test`.
- [ ] New network models match backend `/api/v1/*` contracts.
- [ ] Sensitive encryption keys or tokens are not committed to Git.
- [ ] Documentation updated in `docs/services/android.md`.
