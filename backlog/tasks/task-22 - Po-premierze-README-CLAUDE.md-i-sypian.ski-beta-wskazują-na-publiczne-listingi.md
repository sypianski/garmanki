---
id: TASK-22
title: >-
  Po premierze: README, CLAUDE.md i sypian.ski/beta wskazują na publiczne
  listingi
status: Done
assignee:
  - '@claude'
created_date: '2026-10-12 11:05'
updated_date: '2026-10-12 14:20'
labels:
  - premiera
dependencies:
  - TASK-20
  - TASK-21
references:
  - README.md
priority: medium
ordinal: 22000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
README.md (sekcja Status) mówi, że companion jest w becie na Google Play i odsyła do formularza sypian.ski/beta, a zegarek linkuje do bety Connect IQ (cc2be8ee…). Po wejściu apki na żywo to wprowadza w błąd. Linków do Play nie wstawiać przed zatwierdzeniem przez Google, żeby nie prowadziły do martwej strony. Adres listingu Play: https://play.google.com/store/apps/details?id=dev.sypianski.garmanki (applicationId jest niezmienny po publikacji). Strona sypian.ski/beta zbiera zapisy do bety — trzeba zdecydować, co z nią po premierze.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 README: sekcja Status wskazuje na listing Google Play i publiczny listing Connect IQ (store id z TASK-21), bez instrukcji zapisu do bety
- [x] #2 CLAUDE.md repo: sekcja Connect IQ Store (beta) zaktualizowana o stan po premierze (publiczny listing, historia UUID bety zachowana jako ostrzeżenie)
- [x] #3 Zdecydowane i wykonane: co z sypian.ski/beta (wygaszenie albo przekierowanie na listing Play)
- [x] #4 Zmiany scommitowane lokalnie; push dopiero po jawnym OK usera
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
README.md zaktualizowany (zmiana tylko w working tree, bez git add/commit/push): sekcja Status opisuje publiczne wydanie — companion w Google Play (v1.0.4, link do listingu) i aplikacja zegarka w Connect IQ Store (v1.2.3, store id f3cb31bc-...); usunięto link sypian.ski/beta i stary wpis bety cc2be8ee-.... Dodano sekcje: Requirements (Android 8+, AnkiDroid + Garmin Connect na tym samym telefonie, Connect IQ 4.2.1+ / API 3.4 dla fenix 6 Pro-class), Supported watches (rodziny z watch/manifest.xml, nazwy z compiler.json; testowane tylko na Forerunner 965; fenix 6 tylko Pro-class), Limitations (tylko tekst, tylko pismo łacińskie, brak iOS bez obietnic), nota o nieoficjalności, link do publicznego repo GitHub. Wiersz watch w tabeli Layout poprawiony zgodnie z CLAUDE.md (73 warianty hw incl. fenix6 Pro-class; minApiLevel 3.4.0). Zachowano zrzuty ekranu oraz linki do SCHEMA.md i DECYZJE.md. POZOSTAJE DO ZROBIENIA: AC #3 — strona sypian.ski/beta (leży poza repo; wygaszenie albo przekierowanie na listing Play); AC #4 — commit lokalny README po przeglądzie usera. AC nie zaznaczone, status bez zmian.

2026-10-12 16:17 (dirigu, weryfikacja): AC1 — README.md (commit 6f50b57 z innej sesji): sekcja Status linkuje do Google Play (dev.sypianski.garmanki) i publicznego listingu Connect IQ (f3cb31bc-…), 0 wystąpień sypian.ski/beta. AC2 — CLAUDE.md: sekcja Connect IQ Store z publicznym listingiem (1.2.3), stara beta zachowana, ostrzeżenie o zawłaszczonym appID nadal w pliku. AC3 (sypian.ski/beta) ustalenia: /beta to WSPÓLNY formularz zapisu dla 5 apek (servi/playstore: apps.json, usługa Node :3015, nginx location /beta/); garmanki ma w apps.json status testing + testUrl do zamkniętego testu; kazimirski/vaporetti/notescodes też testing, kanaletto hidden. Wygaszenie całej strony zepsułoby pozostałe apki — zmiana powinna dotyczyć tylko wpisu garmanki. Wymaga decyzji usera.

2026-10-12 16:20 (dirigu): AC3 wykonane po decyzji usera (ukryć TYLKO Garmanki). W /home/yaqub/servi/playstore/apps.json zmieniono wyłącznie garmanki.status testing -> hidden (diff = 1 linia; kopia: scratchpad/apps.json.bak); beta-service zrestartowany (serwer czyta apps.json raz przy starcie), active od 16:19:43. Weryfikacja dyrygenta: /apps lokalnie i https://sypian.ski/beta/api/apps -> [kazimirski, vaporetti, notescodes]; /beta/ HTTP 200; baza zgłoszeń bez zmian (failed 3, mailed 27, unconfirmed 3 — worker bierze dane z całego katalogu, więc obsługa istniejących zgłoszeń Garmanki nie ucierpiała; zablokowane są tylko NOWE zapisy). Cofnięcie: status garmanki z powrotem na testing + restart beta-service. apps.json w servi/playstore jest nieśledzony w git, więc nie ma tam commita. AC4: README (6f50b57, inna sesja) i CLAUDE.md (3388546, 25b71c2) scommitowane lokalnie; push wykonuje dirigu zgodnie z PC-004 (wyjątek lokalny skilla wywołanego przez usera), po skanie sekretów (czysto).
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Po premierze README i CLAUDE.md wskazują na publiczne listingi Play i Connect IQ, a Garmanki zniknął z formularza sypian.ski/beta (pozostałe apki bez zmian).
<!-- SECTION:FINAL_SUMMARY:END -->
