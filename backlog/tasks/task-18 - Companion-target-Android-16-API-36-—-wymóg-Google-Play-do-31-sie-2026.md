---
id: TASK-18
title: 'Companion: target Android 16 (API 36) — wymóg Google Play do 31 sie 2026'
status: Done
assignee:
  - '@claude'
created_date: '2026-07-30 08:17'
updated_date: '2026-10-12 14:02'
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
- [x] #1 targetSdk i compileSdk = 36 w android/app/build.gradle.kts
- [x] #2 AGP podbite do wersji oficjalnie wspierającej compileSdk 36; build bez warningów o niekompatybilności
- [x] #3 assembleRelease, lint i testDebugUnitTest przechodzą
- [x] #4 versionCode/versionName podbite (bump wersji jako pierwszy krok publikacji)
- [x] #5 Nowa wersja wgrana do Google Play (produkcja) — po potwierdzeniu przez usera
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Podbij AGP w gradle/libs.versions.toml do wersji stabilnej ze wsparciem compileSdk 36 (sprawdź dostępne w google maven; Gradle wrapper 8.14.3 zostaje). 2. compileSdk=36, targetSdk=36 w app/build.gradle.kts. 3. versionCode=14, versionName=1.0.4. 4. ./gradlew assembleRelease lint testDebugUnitTest. 5. Przegląd behavior changes API 36 pod kątem companiona (Compose, BLE/CIQ SDK, foreground/JobScheduler). 6. Build release AAB/APK; upload do Play dopiero po potwierdzeniu usera (Publish Gate).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
AGP 8.7.3 → 8.13.2 (ostatnia stabilna linia 8.x ze wsparciem compileSdk 36; Gradle wrapper 8.14.3 bez zmian). compileSdk/targetSdk 35→36, versionCode 14, versionName 1.0.4. assembleRelease + lint + testDebugUnitTest przechodzą; bundleRelease zbudowany. aapt2 potwierdza targetSdkVersion 36 w APK. Manifest bez screenOrientation i bez opt-outu edge-to-edge — behavior changes API 36 nie dotykają companiona. Czeka: potwierdzenie usera na upload AAB do Play (produkcja) + commit.

Upload przez Play Developer API (service account, skill play-testing): AAB versionCode=14 wgrany, tory internal + alpha = completed (1.0.4), edit scommitowany, potwierdzone tracks().list. Tor production PUSTY (apka nigdy nie miała release produkcyjnego; live wcześniej wisiało tylko vc10/1.0.0 na internal+alpha — vc11–13 nigdy nie poszły live). Update toru production przez API zwraca 400 "Precondition check failed" nawet dla status=draft — najpewniej konto osobiste bez odblokowanego production access (wymóg closed testu 12 testerów/14 dni) i/lub brak konfiguracji country availability dla production. Pierwszy release produkcyjny trzeba przeklikać w Play Console UI; sprawdzić też Publishing overview czy zmiany nie czekają w "changes ready to publish".

2026-10-12 15:59 (weryfikacja po premierze): #1 — android/app/build.gradle.kts: compileSdk=36, targetSdk=36; Play Console (Preview and confirm) pokazał Target SDK 36 dla bundla vc14. #4 — versionCode 14 / versionName 1.0.4 (build.gradle.kts; Play: 14 (1.0.4)). #5 — release produkcyjny zatwierdzony i LIVE w Google Play (TASK-20): https://play.google.com/store/apps/details?id=dev.sypianski.garmanki. #2 (AGP 8.13.2 potwierdzone w libs.versions.toml; brak ostrzeżeń w buildzie) i #3 (assembleRelease, lint, testDebugUnitTest) — ponowne uruchomienie w tle, wynik w kolejnym wpisie.

2026-10-12 16:02: ponowne uruchomienie ./gradlew assembleRelease lint testDebugUnitTest (android/, AGP 8.13.2, Gradle wrapper 8.14.3) -> BUILD SUCCESSFUL in 2m 59s, 85 tasks, brak ostrzeżeń o niekompatybilności compileSdk/AGP w logu (scratchpad/gradle-verify.log). Wszystkie 5 kryteriów spełnionych.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Companion celuje w Android 16 (API 36): AGP 8.13.2, compileSdk/targetSdk 36, versionCode 14 (1.0.4); build, lint i testy przechodzą; wersja zatwierdzona i live w Google Play.
<!-- SECTION:FINAL_SUMMARY:END -->
