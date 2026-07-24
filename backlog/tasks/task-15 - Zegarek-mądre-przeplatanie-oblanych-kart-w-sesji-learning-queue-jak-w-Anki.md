---
id: TASK-15
title: 'Zegarek: mądre przeplatanie oblanych kart w sesji (learning queue jak w Anki)'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-07-23 19:18'
updated_date: '2026-07-23 19:25'
labels:
  - garmanki
  - watch
  - anki
  - ux
dependencies: []
ordinal: 15000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Obecnie ReviewView re-queue oblanych kart (ease=1 'Again') robi _cards.add(card) — dopina kartę na sam koniec płaskiej tablicy sesji, max 2x (ReviewView.mc:201-208). Efekt: oblana karta wraca dopiero po WSZYSTKICH pozostałych nowych, a nie wpleciona w bieżący nurt. To nie jest przeplatanie, mimo że nagłówek pliku obiecuje 'learning steps get a same-session second pass'.

Cel: odtworzyć NA ZEGARKU czasową logikę learning queue z Anki, w całości lokalnie (bez zmian w kontrakcie companiona ani pv). Wykorzystać fakt, że snapshot karty już niesie nt1 (SCHEMA.md §2: 'next-interval label' dla Again z AnkiDroid NEXT_REVIEW_TIMES, np. '<1 min','10 min') — czyli sam AnkiDroid policzył długość learning-stepa; zegarek go pożycza zamiast symulować scheduler.

Model: zamiast płaskiej _cards + _i trzymać kolejkę nowych + learning queue z due-timestampami (System.getTimer()). Wybór karty priorytetowo: uczona z minionym due -> nowa -> learn-ahead (najbliższa uczona przed czasem, gdy brak nowych). Ocena >= Good zdejmuje kartę z learning queue (graduated), zastępując dotychczasowy cap _repeats<2.

Kontrakt bez zmian: kolejka odpowiedzi q nadal = 1 wiersz na wciśnięcie z prawdziwym ease/timeMs/epoch; zmienia się tylko KOLEJNOŚĆ prezentacji. Oblać->wrócić->Good produkuje wiersze Again potem Good, jak realna sesja Anki.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Oblanie karty (ease=1) wstawia ją do sesyjnej learning queue z due = teraz + interwał sparsowany z nt1 (a nie na koniec tablicy)
- [x] #2 Wybór następnej karty: uczona z minionym due ma priorytet nad nową; przy braku nowych działa learn-ahead (pokazuje najbliższą uczoną mimo że przed czasem)
- [x] #3 Parser etykiety nt (np. '<1 min','10 min','1 d') -> ms; interwały za długie na sesję (dni) obsłużone przez learn-ahead na końcu bez zawieszenia sesji
- [x] #4 Ocena >= Good (Hard/Good/Easy) zdejmuje kartę z learning queue (graduated); zastępuje cap _repeats < 2
- [x] #5 Zabezpieczenie przed nieskończoną pętlą: krok rośnie lub jest limit powtórzeń per karta, tak by sesja zawsze się kończyła
- [x] #6 Kolejka q niezmieniona: 1 wiersz na wciśnięcie oceny z prawdziwym ease/timeMs/epoch; brak zmian w SCHEMA/pv/companion
- [x] #7 Pasek postępu (arc + licznik n/N) przerobiony na dynamiczną kolejkę (nie _i/_cards.size(), które przy rosnącej tablicy jest błędne)
- [ ] #8 Kompiluje się monkeyc -d fr965 bez błędów; ręczny przebieg w symulatorze: oblana karta wraca wpleciona, nie na końcu
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. ReviewView: zamień płaską _cards+_i na model kolejek: _new (Array nowych, wskaźnik _ni) + _learn (Array wpisów [dueMs, card, stepIdx]). Dodaj _cur (aktualnie pokazywana karta) bo indeks _i znika.
2. _parseInterval(label): '<1 min'/'1 min'->~45-60s, 'N min'->N*60000, 'N h'->*3600000, 'N d'/'N mo'->wartość > progu sesji (SESSION_LONG) traktowana jako learn-ahead. Odporny na puste/nieznane -> fallback 60000.
3. _pick(): t=getTimer(); jeśli _learn ma wpis z dueMs<=t -> najbliższy (min); else jeśli _ni<_new.size -> _new[_ni++]; else jeśli _learn niepuste -> najbliższy (learn-ahead); else null=koniec.
4. grade(): zawsze queueRow (bez zmian). Jeśli ease==1: stepIdx z _cur; dueMs=t+_parseInterval(nt1)*mnożnik(stepIdx); dodaj do _learn; cap przez STEP_CAP (np. 3) by uniknąć pętli. Jeśli ease>=2 i karta była w _learn: już zdjęta przez _pick, nic. Zawsze _advance przez _pick.
5. _advance(): _cur=_pick(); jeśli null-> endSession(true); reset _showAnswer,_cardStartMs. openMenu/onUpdate używają _cur zamiast _cards[_i].
6. Postęp: licznik zrobiony/n-do-zrobienia. Arc na bazie _done vs (_done + pozostałe: (_new.size-_ni)+_learn.size). Górny licznik: pozostało = new+learn.
7. Kompilacja: export PATH SDK; monkeyc -f monkey.jungle -d fr965. Sanity w symulatorze jeśli dostępny.
8. Zaktualizuj komentarz nagłówkowy pliku by opisywał realną logikę (usuń rozjazd komentarz vs kod).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zaimplementowano w watch/source/ui/ReviewView.mc:
- Zastąpiono płaską _cards+_i modelem kolejek: _new (cursor _ni) + _learn (wpisy [dueMs, card, stepIdx]) + _cur/_curStep.
- _pick(): learning z minionym due (najbliższy) > nowa > learn-ahead (najbliższa uczona przed czasem gdy brak nowych) > null=koniec.
- _parseInterval(nt1): '<1 min'->30s, 'N min'->N*60s, 'N s'->N*1s; h/d/mo/yr -> SESSION_LONG (learn-ahead na koniec); floor 5s; fallback 60s.
- grade(ease==1 && _curStep<STEP_CAP=3): dodaje do _learn z due=t+base*(_curStep+1) (krok rośnie z każdą wpadką, terminacja gwarantowana). Ocena>=Hard nie re-queuuje (graduated) — zastąpiło _repeats<2.
- _action/openMenu na _cur. _advance() przez _pick() zamiast _i++.
- Postęp (dot-arc + licznik) na dynamicznym known=_done+1+remaining zamiast _i/_cards.size().
- Kolejka q bez zmian: 1 wiersz na wciśnięcie z realnym ease/timeMs/epoch. Zero zmian w SCHEMA/pv/companion.
- Nagłówkowy komentarz przepisany (usunięto rozjazd 'max twice' vs realna logika).
Kompilacja: monkeyc -d fr965 => BUILD SUCCESSFUL (bez ostrzeżeń).

Weryfikacja: (1) monkeyc -d fr965 => BUILD SUCCESSFUL, brak ostrzeżeń. (2) Wierny port logiki _pick/grade/_parseInterval do Pythona: 8 nowych kart, C oblane 1x (krok '<1 min'), F oblane 2x. Wynik kolejności: C wraca na poz. 8 — WPLECIONE przed ostatnią nową kartą H (poz. 9), nie na końcu; F dostaje próby przez learn-ahead po wyczerpaniu nowych; sesja się kończy (STEP_CAP). (3) git diff: zmiana wyłącznie w watch/source/ui/ReviewView.mc — SCHEMA.md/android/manifest nietknięte => brak zmian kontraktu/pv. AC#8 GUI-sim/on-device: do potwierdzenia przez usera na fr965.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Zegarek odtwarza teraz czasową learning queue Anki w obrębie jednej sesji: oblana karta (ease=1) wraca wpleciona wg due-timestampa pożyczonego z etykiety nt1 (AnkiDroid NEXT_REVIEW_TIMES), zamiast lądować na końcu płaskiej tablicy. Zmiana wyłącznie po stronie zegarka (ReviewView.mc): kolejki _new+_learn, _pick() z priorytetem due>nowa>learn-ahead, parser interwałów, graduacja przy ocenie>=Hard, terminacja przez STEP_CAP, dynamiczny pasek postępu. Kolejka odpowiedzi q i kontrakt (SCHEMA/pv/companion) bez zmian. Zweryfikowano: kompilacja fr965 OK + port algorytmu w Pythonie pokazujący realne przeplatanie. Pozostaje przebieg na fr965 (AC#8).
<!-- SECTION:FINAL_SUMMARY:END -->
