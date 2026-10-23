---
id: TASK-28.5
title: 'Zegarek: podsumowanie — słupki ocen'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-23 18:00'
updated_date: '2026-10-23 18:06'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 33000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Cztery liczniki ocen w ReviewView.grade przekazane do SummaryView (bez zapisu w Storage), słupki w kolorach ocen pod 'cards · N min', linia kolejki niżej.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Słupki odzwierciedlają rzeczywiste oceny z sesji, także po przerwaniu
- [ ] #2 Układ mieści się w okręgu na fr965 i fenix7
- [ ] #3 Build monkeyc przechodzi dla fr965, fenix7 i venu3
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
ReviewView liczy oceny w _counts (bez zapisu w Storage) i przekazuje do SummaryView; słupki Again/Hard/Good/Easy w stałej kolejności, 0 = cienka kreska FAINT, linia kolejki na +54 px. Build OK. Niesprawdzone na zegarku (fenix7: hero vs 'kart · min' wg założenia ×1,2).
<!-- SECTION:NOTES:END -->
