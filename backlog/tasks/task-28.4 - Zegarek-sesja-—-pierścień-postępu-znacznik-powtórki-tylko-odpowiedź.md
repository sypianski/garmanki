---
id: TASK-28.4
title: 'Zegarek: sesja — pierścień postępu, znacznik powtórki, tylko odpowiedź'
status: To Do
assignee: []
created_date: '2026-10-23 18:00'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 32000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
ReviewView: pierścień Theme.ring zamiast 16 kropek, strzałka powtórki obok licznika gdy _curStep>0 (ekran pytania), po odsłonięciu tylko odpowiedź (bez pytania i kreski, pole od h*20/100). Zadbać, by etykiety interwałów nie nachodziły na odpowiedź.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Ekran pytania i odpowiedzi działają według wariantów; usunięty kod kropek
- [ ] #2 Długa odpowiedź z polem dodatkowym nie nachodzi na etykiety ticków
- [ ] #3 Build monkeyc przechodzi dla fr965, fenix7 i venu3
<!-- AC:END -->
