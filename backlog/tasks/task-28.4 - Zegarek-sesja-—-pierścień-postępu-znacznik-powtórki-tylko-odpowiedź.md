---
id: TASK-28.4
title: 'Zegarek: sesja — pierścień postępu, znacznik powtórki, tylko odpowiedź'
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

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
ReviewView: Theme.ring zamiast 16 kropek (usunięty kod kropek), strzałka powtórki rysowana (łuk 270° + grot) obok licznika gdy _curStep>0 na ekranie pytania, po odsłonięciu tylko odpowiedź (pole od h*20/100, wys. 58%/40%, pole dodatkowe od 64%). Build OK fr965/fenix7/venu3. Zostaje do sprawdzenia na zegarku: długie odpowiedzi vs etykiety interwałów (TASK-28.6) i wygląd strzałki.
<!-- SECTION:NOTES:END -->
