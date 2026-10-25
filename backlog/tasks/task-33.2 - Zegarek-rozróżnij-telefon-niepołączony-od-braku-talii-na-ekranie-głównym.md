---
id: TASK-33.2
title: 'Zegarek: rozróżnij telefon niepołączony od braku talii na ekranie głównym'
status: To Do
assignee: []
created_date: '2026-10-25 10:53'
labels:
  - zegarek
dependencies:
  - TASK-28.3
  - TASK-33.1
parent_task_id: TASK-33
priority: low
ordinal: 42000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Krok 2: gdy phoneConnected=false pokazać „Phone not connected” (PL: telefon niepołączony), gdy telefon połączony, a talii brak — instrukcję z TASK-33.1. Wymaga edycji HomeView.mc, więc czeka na zamknięcie TASK-28.3 (ten sam plik, równoległa sesja).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Stan telefon niepołączony i stan brak talii pokazują różne teksty (EN i PL)
- [ ] #2 Zmiana nie psuje pozostałych stanów ekranu głównego (talie, wszystko zrobione na dziś)
- [ ] #3 Build monkeyc przechodzi dla fr965, fr255 i venu3
<!-- AC:END -->
