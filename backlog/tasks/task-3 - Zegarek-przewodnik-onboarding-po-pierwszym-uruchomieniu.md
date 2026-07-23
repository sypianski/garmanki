---
id: TASK-3
title: 'Zegarek: przewodnik (onboarding) po pierwszym uruchomieniu'
status: Done
assignee: []
created_date: '2026-07-23 08:11'
updated_date: '2026-07-23 08:12'
labels:
  - watch
  - ux
dependencies: []
ordinal: 3000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
4 slajdy ink-and-paper po pierwszym starcie: reveal, oceny na przyciskach (generowane z ActionMap), gesty, menu wiecej + wyjscie. Flaga onboardingSeen w Storage; ponowne obejrzenie przez pozycje Guide w DeckMenu.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Swieza instalacja pokazuje przewodnik, kolejne starty nie
- [x] #2 Przewodnik da sie wywolac ponownie z menu
- [x] #3 Slajdy pokazuja aktualne mapowanie z ActionMap, nie hardcode
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
OnboardingView.mc: slajdy 2-3 w pelni generowane z ActionMap (ticki easeColor, slowa ocen przy bezelu, strzalki fillPolygon). GarmankiApp.getInitialView zwraca OnboardingView gdy brak flagi; BACK = pomin + flaga. Guide w DeckMenu (id guide, popView na koncu). Build fr965: BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
OnboardingView z 4 slajdami generowanymi z ActionMap, flaga onboardingSeen, Guide w DeckMenu
<!-- SECTION:FINAL_SUMMARY:END -->
