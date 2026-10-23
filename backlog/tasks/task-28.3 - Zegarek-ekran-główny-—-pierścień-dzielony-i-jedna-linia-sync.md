---
id: TASK-28.3
title: 'Zegarek: ekran główny — pierścień dzielony i jedna linia sync'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-23 18:00'
updated_date: '2026-10-23 18:05'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 31000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Pierścień dnia w odcinkach: zrobione, nowe, powtórki (przeskalowane do remaining, gdy nNew+nRev != remaining). Linia sync jako 'Sync HH:MM' (pełna data tylko gdy nie dziś), wezwanie na h*70/100. Decyzja: data znika, gdy sync był dziś.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Pierścień pokazuje trzy odcinki i nie kłamie, gdy liczniki się rozjeżdżają
- [ ] #2 Linia sync: sam czas gdy dziś, data i czas gdy wcześniej
- [ ] #3 Build monkeyc przechodzi dla fr965, fenix7 i venu3
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Theme.ringSplit: trzy odcinki (zrobione/nowe/powtórki), nowe i powtórki skalowane do remaining, gdy nNew+nRev się nie zgadza. HomeView: jedna linia 'Synced HH:MM' (z datą gdy nie dziś, porównanie dnia/miesiąca/roku), CTA na h*70/100; HomeLastSync zastąpione HomeSynced. Build OK fr965/fenix7/venu3. Niesprawdzone na zegarku.
<!-- SECTION:NOTES:END -->
