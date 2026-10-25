---
id: TASK-33.1
title: 'Zegarek: nowy tekst HomeNoData (EN + PL) z wymaganiami'
status: To Do
assignee: []
created_date: '2026-10-25 10:53'
labels:
  - zegarek
dependencies: []
parent_task_id: TASK-33
priority: medium
ordinal: 41000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Zmiana wyłącznie w strings.xml (watch/resources/strings i resources-pol/strings), bez ruszania HomeView.mc — plik edytuje równolegle TASK-28.3. Tekst ma mówić: brak talii, potrzebny telefon z Androidem, companion Garmanki i AnkiDroid, potem wybór talii i sync. Pole tekstu ma fonty zapasowe SMALL→TINY→XTINY, ale nowy tekst jest ~2,5× dłuższy niż dziś i na małych ekranach może się nie zmieścić.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Nowy tekst EN i PL (z diakrytykami) wspomina o Androidzie, companionie i AnkiDroid
- [ ] #2 Tekst mieści się bez ucięcia na fr255/fr255s (260 px), fr965 i fenix7 (symulator lub zrzut; jeśli symulator niedostępny — opisane ograniczenie i skrócony tekst z uzasadnieniem)
- [ ] #3 Build monkeyc przechodzi dla fr965, fr255 i venu3
- [ ] #4 Wersja AppVersion podbita przy wydaniu wg Version Bump
<!-- AC:END -->
