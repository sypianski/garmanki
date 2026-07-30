---
id: TASK-18
title: 'Companion: target Android 16 (API 36) — wymóg Google Play do 31 sie 2026'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-07-30 08:17'
updated_date: '2026-07-30 08:25'
labels: []
dependencies: []
references:
  - 'https://developer.android.com/about/versions/16/behavior-changes-16'
ordinal: 18000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Google Play Console zgłasza: "App must target Android 16 (API level 36) or higher". Od 31 sierpnia 2026 bez targetSdk 36 nie da się publikować aktualizacji. Obecnie android/app/build.gradle.kts ma compileSdk=35, targetSdk=35, a AGP 8.7.3 (oficjalne wsparcie API 36 od AGP ~8.9.x). Trzeba podbić SDK + AGP, zweryfikować behavior changes Androida 16 dla companiona (Compose M3, single activity), zbudować i wypuścić nową wersję do produkcji w Play.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 targetSdk i compileSdk = 36 w android/app/build.gradle.kts
- [ ] #2 AGP podbite do wersji oficjalnie wspierającej compileSdk 36; build bez warningów o niekompatybilności
- [ ] #3 assembleRelease, lint i testDebugUnitTest przechodzą
- [ ] #4 versionCode/versionName podbite (bump wersji jako pierwszy krok publikacji)
- [ ] #5 Nowa wersja wgrana do Google Play (produkcja) — po potwierdzeniu przez usera
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Podbij AGP w gradle/libs.versions.toml do wersji stabilnej ze wsparciem compileSdk 36 (sprawdź dostępne w google maven; Gradle wrapper 8.14.3 zostaje). 2. compileSdk=36, targetSdk=36 w app/build.gradle.kts. 3. versionCode=14, versionName=1.0.4. 4. ./gradlew assembleRelease lint testDebugUnitTest. 5. Przegląd behavior changes API 36 pod kątem companiona (Compose, BLE/CIQ SDK, foreground/JobScheduler). 6. Build release AAB/APK; upload do Play dopiero po potwierdzeniu usera (Publish Gate).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
AGP 8.7.3 → 8.13.2 (ostatnia stabilna linia 8.x ze wsparciem compileSdk 36; Gradle wrapper 8.14.3 bez zmian). compileSdk/targetSdk 35→36, versionCode 14, versionName 1.0.4. assembleRelease + lint + testDebugUnitTest przechodzą; bundleRelease zbudowany. aapt2 potwierdza targetSdkVersion 36 w APK. Manifest bez screenOrientation i bez opt-outu edge-to-edge — behavior changes API 36 nie dotykają companiona. Czeka: potwierdzenie usera na upload AAB do Play (produkcja) + commit.
<!-- SECTION:NOTES:END -->
