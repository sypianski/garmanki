---
id: TASK-42
title: 'Zegarek: wydanie z nową ikoną (wariant B) i grafiką listingu Connect IQ'
status: To Do
assignee: []
created_date: '2026-10-29 16:57'
labels: []
dependencies:
  - TASK-39
  - TASK-40
priority: low
ordinal: 51000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Ikona wariant B (tarcza pytanie/odpowiedź) jest w repo (commit 4e6fb9a: assets/, watch/resources/drawables/launcher_icon.png 70x70) i w companionie 1.0.6 (Play, production 20% od 2026-10-29). Do sklepu Connect IQ trafi dopiero z kolejnym wydaniem zegarka, razem z gotową pracą z TASK-39 i TASK-40 (nie wydawać zegarka z niedokończonymi zmianami z drzewa roboczego). Listing CIQ ma osobne grafiki (ikona/okładka 500x500/zrzuty ekranu), które dziś pokazują starą ikonę — user zdecydował 2026-10-29, że mają zostać podmienione przy tym wydaniu. Pliki źródłowe: assets/icon.svg, icon-fg.svg, icon-512.png.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Przed wydaniem sprawdzono w panelu Connect IQ, które grafiki listingu pokazują starą ikonę (ikona, okładka, zrzuty)
- [ ] #2 Grafiki listingu podmienione na wariant B w wymaganych wymiarach
- [ ] #3 Wersja zegarka podbita (AppVersion w watch/resources/strings/strings.xml) jako krok 1, build monkeyc zielony na fr965 i vivoactive4
- [ ] #4 Wydanie zegarka wysłane przez /garminiq (/update) po osobnej zgodzie usera; wersja w sklepie >= AppVersion
<!-- AC:END -->
