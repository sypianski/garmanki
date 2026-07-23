---
id: TASK-6
title: 'Zegarek: menu wiecej bez akcji zdublowanych z przyciskow'
status: Done
assignee: []
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:12'
labels:
  - watch
  - ux
dependencies:
  - TASK-4
  - TASK-5
ordinal: 6000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
openMenu buduje pozycje ocen dynamicznie: tylko oceny nieosiagalne przyciskiem/gestem wg ActionMap.isMapped; akcje karty (suspend/bury/flag/delete, id 11-14) zawsze obecne. Przy domyslnej tabeli menu zawiera wylacznie akcje karty.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Zadna pozycja menu nie dubluje akcji dostepnej bezposrednio
- [x] #2 Suspend/bury/flag/delete zawsze obecne
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
ActionMap.isMapped(ease) iteruje po scalonej tabeli. _gradeLabel z interwalami zachowany dla pozycji ktore zostaja. Build fr965: BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Dynamiczne menu ocen na isMapped; akcje karty bezwarunkowe
<!-- SECTION:FINAL_SUMMARY:END -->
