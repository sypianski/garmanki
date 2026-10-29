---
id: TASK-41
title: 'Zegarek: BACK w powtórce otwiera Opcje (Cofnij, akcje karty, Zakończ sesję)'
status: In Progress
assignee: []
created_date: '2026-10-29 16:55'
updated_date: '2026-10-29 18:39'
labels: []
dependencies: []
priority: high
ordinal: 50000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Zgłoszenie usera z testu na fr965 (2026-10-29): w powtórce brakuje przycisku cofnij i przycisku 'więcej opcji' — widać tylko odpowiedzi i wyjście z sesji. Propozycja usera: opcje tam, gdzie teraz wyjście z sesji (BACK). Menu kart dotąd było tylko pod przytrzymaniem UP, które na fr965 w ogóle nie działało (TASK-39).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 BACK w powtórce otwiera menu Opcje: Cofnij (gdy jest co cofnąć), oceny niedostępne pod przyciskami (po odsłonięciu), akcje karty (poza talią przykładową), Zakończ sesję; BACK w menu je zamyka
- [x] #2 Przytrzymanie UP otwiera to samo menu
- [ ] #3 Cofnij przywraca poprzednią kartę i stan sesji (licznik, oceny, kolejkę powtórek) i usuwa ostatni wiersz z kolejki q, jeśli nie jest w locie ani już wysłany; inaczej komunikat 'Już wysłane'
- [x] #4 Cofnij działa też dla talii przykładowej (bez wierszy w q)
- [x] #5 Znacznik przy przycisku BACK w powtórce; przewodnik (slajd 4) opisuje Opcje pod BACK; teksty PL/EN
- [x] #6 Buildy wszystkich profili bez ostrzeżeń, lint bez nowych wpisów względem HEAD; przeklikane na fr965
- [x] #7 Na podsumowaniu sesji BACK wychodzi z aplikacji na tarczę (System.exit), START/dotknięcie wraca do listy talii; znaczniki przy START i BACK
- [x] #8 Na talii przykładowej menu ma te same akcje karty (wg ustawień companiona); działają tylko w bieżącej sesji, nic nie idzie do q
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-29: zaimplementowane. ReviewView: stos _history (max 10) zapisuje stan przed oceną/akcją (_cur, _curStep, _ni, _learn, _done, _counts + wiersz q); undo() zdejmuje wiersz przez CardStore.dropLastIf(row, Link.inFlightCount()) — tylko gdy to ostatni wiersz q i nie należy do partii w locie; inaczej toast UndoSynced i czyszczenie historii. openMenu: Cofnij (id 20) gdy historia niepusta, oceny tylko po odsłonięciu, akcje karty (nie dla DemoDeck), Zakończ sesję (id 21) zawsze. ReviewDelegate.onBack i onHoldUp -> openMenu. Znacznik MUTED na ANG_BACK=330 (wszystkie 4 profile). Guide4 PL/EN przepisane, slajd 4 rysuje znacznik przy BACK. Ograniczenie: ostatniej oceny sesji nie da się cofnąć (sesja kończy się od razu). Buildy fr965/vivoactive4/venu2/fr255/fenix6pro OK; lint -l 3 identyczny z HEAD (63 wpisy). Build fr965 leży na Macu: ~/ciq-dl/prg/garmanki-t41-fr965.prg — czeka na podłączenie zegarka.

2026-10-29: podsumowanie — BACK = System.exit() (API 1.0.0), START/tap = lista talii, znaczniki PAPER przy START i MUTED przy BACK. Z powtórki do tarczy: BACK → Zakończ sesję → BACK. Wgrane na fr965 (item 16783220).

2026-10-29: na prośbę usera akcje karty widoczne też na talii przykładowej; _action dla DemoDeck robi snapshot bez wiersza i nie kolejkuje (susp/bury/del zdejmują kartę z sesji, flag = toast). Kryterium #1 w części 'poza talią przykładową' nieaktualne.

2026-10-29: user przeklikał na fr965 (talia przykładowa): Opcje pod BACK, hold UP, Cofnij, wyjście z podsumowania — OK. AC#3 (cofanie z wierszem w q na prawdziwej talii) nie było testowane wprost.
<!-- SECTION:NOTES:END -->
