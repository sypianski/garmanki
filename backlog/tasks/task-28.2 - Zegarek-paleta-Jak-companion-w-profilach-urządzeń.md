---
id: TASK-28.2
title: 'Zegarek: paleta ''Jak companion'' w profilach urządzeń'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-23 18:00'
updated_date: '2026-10-23 18:04'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 30000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Pastelowe oceny i chłodne szarości z companiona (#EF9A9A #FFB74D #81C784 #64B5F6, akcent #A9C7E4) w DeviceProfile fivebtn-amoled i touch-amoled; osobne wartości z siatki 64 kolorów dla fivebtn-mip.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Paleta zmieniona w trzech DeviceProfile.mc
- [ ] #2 Kontrast tekstu MUTED/FAINT na czerni sprawdzony
- [ ] #3 Build monkeyc przechodzi dla fr965, fenix7 i venu3
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Paleta z companiona w trzech DeviceProfile (AMOLED i dotyk: pełne hex; MIP: siatka 64 kolorów, pastelowe Again/Good). Build OK fr965/fenix7/venu3. Kontrast w słońcu i wygląd na zegarku niesprawdzone.
<!-- SECTION:NOTES:END -->
