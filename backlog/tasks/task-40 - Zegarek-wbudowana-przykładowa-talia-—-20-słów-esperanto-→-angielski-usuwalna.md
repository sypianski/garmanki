---
id: TASK-40
title: >-
  Zegarek: wbudowana przykładowa talia — 20 słów esperanto → angielski
  (usuwalna)
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-29 16:48'
updated_date: '2026-10-29 18:39'
labels: []
dependencies: []
priority: medium
ordinal: 49000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Prośba usera 2026-10-29. Talia demonstracyjna wbudowana w aplikację zegarka: działa od pierwszego uruchomienia bez telefonu i AnkiDroid (pierwsze wrażenie, recenzenci Garmina, użytkownicy przed konfiguracją companiona). Decyzja usera: wbudowana w zegarek (nie w AnkiDroid), kierunek esperanto → angielski, 20 słów. Wyjątek od zasady 'harmonogram w AnkiDroid': odpowiedzi z talii przykładowej NIE trafiają do kolejki odpowiedzi (SCHEMA §3) i nie mogą kolidować z deckIdx z pushu (§4 replaceAll).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Talia 'Esperanto' (20 kart, front esperanto, back angielski) widoczna w menu talii od pierwszego uruchomienia, także bez danych z telefonu
- [x] #2 Odpowiedzi i akcje kart z talii przykładowej nie trafiają do kolejki q i nie są wysyłane do companiona; menu karty w tej talii pokazuje tylko oceny
- [x] #3 Push stanu z telefonu (replaceAll) nie usuwa ani nie nadpisuje talii przykładowej
- [x] #4 Talię można usunąć z menu talii (z potwierdzeniem); po usunięciu nie wraca po restarcie ani po pushu
- [x] #5 Teksty PL/EN; litery ĉ ĝ ĥ ĵ ŝ ŭ sprawdzone na fr965
- [x] #6 Buildy wszystkich profili bez ostrzeżeń, lint bez nowych wpisów; SCHEMA.md §5 opisuje nowy klucz Storage
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zrobione (niezacommitowane, na wierzchu TASK-39): source/model/DemoDeck.mc — 20 słów (eo→en, w tym ĉ ĝ ĥ ĵ ŝ ŭ), deckIdx -1, cid/nid 'demo<i>', nt puste; Storage 'demoRemoved'. ReviewView: grade() nie kolejkuje odpowiedzi dla talii przykładowej, openMenu() pomija akcje kart. DeckMenu: pozycja 'Esperanto' (sub 'Przykład · 20 słów') po taliach z telefonu + 'Usuń talię przykładową' na końcu → WatchUi.Confirmation → DemoRemoveDelegate (DemoDeck.remove + Menu2.deleteItem obu pozycji). replaceAll nietknięte (talia nie jest w cards_<i>). SCHEMA §5: klucz demoRemoved + opis. Buildy fr965/vivoactive4/venu2/fr255/fenix6pro OK, lint -l 3 identyczny z HEAD (63 unikalnych). Postęp talii przykładowej nie jest zapamiętywany — każda sesja pokazuje 20 kart (Again wraca w sesji).

2026-10-29 (TASK-41): akcje karty są też w menu talii przykładowej, ale działają tylko lokalnie w sesji — nic nie trafia do q.
<!-- SECTION:NOTES:END -->
