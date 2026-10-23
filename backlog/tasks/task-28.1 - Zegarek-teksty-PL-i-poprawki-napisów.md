---
id: TASK-28.1
title: 'Zegarek: teksty PL i poprawki napisów'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-23 18:00'
updated_date: '2026-10-23 18:03'
labels:
  - watch
  - ux
dependencies: []
parent_task_id: TASK-28
ordinal: 29000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Dodać resources-pol/strings.xml (pol w manifest.xml), przenieść 'Last sync:' i liczbę mnogą talii do zasobów. Skrócić napisy, które wystają poza okrąg po polsku (np. 'Sesja zakończona').
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Zegarek z językiem polskim pokazuje polskie napisy, angielski bez zmian
- [ ] #2 '1 deck' zamiast '1 decks'; 'Last sync' pochodzi ze strings.xml
- [ ] #3 Build monkeyc przechodzi dla fr965, fenix7 i venu3
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. strings.xml: formy liczby mnogiej talii (one/few/many), HomeLastSync, HomeStreakOne. 2. HomeView: Last sync ze strings, _decksWord, miesiąc z FORMAT_MEDIUM (język zegarka). 3. resources-pol/strings.xml + pol w manifest.xml; napisy rozstrzelane tylko ASCII. 4. Build fr965/fenix7/venu3.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zrobione, build monkeyc OK dla fr965, fenix7, venu3. Napisy rozstrzelane po polsku bez diakrytyków (toUpper nie jest pewne dla ą ś): 'Dzisiaj gotowe', 'Koniec sesji'. Miesiąc w linii sync idzie z Gregorian FORMAT_MEDIUM, więc też zależy od języka zegarka. NIE zweryfikowane na zegarku ani w symulatorze (headless nie daje obrazu): kryteria zostają niezaznaczone do testu na fr965 z językiem polskim.
<!-- SECTION:NOTES:END -->
