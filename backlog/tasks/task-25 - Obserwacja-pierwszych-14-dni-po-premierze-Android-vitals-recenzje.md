---
id: TASK-25
title: 'Obserwacja pierwszych 14 dni po premierze (Android vitals, recenzje)'
status: To Do
assignee: []
created_date: '2026-10-12 11:05'
labels:
  - premiera
dependencies:
  - TASK-20
priority: medium
ordinal: 25000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Pierwsze tygodnie publicznej dostępności to okres największego ryzyka: apka współpracuje z AnkiDroid i zegarkami Garmin różnych modeli (TASK-16), a dotąd testowało ją kilka osób. Trzeba regularnie sprawdzać w Play Console Android vitals (crash rate, ANR) oraz recenzje i zgłoszenia od użytkowników, i szybko reagować hotfixem. Hotfix = nowy build, więc zgodnie z regułą wersjonowania pierwszym krokiem jest bump versionCode/versionName.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Przeglądy Android vitals i recenzji w Play Console: dzień 1–2, ok. dnia 7 i dnia 14 po publikacji; wyniki zapisane w notatkach zadania
- [ ] #2 Gdy wskaźniki crash/ANR przekraczają progi bad behavior z Android vitals albo pojawia się powtarzalny błąd — decyzja o hotfixie zapisana w notatkach
- [ ] #3 Powtarzalne zgłoszenia użytkowników założone jako osobne taski w backlogu
<!-- AC:END -->
