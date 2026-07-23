---
id: TASK-4
title: >-
  Zegarek: konfigurowalne mapowanie przyciskow (domyslnie DOWN=Good,
  START=Again, UP=Easy)
status: Done
assignee: []
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:12'
labels:
  - watch
  - core
dependencies: []
ordinal: 4000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Warstwa posrednia ActionMap.mc: tabela zdarzenie->ease ladowana z Storage (klucz actionMap, nakladana na defaulty), klucze up/down/start/tap/swipeR/swipeL/swipeU/swipeD, wartosci ease 1-4 lub null. Decyzje D13/D14 w DECYZJE.md. Ticki na bezelu generowane z tabeli; menu wiecej przeniesione na onMenu (przytrzymanie UP).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Zmiana tabeli w Storage zmienia dzialanie przyciskow i ticki bez rekompilacji
- [x] #2 Defaulty: down=3 good, start=1 again, up=4 easy
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
ActionMap.easeFor(event), reload(), isMapped(ease), stale AGAIN/HARD/GOOD/EASY. Theme: kolory HARD/EASY, easeColor(ease). ReviewDelegate: wspolny _key(event), onMenu -> openMenu. Build fr965: BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
ActionMap.mc jako fundament konfigurowalnego sterowania; ReviewDelegate i ticki przepisane na tabele
<!-- SECTION:FINAL_SUMMARY:END -->
