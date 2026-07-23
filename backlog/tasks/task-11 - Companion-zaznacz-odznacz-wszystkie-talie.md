---
id: TASK-11
title: 'Companion: zaznacz/odznacz wszystkie talie'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-07-23 09:23'
updated_date: '2026-07-23 09:23'
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
- [ ] #1 Nad drzewem talii jest widoczny kontrolka (checkbox/TriStateCheckbox z etykieta) reprezentujaca stan zaznaczenia wszystkich talii: zaznaczona gdy wszystkie talie sa wybrane, pusta gdy zadna, indeterminate gdy czesc
- [ ] #2 Klikniecie zaznacza wszystkie talie gdy nie wszystkie byly zaznaczone, odznacza wszystkie gdy wszystkie byly zaznaczone (jedna atomowa aktualizacja selectedDecks przez istniejace onToggleDecks)
- [ ] #3 Dziala niezaleznie od stanu rozwiniecia/zwiniecia folderow w drzewie (operuje na pelnej liscie talii, nie tylko widocznych)
- [ ] #4 ./gradlew assembleDebug lint testDebugUnitTest przechodzi
<!-- AC:END -->
