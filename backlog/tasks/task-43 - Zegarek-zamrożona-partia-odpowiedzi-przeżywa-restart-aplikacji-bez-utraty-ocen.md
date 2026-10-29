---
id: TASK-43
title: >-
  Zegarek: zamrożona partia odpowiedzi przeżywa restart aplikacji (bez utraty
  ocen)
status: In Progress
assignee: []
created_date: '2026-10-29 18:05'
updated_date: '2026-10-29 18:07'
labels:
  - watch
  - sync
  - bug
dependencies: []
priority: high
ordinal: 52000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Błąd od 1.3.1 (prawdopodobnie od początku): PhoneLink trzyma liczbę wierszy wysłanej partii (_sentCount) tylko w pamięci. Scenariusz: partia N z k wierszami zostaje zapisana przez companion, ale potwierdzenie nie dociera, bo aplikacja się zamknęła (szybkie wyjście z TASK-41, timeout, przejście na tarczę). Po restarcie zegarek wysyła pod tym samym N wszystkie k+m wierszy; companion widzi N == lastAppliedBatch, potwierdza bez zapisu (SyncEngine.applyAnswers), a zegarek usuwa k+m wierszy — m nowych ocen ginie po cichu. Poprawka po stronie zegarka, bez zmiany protokołu: zapisać w Storage [batch, count] przy wysyłce i po restarcie wysłać ten sam zamrożony zestaw pod N; nowe wiersze dopiero pod N+1.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Przy wysyłce partii zegarek zapisuje w Storage numer partii i liczbę wierszy; czyści po potwierdzeniu ok
- [ ] #2 Po restarcie aplikacji ponowna wysyłka niepotwierdzonej partii N zawiera dokładnie te same wiersze; wiersze dodane później idą w partii N+1
- [ ] #3 Cofnij (TASK-41) nie usuwa wiersza należącego do zamrożonej partii także po restarcie
- [x] #4 SCHEMA.md §5 opisuje nowy klucz Storage
- [x] #5 Buildy bez błędów, lint bez nowych wpisów względem HEAD
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-29: CardStore.sentCount()/setSent() trzymają w Storage "sent" = [batch, count]; PhoneLink.flush bierze zamrożony count po restarcie, setSent przy wysyłce, setSent(0) po ack ok; inFlightCount zwraca persisted count, gdy w tej sesji nic nie leci (undo nie ruszy zamrożonych wierszy). Kolejka q nie jest nigdzie przycinana od początku poza ack, więc przesunięcie nie grozi. SCHEMA §5 + klucz sent. Buildy 5 urządzeń OK, lint -l 3 identyczny z HEAD. Test na zegarku+telefonie (zabicie apki przed ack) nie wykonany.
<!-- SECTION:NOTES:END -->
