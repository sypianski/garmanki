---
id: TASK-20
title: 'Google Play: pierwszy release produkcyjny (vc14 / 1.0.4)'
status: Done
assignee:
  - '@claude'
created_date: '2026-10-12 11:05'
updated_date: '2026-10-12 13:59'
labels:
  - premiera
dependencies: []
references:
  - TASK-18
priority: high
ordinal: 20000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Google przyznał production access (2026-10-12). Tor production jest pusty — apka nigdy nie miała release produkcyjnego (live wisiało tylko na internal/alpha). Promujemy ten sam artefakt, który przeszedł test zamknięty: AAB versionCode=14 (1.0.4, target API 36), bez ponownego buildu — bump wersji dotyczy dopiero kolejnego builda. Wcześniejsza próba przez Play Developer API kończyła się 400 Precondition check failed (zapis do toru production, patrz TASK-18) — najpewniej brak production access; spróbować ponownie przez API (skill play-testing), a gdy nadal 400 — przeklikać w Play Console (Production > Create new release > Add from library). Wysłanie do review jest działaniem zewnętrznym: wymaga osobnego, jawnego OK usera (Publish Gate). Review pierwszego release produkcyjnego trwa zwykle do kilku dni.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Draft release w torze production zawiera AAB versionCode=14 (z biblioteki, bez nowego buildu)
- [x] #2 Ustawiona dostępność w krajach/regionach
- [x] #3 Release notes dodane (min. en-US)
- [x] #4 Publishing overview nie zgłasza brakujących sekcji ani oczekujących zmian (listing, data safety, content rating)
- [x] #5 Release wysłany do review dopiero po jawnym OK usera
- [x] #6 Google zatwierdził, apka jest live; link do listingu zapisany w notatkach zadania; TASK-18 kryterium #5 odhaczone
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-12 (dirigu, weryfikacja niezależna tracks.list): tor production nadal pusty, internal+alpha bez zmian (1.0.4 / vc14 / completed). Dawne 400 Precondition check failed ZNIKNĘŁO — production access działa: tracks.update(production, versionCodes [14], status draft, release notes en-US) i edits.validate przechodzą. BLOKER: edits.commit zwraca 400 INVALID_ARGUMENT "Changes are sent for review automatically. The query parameter changesNotSentForReview must not be set." — Managed publishing jest WYŁĄCZONE, więc każdy commit przez API idzie do review automatycznie; nie ma bezpiecznego commitu draftu z API. Edit skasowany, nic nie zapisano w Play. Country availability dla production = {} (API v3 ma tylko get, kraje ustawia się w UI). CDP (porty 29223/19223/9223/19222) nie odpowiada, ssh do Maca: connection reset — AC4 nie sprawdzone. Dwie drogi do AC1 (decyzja usera): (A) commit draftu przez API bez changesNotSentForReview — draft prawdopodobnie nie wchodzi do kolejki review, ale API tego nie potwierdza; (B) user klika w Play Console: Release > Production > Create new release > Add from library > vc14 > release name 1.0.4 > notes en-US > Save as draft (NIE Save and publish / Send for review / Start rollout). Kraje i tak trzeba ustawić w UI: Release > Production > Countries/regions > Add countries/regions. Skrypty: scratchpad/step1.py (odczyt), step2.py (update+validate+commit).

2026-10-12 (dirigu, po decyzji usera: zapis przez skrypt): edits.commit BEZ changesNotSentForReview zakończył się OK (edit 05996735490858563555). Niezależny odczyt tracks.list w nowym edicie: production = release 1.0.4, versionCodes [14], status draft, release notes en-US (First public release. Review your Anki cards on a Garmin watch…); alpha bez zmian (1.0.4 / vc14 / completed). Kryteria #1 i #3 spełnione. NIEZNANE: czy Google przyjął ten draft do kolejki przeglądu — API pokazuje tylko status draft; sprawdzić w Play Console > Publishing overview (sekcja Changes in review / Changes not yet sent for review). ZOSTAJE: #2 kraje dla production (country availability = {}, w UI: Release > Production > Countries/regions > Add countries/regions > Save), #4 przegląd Publishing overview (CDP niedostępne), #5 wysłanie do review/publikacja — dopiero po jawnym OK usera, #6 zatwierdzenie przez Google.

2026-10-12 (odczyt Play Console przez retumilo/CDP, Mac odzyskany): (1) Publishing overview jest PUSTY — brak sekcji Changes in review i Changes not yet sent for review; widoczne tylko Managed publishing off, Last published on July 30, 2026 (upload vc14 do testów) i Submission activity. Wniosek: szkic produkcyjny NIE trafił do kolejki przeglądu, czyli commit przez API zostawił go jako szkic. (2) Strona Release > Production: Inactive, Draft release: 1.0.4, 0 countries / regions, 9 installs. (3) Do zrobienia przez usera/UI: ustawić kraje (Release > Production > zakładka Countries / regions), potem Create new release / edycja szkicu > Next > Save, a wysłanie do review dopiero po jawnym OK. Kryterium #4 częściowo: oczekujących zmian brak; checklisty Dashboard (listing/data safety/content rating) nie przeglądałem — ścieżka dla Production wymaga tych sekcji, a test zamknięty już je przeszedł.

2026-10-12 (retumilo/CDP, na polecenie usera: ustaw kraje i zapisz szkic; BEZ wysyłki): (1) Production > Countries/regions: zaznaczone wszystkie 178 krajów/regionów (+ rest of world), Save -> "Your change has been saved". Zakres = WSZYSTKIE kraje — decyzja domyślna, do przejrzenia przez usera przed wysyłką; da się zawęzić w Countries/regions. (2) Production > Releases > Edit release > Next > Save: ekran Preview and confirm — 0 błędów, 2 ostrzeżenia informacyjne (brak pliku deobfuskacji R8/proguard; brak symboli debugowania dla kodu natywnego), bundle 14 (1.0.4), API 26+, target SDK 36, 11.1 MB, release notes en-US; Save -> "Your change has been saved". (3) Publishing overview: sekcja Changes not yet submitted for review, przycisk Submit 3 changes for review (kraje x2 + release produkcyjny); NIC nie jest w review. Play uruchomił Running quick checks for commonly found issues (do ok. 14 min); po Submit zmiany idą do Google dopiero po pomyślnym zakończeniu tych sprawdzeń. Managed publishing off. NIE KLIKNIĘTO Submit 3 changes for review — to krok #5, wymaga jawnego OK usera. Kryterium #4 uznane: brak blokad i brakujących sekcji, są tylko 3 własne zmiany gotowe do wysyłki. Warning o EU: banner Geo-blocking regulation (EU) 2018/302 — tylko informacja, do zamknięcia (Dismiss). Do rozważenia przed/po premierze: wgrać mapping.txt/symbole, żeby raporty crashy były czytelne.

2026-10-12 ~15:10 CEST: user potwierdził wysyłkę ("tak"). W Publishing overview kliknięto Submit 3 changes for review > Send changes for review (okno: release 1.0.4 Start full rollout + kraje x2; Google: przegląd zwykle do 7 dni, może dłużej). Wynik: sekcja Changes in review z linkiem Remove changes (można wycofać zgłoszenie). Play wciąż uruchamia Running quick checks for commonly found issues (ok. 11 min) — zmiany pójdą do przeglądu Google dopiero po ich pomyślnym zakończeniu; gdy sprawdzenia coś wykryją, Console pokaże problem w Publishing overview. Pozostaje #6: zatwierdzenie przez Google, apka live, link do listingu (https://play.google.com/store/apps/details?id=dev.sypianski.garmanki) i odhaczenie TASK-18 #5. Po zatwierdzeniu ruszają TASK-22/23/24/25.

2026-10-12 15:36 (sprawdzenie statusu): szybkie sprawdzenia Play przeszły pomyślnie — Publishing overview: Changes in review, komunikat 'Your changes are now in review'. Publiczny listing https://play.google.com/store/apps/details?id=dev.sypianski.garmanki zwraca jeszcze HTTP 404 (nie live). Czekamy na decyzję Google (zwykle do 7 dni).

2026-10-12 15:59: ZATWIERDZONE I LIVE. https://play.google.com/store/apps/details?id=dev.sypianski.garmanki -> HTTP 200, tytuł 'Garmanki — Anki for Garmin - Apps on Google Play', przycisk Install, brak komunikatu not found (wcześniej 404). Wszystkie 6 kryteriów spełnione. TASK-18 kryterium #5 (wersja w produkcji) tym samym spełnione. Odblokowane: TASK-22, 23, 24, 25.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Release produkcyjny 1.0.4 (vc14, target API 36) zatwierdzony przez Google i dostępny publicznie w Google Play w 178 krajach; dev.sypianski.garmanki.
<!-- SECTION:FINAL_SUMMARY:END -->
