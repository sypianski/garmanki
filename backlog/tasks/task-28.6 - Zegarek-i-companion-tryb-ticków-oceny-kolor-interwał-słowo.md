---
id: TASK-28.6
title: 'Zegarek i companion: tryb ticków oceny (kolor / interwał / słowo)'
status: To Do
assignee: []
created_date: '2026-10-23 18:00'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 34000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Tick z etykietą interwału (nt1..nt4) jako domyślny. Ustawienie trybu w companionie, nowy klucz w SCHEMA.md §8, zapis na zegarku obok actionMap. Zapisać rekord decyzji (odstępstwo od TASK-2) przez /path-chosen. Sprawdzić, czy potrzebne podbicie pv.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Ustawienie w companionie zmienia tryb ticków na zegarku
- [ ] #2 SCHEMA.md §8 opisuje klucz, rekord decyzji PC zapisany
- [ ] #3 Build monkeyc (fr965, fenix7, venu3) i gradle assembleRelease testDebugUnitTest przechodzą
<!-- AC:END -->
