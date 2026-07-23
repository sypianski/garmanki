---
id: TASK-10
title: 'Companion: checkboxy talii po prawej + kaskadowe zaznaczanie folderu'
status: Done
assignee:
  - '@claude'
created_date: '2026-07-23 08:59'
updated_date: '2026-07-23 09:01'
labels: []
dependencies: []
references:
  - android/app/src/main/kotlin/app/sypianski/garmanki/ui/MainScreen.kt
  - android/app/src/main/kotlin/app/sypianski/garmanki/anki/DeckTree.kt
ordinal: 10000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Uzupelnienie TASK-9 (drzewo talii wg '::'). User chce: 1) checkboxy zaznaczania talii/folderow konsekwentnie po prawej stronie wiersza (dzis lisc ma checkbox z lewej, naglowek grupy z prawej - niespojnosc); 2) zaznaczenie checkboxa folderu (naglowka grupy) ma kaskadowo/wizualnie zaznaczac tez wszystkie podtalie w tym folderze, bo zaznaczenie folderu bedzie i tak stosowane do talii wewnatrz. Dotyczy DecksCard/DeckTreeRows w android/app/src/main/kotlin/app/sypianski/garmanki/ui/MainScreen.kt i DeckTree.kt.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Wszystkie wiersze (lisc-talia i naglowek-folder) maja checkbox po prawej stronie wiersza
- [x] #2 Naglowek folderu ma TriStateCheckbox: zaznaczony gdy wszystkie potomne talie sa wybrane, pusty gdy zadna, indeterminate gdy czesc
- [x] #3 Klikniecie checkboxa folderu zaznacza/odznacza wszystkie potomne realne talie naraz (jedna aktualizacja selectedDecks, bez utraty innych zmian z tego samego klikniecia)
- [x] #4 Folder bez wlasnej talii (implied parent, deck=null) rowniez ma dzialajacy checkbox kaskadowy mimo braku wlasnego id
- [x] #5 ./gradlew assembleDebug lint testDebugUnitTest przechodzi
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
DeckTreeRows: checkbox zawsze po prawej (lisc: Column tekstu z indent + Checkbox na koncu; folder: TriStateCheckbox po prawej zamiast zwyklego Checkbox tylko dla node.deck!=null). Dodano DeckNode.allDeckIds() w DeckTree.kt (zbiera id wlasnej talii + wszystkich potomnych). Zmieniono API onToggleDeck(String,Boolean) -> onToggleDecks(List<String>,Boolean) w DecksCard/DeckTreeRows + call site w MainScreen, bo kaskadowe zaznaczanie wymaga jednej atomowej aktualizacji selectedDecks (kilka sekwencyjnych wywolan starego onToggleDeck nadpisywalyby sie nawzajem, bo kazde liczylo 'next' z tego samego zastanego settings.selectedDecks). TriStateCheckbox: On gdy wszystkie potomne id zaznaczone, Off gdy zadne, Indeterminate gdy czesc; klik zawsze przelacza w druga strone (On -> odznacz wszystko, inaczej -> zaznacz wszystko). Dziala tez dla implied parent (deck=null) bo kaskada operuje na allDeckIds(), nie na wlasnym id folderu. Dodano 3 testy allDeckIds w DeckTreeTest.kt (8/8 total pass). ./gradlew assembleDebug lint testDebugUnitTest -> exit 0.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Checkboxy talii/folderow sa teraz konsekwentnie po prawej stronie kazdego wiersza. Zaznaczenie folderu (naglowka grupy, z wlasna talia lub bez) uzywa TriStateCheckbox i kaskadowo zaznacza/odznacza wszystkie potomne talie naraz (jedna aktualizacja selectedDecks przez nowe API onToggleDecks(List<String>,Boolean)), wizualnie odzwierciedlajac ze wybor folderu obejmuje talie w srodku. Zweryfikowane: gradlew assembleDebug lint testDebugUnitTest zielone, 8/8 testow w DeckTreeTest.kt w tym 3 nowe dla allDeckIds (implied parent, real-parent-with-own-deck, zagniezdzenie).
<!-- SECTION:FINAL_SUMMARY:END -->
