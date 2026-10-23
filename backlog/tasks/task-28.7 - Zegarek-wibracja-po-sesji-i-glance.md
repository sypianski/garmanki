---
id: TASK-28.7
title: 'Zegarek: wibracja po sesji i glance'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-23 18:00'
updated_date: '2026-10-23 18:15'
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

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Wibracja: ReviewView._buzz() — jeden impuls 250 ms przy ocenie ostatniej karty (finished=true), z osłoną 'Attention has :vibrate' i respektowaniem DeviceSettings.vibrateOn. SDK nie wymaga uprawnienia w manifeście (doc Toybox.Attention). Build fr965/fenix7/venu3 OK; nie sprawdzone na zegarku. Glance: NIE wdrożony — AppBase.getGlanceView() jest od API 4.0.0, a manifest ma minApiLevel 3.4.0; wymaga decyzji usera (podnieść minApiLevel / ograniczyć do wspieranych urządzeń / zrezygnować).
<!-- SECTION:NOTES:END -->
