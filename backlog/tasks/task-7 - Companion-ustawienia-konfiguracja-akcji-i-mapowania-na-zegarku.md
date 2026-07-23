---
id: TASK-7
title: 'Companion: ustawienia - konfiguracja akcji i mapowania na zegarku'
status: Done
assignee:
  - '@claude'
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:17'
labels:
  - android
  - watch
  - schema
dependencies:
  - TASK-4
  - TASK-5
  - TASK-6
ordinal: 7000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Companion staje sie centrum konfiguracji zegarka: sekcja ustawien (mapowanie 8 zdarzen, przelaczniki akcji karty, reset przewodnika, reset do domyslnych), rozszerzenie Settings/SettingsStore i StatePayload; NAJPIERW nowa sekcja w SCHEMA.md (konfiguracja UI zegarka: actionMap, cardActions, guideReset). Zegarek: PhoneLink zapisuje actionMap do Storage + ActionMap.reload(), filtruje akcje karty w menu, guideReset kasuje onboardingSeen. Walidacja: mapowanie musi zawierac Again i Good.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 SCHEMA.md opisuje format konfiguracji przed zmianami w kodzie
- [x] #2 Zmiana w companionie po sync zmienia zachowanie zegarka
- [x] #3 StatePayloadTest pokrywa nowe pola
- [x] #4 gradlew lint testDebugUnitTest i monkeyc fr965 przechodza
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
W realizacji przez subagenta (fork gar07).

SCHEMA.md par.8: cfg={am,ca,gr} w chunku seq:1, kazda czesc opcjonalna, sticky. Walidacja wymaga OBU ocen Again i Good. Auto-push przy zapisie mapowania i resecie przewodnika; checkboxy akcji karty jada z nastepnym pushem. Weryfikacja supervisora: par.8 w SCHEMA.md obecny, cardActions/guideReset w SettingsStore, garmanki.prg zbudowany 10:13. monkeyc fr965 i gradlew lint testDebugUnitTest zielone (raport agenta).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Konfiguracja UI zegarka w SCHEMA.md par.8 + pelny lancuch: SettingsStore -> StatePayload cfg -> PhoneLink -> ActionMap.reload/cardActions/guideReset; sekcja WATCH CONTROLS w MainScreen z walidacja
<!-- SECTION:FINAL_SUMMARY:END -->
