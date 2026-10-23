---
id: TASK-28.7
title: 'Zegarek: wibracja po sesji i glance'
status: To Do
assignee: []
created_date: '2026-10-23 18:00'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 35000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Wibracja po ostatniej karcie (sprawdzić w dokumentacji Toybox.Attention, czy potrzebne uprawnienie, i ustawienia wibracji urządzenia). Glance z liczbą do zrobienia i niewysłanymi: najpierw zbadać w SDK 9.2.0 minApiLevel 3.4.0 i budżet pamięci, glance czyta jedną liczbę ze Storage.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Wibracja po zakończeniu sesji, tylko na urządzeniach z Attention.vibrate
- [ ] #2 Glance zaimplementowany albo udokumentowane, dlaczego niemożliwy na minApiLevel 3.4.0
- [ ] #3 Build monkeyc przechodzi dla fr965, fenix7 i venu3
<!-- AC:END -->
