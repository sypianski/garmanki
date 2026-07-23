---
id: TASK-1
title: 'Zegarek: tekst nie wychodzi poza okragly ekran'
status: Done
assignee: []
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:12'
labels:
  - watch
  - ux
dependencies: []
ordinal: 1000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Napisy rysowane nisko/wysoko na kole (colorLine h*86/100, wersja h*93/100 w HomeView) obcinaly sie na krawedzi. Helper Theme.chordWidth(dc, yCenter, margin) liczy dostepna cieciwe; audyt wszystkich widokow.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Na fr965 zaden tekst nie jest przyciety przez krawedz ekranu
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zrobione przez subagenta razem z GAR-02. Theme.chordWidth dodany, wersja przeniesiona na h*89/100, linia info w HomeView redukowana do szerokosci cieciwy. Audyt pozostalych napisow: mieszcza sie z zapasem. Build monkeyc fr965: BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
chordWidth w Theme + korekty HomeView; pozostale napisy zweryfikowane geometrycznie
<!-- SECTION:FINAL_SUMMARY:END -->
