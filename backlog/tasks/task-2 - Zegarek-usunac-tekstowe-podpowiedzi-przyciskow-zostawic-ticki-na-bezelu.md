---
id: TASK-2
title: 'Zegarek: usunac tekstowe podpowiedzi przyciskow, zostawic ticki na bezelu'
status: Done
assignee: []
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:12'
labels:
  - watch
  - ux
dependencies: []
ordinal: 2000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Objasnienia co nacisnac na dole ekranu zbedne (przyciski samoobjasniajace) i wychodzily poza okrag. Usuniete colorLine z ReviewView i HomeView; ticki na bezelu zostaja jedynym stalym systemem podpowiedzi; onboarding (task-3) przejmuje objasnianie.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Widoki review i home bez tekstowych podpowiedzi przyciskow
- [x] #2 Ticki pozostaja, tresc karty ma wiecej miejsca
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Pytanie locY 16%/height 68% (bylo 18%/60%), odpowiedz height 42%. Usuniety martwy helper _easeWord, funkcja colorLine i stringi HintShow/WordMore/HomeHint*. Build fr965: BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
colorLine usuniete z obu widokow, TextArea powiekszone, martwe stringi wyczyszczone
<!-- SECTION:FINAL_SUMMARY:END -->
