---
id: TASK-14
title: Klient Anki na zegarek Garmin (z Android companion)
status: To Do
assignee: []
created_date: '2026-07-23 10:09'
labels:
  - garmanki
  - from-recording
  - anki
dependencies: []
priority: medium
ordinal: 14000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Kontekst: nagranie dyktafonu id 128 z 2026-07-22 09:29.

Chciałbym stworzyć aplikację nazywak Zegarek Garmin, która jest klientem dla Aneki. Synchronizuje się z Anki przez internet Zaznaczy pewnie za pośrednictwem jakiejś aplikacji typu companion albo wtyczki do Anki Nie wiem, ale powinna mieć możliwość wybierania talii, którą chcemy ćwiczyć. Powinna mieć możliwość kontroli za pomocą przycisków oraz za pomocą ekranu. Powinna mieć możliwość flagowania, usuwania zawieszania kart to co użytkownik ma w apce powinno być kontrolowane w tym kompanionie na Anroid. Powinna być też w komponianie opcja wybierania, które talie chcemy tam mieć jakie opcje, na przykład jeżeli skończyły się karty w naszej talii, czy wtedy ma być tak jak w normalnej apcji Anki, możliwość dodania kart na dzień dzisiejszy, czy mają być jakieś podstawowe statystyki przejrzyj jak działa Anki, jakie ma opcje i co możemy wykorzystać, co warto donieść na zegarek Garmin, żeby się zmieściło Przygotuję decyzji do podjęcia. Listę problemów. W sensie listę decyzji do podjęcia dla mnie i będę je podejmował tak, żebyś ty już mogło wystartować potem stworzeniem gotowej aplikacji.

UWAGA: przed implementacją wymagana lista decyzji do rozstrzygnięcia przez użytkownika (patrz AC #1).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Przegląd funkcji Anki (talie, opcje, statystyki, tryby powtarzania, flag/suspend/delete) — dokument z rekomendacją co warto przenieść na zegarek
- [ ] #2 Lista decyzji do podjęcia przez użytkownika przed startem implementacji (kanał synchronizacji z Anki: AnkiWeb API vs wtyczka desktopowa vs companion-only; zachowanie po wyczerpaniu talii; zakres statystyk; itd.)
- [ ] #3 Wybór talii do ćwiczenia z poziomu zegarka
- [ ] #4 Kontrola za pomocą przycisków fizycznych oraz ekranu dotykowego
- [ ] #5 Flagowanie, usuwanie i zawieszanie kart z poziomu zegarka
- [ ] #6 Companion Android: konfiguracja co ma być dostępne na zegarku (które talie, jakie opcje)
- [ ] #7 Companion Android: obsługa zachowania po wyczerpaniu kart (add-more jak w Anki desktop lub inne)
- [ ] #8 Podstawowe statystyki użycia widoczne na zegarku i/lub w companion
- [ ] #9 Synchronizacja stanu (flag/suspend/delete) między zegarkiem, companionem i Anki
<!-- AC:END -->
