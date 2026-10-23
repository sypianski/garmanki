---
id: TASK-28.3
title: 'Zegarek: ekran główny — pierścień dzielony i jedna linia sync'
status: To Do
assignee: []
created_date: '2026-10-23 18:00'
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
