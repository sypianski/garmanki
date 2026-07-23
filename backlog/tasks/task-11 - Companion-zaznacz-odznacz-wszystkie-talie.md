---
id: TASK-11
title: 'Companion: zaznacz/odznacz wszystkie talie'
status: Done
assignee:
  - '@claude'
created_date: '2026-07-23 09:23'
updated_date: '2026-07-23 09:24'
labels: []
dependencies: []
references:
  - android/app/src/main/kotlin/app/sypianski/garmanki/ui/MainScreen.kt
ordinal: 11000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Uzupelnienie DecksCard (TASK-9/TASK-10, drzewo talii + kaskadowe checkboxy folderow). User chce jeden przelacznik nad lista talii ktory zaznacza lub odznacza wszystkie widoczne talie naraz, analogicznie do kaskady na poziomie folderu ale obejmujacy cala liste.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Nad drzewem talii jest widoczny kontrolka (checkbox/TriStateCheckbox z etykieta) reprezentujaca stan zaznaczenia wszystkich talii: zaznaczona gdy wszystkie talie sa wybrane, pusta gdy zadna, indeterminate gdy czesc
- [x] #2 Klikniecie zaznacza wszystkie talie gdy nie wszystkie byly zaznaczone, odznacza wszystkie gdy wszystkie byly zaznaczone (jedna atomowa aktualizacja selectedDecks przez istniejace onToggleDecks)
- [x] #3 Dziala niezaleznie od stanu rozwiniecia/zwiniecia folderow w drzewie (operuje na pelnej liscie talii, nie tylko widocznych)
- [x] #4 ./gradlew assembleDebug lint testDebugUnitTest przechodzi
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Dodano wiersz 'Select all' (string res decks_select_all) nad drzewem w DecksCard: TriStateCheckbox liczony po decks.map{id} (pelna plaska lista wszystkich realnych talii, niezaleznie od expand/collapse folderow), klik na caly wiersz lub checkbox wola ten sam onToggleDecks(allIds, allState != On) co kaskada folderow - jedna atomowa aktualizacja selectedDecks. ./gradlew assembleDebug lint testDebugUnitTest -> exit 0.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Nad lista talii w DecksCard jest teraz wiersz 'Select all' z TriStateCheckbox (On/Off/Indeterminate wg stanu wszystkich talii), klikniecie zaznacza lub odznacza wszystkie talie jedna atomowa aktualizacja selectedDecks (reuzywa mechanizmu onToggleDecks z TASK-10). Dziala na pelnej liscie decks (nie zalezy od stanu rozwiniecia drzewa). Zweryfikowane: gradlew assembleDebug lint testDebugUnitTest zielone.
<!-- SECTION:FINAL_SUMMARY:END -->
