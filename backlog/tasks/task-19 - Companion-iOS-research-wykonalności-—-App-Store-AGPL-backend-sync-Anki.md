---
id: TASK-19
title: 'Companion iOS: research wykonalności — App Store, AGPL, backend sync Anki'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-09-10 07:58'
updated_date: '2026-09-10 08:03'
labels: []
dependencies: []
ordinal: 19000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Cel: publiczny companion na iOS w Apple App Store (decyzja usera 2026-09-10: rozwiązanie dla innych, nie osobisty hack). Na iOS nie istnieje AnkiDroid ContentProvider (kontrakt AnkiBridge z android/), a AnkiMobile nie wystawia API do due cards/answerCard — trzeba wybrać nowy backend kart. Research ma rozstrzygnąć wykonalność zanim powstanie plan implementacji.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Rozstrzygnięte: czy AGPL (rslib Anki) blokuje dystrybucję w App Store (źródła: FSF/Apple/precedensy)
- [ ] #2 Rozstrzygnięte: polityka AnkiWeb wobec nieoficjalnych klientów sync (TOS + stanowisko Anki)
- [ ] #3 Rozstrzygnięte: stan Connect IQ Mobile SDK na iOS (wersja, BLE w tle, ograniczenia)
- [ ] #4 Przegląd istniejących podejść: jak inne apki iOS integrują się z Anki
- [ ] #5 Rekomendacja architektury backendu kart z uzasadnieniem, zapisana w DECYZJE.md po akceptacji usera
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. WebSearch: AGPL vs App Store (FSF/precedensy, stanowisko Anki ws. rslib). 2. WebSearch: polityka AnkiWeb/TOS dla nieoficjalnych klientów sync; anki-sync-server. 3. WebSearch: Connect IQ Mobile SDK iOS — wersja, BLE w tle. 4. Przegląd istniejących integracji iOS↔Anki. 5. Synteza → rekomendacja architektury → decyzja usera → wpis do DECYZJE.md.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
USTALENIA (2026-09-10):
1. AGPL vs App Store: dystrybucja kodu AGPL przez App Store domyślnie NIEDOZWOLONA (EULA Apple = 'further restrictions', casus FSF/VLC); wymaga wyjątku Section 7 od właściciela praw (Ankitects). Embedding rsliba Anki w apce App Store = zablokowany bez zgody.
2. TOS AnkiWeb (§Access to the Service, zescrapowany): third-party klienci JAWNIE ZABRONIENI; approved clients = Anki, AnkiMobile, AnkiDroid, AnkiUniversal. TOS sam wskazuje AnkiConnect jako sankcjonowaną ścieżkę integracji.
3. AnkiMobile: URL scheme tylko addnote (x-callback-url) — brak API due cards/answer. Nieużywalne dla kontraktu AnkiBridge.
4. CIQ Mobile SDK iOS: istnieje oficjalnie (github.com/garmin/connectiq-companion-app-sdk-ios), apki standalone (bez GCM), background BLE wake możliwy.
WNIOSEK: jedyna TOS-czysta architektura publiczna zachowująca 'scheduler w Anki' = iPhone ↔ desktop Anki + AnkiConnect (LAN). Alternatywa: prośba do Ankitects o status approved client / wyjątek S7 (małe szanse — konkurencja dla płatnego AnkiMobile).

DECYZJA USERA (2026-09-10): przed wyborem architektury — list do Ankitects z pytaniem o (a) wąsko zakrojony dostęp sync do AnkiWeb (review-only approved client) lub (b) potwierdzenie ścieżki AnkiConnect-over-LAN. Szkic listu przygotowany (scratchpad/list-ankitects.md, dostarczony userowi); kanał: prywatny helpdesk anki.tenderapp.com/discussions/private. Wysyłka RĘCZNIE przez usera (formularz web, wymaga jego konta). Task czeka na odpowiedź Ankitects — dalszy wybór architektury po niej.
<!-- SECTION:NOTES:END -->
