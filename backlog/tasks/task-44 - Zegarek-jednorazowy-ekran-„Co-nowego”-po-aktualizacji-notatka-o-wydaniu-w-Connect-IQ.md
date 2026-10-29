---
id: TASK-44
title: >-
  Zegarek: jednorazowy ekran „Co nowego” po aktualizacji + notatka o wydaniu w
  Connect IQ
status: In Progress
assignee: []
created_date: '2026-10-29 18:24'
updated_date: '2026-10-29 18:56'
labels:
  - watch
  - ux
dependencies: []
priority: medium
ordinal: 53000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Po aktualizacji do nowej wersji zegarek raz pokazuje krótki ekran z nowościami (dla 1.3.2: BACK = Opcje z Cofnij, szybkie wyjście z podsumowania, talia przykładowa, poprawione gesty). Nowi użytkownicy widzą przewodnik, nie ten ekran. Przy wysyłce do Connect IQ — ta sama treść w notatce o wydaniu. Decyzja usera 2026-10-29.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Po aktualizacji (onboardingSeen=true, zapisana wersja ≠ AppVersion) zegarek raz pokazuje ekran Co nowego; START/BACK/dotknięcie przechodzi do ekranu głównego i zapisuje wersję
- [ ] #2 Świeża instalacja: przewodnik, bez ekranu Co nowego (wersja zapisana po przewodniku)
- [ ] #3 Teksty PL/EN mieszczą się na okrągłych ekranach (fr965, vivoactive4, fenix6pro, fr255)
- [x] #4 Notatka o wydaniu 1.3.2 przygotowana do pola w formularzu Connect IQ
- [x] #5 SCHEMA.md §5 opisuje nowy klucz Storage; lint bez nowych wpisów
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-29: source/ui/WhatsNewView.mc — moduł WhatsNew (NOTES_ID="1.3.2", pending()/markSeen(), Storage newsSeen) + widok (tytuł, v1.3.2, 4 linijki News1-4, font TINY albo XTINY wg cięciwy, znacznik przy START) + delegat (START/tap/BACK → HomeView, zapis). GarmankiApp.getInitialView: przewodnik → Co nowego → Home. Przewodnik przy pierwszym uruchomieniu też zapisuje newsSeen. Ekran pokazuje się według NOTES_ID, nie AppVersion — wydanie bez nowości zostawia stare id. Buildy 5 urządzeń OK, lint identyczny z HEAD. Zmieszczenie tekstu na 218-260 px niesprawdzone.

Projekt notatki do #app-whats-new-en (czeka na akceptację usera):
- BACK during a review now opens Options: Undo, card actions (suspend, bury, flag, delete) and End session. Holding UP opens the same menu.
- After a session, BACK goes straight to the watch face; START returns to the deck list.
- New built-in sample deck: 20 Esperanto → English words that work before your phone syncs. You can remove it from the deck list.
- Taps and swipes are recognised reliably, and the first-run guide lets you try every button and gesture.
- Fix: answers could be lost if the app closed before the phone confirmed them.

2026-10-29: user zaakceptował notatkę do What's New.

2026-10-29: wysłane w 1.3.2 do Connect IQ (czeka na zatwierdzenie Garmina).
<!-- SECTION:NOTES:END -->
