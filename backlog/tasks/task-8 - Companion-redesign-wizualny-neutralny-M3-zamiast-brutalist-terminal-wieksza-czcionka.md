---
id: TASK-8
title: >-
  Companion: redesign wizualny - neutralny M3 zamiast brutalist-terminal,
  wieksza czcionka
status: Done
assignee:
  - '@claude'
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:23'
labels:
  - android
  - ux
dependencies:
  - TASK-7
ordinal: 8000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Odejscie od Space Mono 11-13sp / cyan-on-black / komponentow terminalowych (Terminal.kt) na neutralny Material 3: standardowa typografia M3 (body >=14sp), dynamic color z fallbackiem, light+dark. Space Mono najwyzej dla danych technicznych (log sync). Redesign, nie przebudowa logiki; uwzglednic sekcje ustawien z task-7. Decyzja D17: sciaga za mala z dyktowanego feedbacku = czcionka (przyjete).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Domyslne rozmiary tekstu M3, czytelne bez mruzenia oczu
- [x] #2 Aplikacja poprawna w light i dark
- [x] #3 Brak elementow terminalowych poza ewentualnym logiem
- [x] #4 gradlew lint testDebugUnitTest przechodzi
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Redesign M3 gotowy: Theme.kt = dynamic color (S+) z neutralnym slate fallbackiem light/dark, domyslna typografia M3 (body 14sp+), Space Mono tylko w LogLine (push/sync log); Terminal.kt USUNIETY (SectionRule/HorizontalRule/StatusDot/KeyValue/TerminalCheckRow/TerminalButton/StreakBar/MeterBar zastapione Card/HorizontalDivider/Checkbox/Switch/Button/OutlinedButton/LinearProgressIndicator); MainScreen.kt przelozony na SectionCard + ExposedDropdownMenuBox dla ocen, walidacja Again+Good bez zmian; themes.xml light+night (Theme.Material.Light/dark), splash i logo w @color z wariantem night. gradlew lint testDebugUnitTest: BUILD SUCCESSFUL.

Weryfikacja supervisora: Terminal.kt nie istnieje (w ui/ tylko MainScreen.kt i Theme.kt), dynamic color w Theme.kt, values-night/themes.xml obecny, jedyny StatusDot to nowy lokalny composable M3 w MainScreen. gradlew lint testDebugUnitTest zielone (raport agenta). Obserwacja poza zakresem: MainActivity bez enableEdgeToEdge - na API 35+ statusBarColor z themes.xml ignorowany; do rozwazenia jako osobny task.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Companion na neutralnym Material 3: dynamic color + slate fallback, typografia domyslna M3, light+dark za systemem, Terminal.kt usuniety, logika nietknieta
<!-- SECTION:FINAL_SUMMARY:END -->
