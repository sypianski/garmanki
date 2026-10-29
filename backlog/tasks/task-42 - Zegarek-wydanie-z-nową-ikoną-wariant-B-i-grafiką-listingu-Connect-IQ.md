---
id: TASK-42
title: 'Zegarek: wydanie z nową ikoną (wariant B) i grafiką listingu Connect IQ'
status: In Progress
assignee: []
created_date: '2026-10-29 16:57'
updated_date: '2026-10-29 18:56'
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
- [x] #1 Przed wydaniem sprawdzono w panelu Connect IQ, które grafiki listingu pokazują starą ikonę (ikona, okładka, zrzuty)
- [x] #2 Grafiki listingu podmienione na wariant B w wymaganych wymiarach
- [x] #3 Wersja zegarka podbita (AppVersion w watch/resources/strings/strings.xml) jako krok 1, build monkeyc zielony na fr965 i vivoactive4
- [x] #4 Wydanie zegarka wysłane przez /garminiq (/update) po osobnej zgodzie usera; wersja w sklepie >= AppVersion
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-29: listing CIQ (publiczne API + strona): ikona sklepu 500x500 = stary styl (napis GARMANKI / ANKI ON GARMIN); 2 zrzuty 601x800 to zdjęcia zegarka z ekranami apki, ikony na nich nie ma — zostają. Do podmiany tylko ikona sklepu: assets/icon-512.png przeskalowane do 500x500 (scratchpad listing/cover-500.png). Wersja 1.3.2: AppVersion podbite, commit 12332a4, eksport .iq na Macu 77/77 → ~/apps/garmanki/garmanki-1.3.2.iq. Uwaga: monkeyc -r (release) wywala kompilator ('Generating code for unsupported operation null') także na 1.3.1 — eksport bez -r, jak w skillu.

2026-10-29: wysłane przez /update w retumilo (sesja zalogowana; tymczasowy profil 9227 trafił na Cloudflare Turnstile przy SSO). Krok 1: garmanki-1.3.2.iq + wersja 1.3.2; krok 2: What's New (5 punktów) + Cover Image 500x500 = assets/icon-512.png; Submit → redirect Manage Your App. /update pokazuje 'Latest app version: 1.3.2'; publiczne API: iconFileId 53348f20-… (było 8e2689d8-…), latestExternalVersion nadal 1.3.1 do zatwierdzenia Garmina. Lekcja: w retumilo karty w tle (Target.createTarget background) + reaper-pauza na kreator; strona Garmina w karcie hidden nie renderuje — Emulation.setFocusEmulationEnabled + Page.setWebLifecycleState active.
<!-- SECTION:NOTES:END -->
