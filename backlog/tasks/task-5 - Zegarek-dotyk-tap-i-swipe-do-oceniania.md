---
id: TASK-5
title: 'Zegarek: dotyk - tap i swipe do oceniania'
status: Done
assignee: []
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:12'
labels:
  - watch
  - ux
dependencies:
  - TASK-4
ordinal: 5000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Tap: przed odslonieciem reveal, po odslonieciu ocena wg tabeli (default Good, D15). Swipe (D16): prawo=Good, lewo=Again, gora=Easy, dol=Hard; przed odslonieciem reveal; kierunki niezmapowane przepuszczaja gest systemowy.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Pelna sesja powtorek mozliwa samym dotykiem
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
onTap deleguje do _key(tap); onSwipe mapuje SWIPE_* na swipeR/L/U/D. UWAGA do testu na sprzecie: przechwycenie SWIPE_RIGHT (systemowy back) - zwracamy true, wymaga potwierdzenia na fizycznym fr965. Build: BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
onTap/onSwipe w ReviewDelegate na tabeli ActionMap; SWIPE_RIGHT do weryfikacji na sprzecie
<!-- SECTION:FINAL_SUMMARY:END -->
