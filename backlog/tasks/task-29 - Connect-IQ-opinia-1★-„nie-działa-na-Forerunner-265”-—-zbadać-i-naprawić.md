---
id: TASK-29
title: 'Connect IQ: opinia 1★ „nie działa na Forerunner 265” — zbadać i naprawić'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-25 10:12'
updated_date: '2026-10-25 10:33'
labels: []
dependencies: []
references:
  - 'https://apps.garmin.com/apps/f3cb31bc-f5d9-4818-b730-4c67e98e3b7a'
priority: high
type: bug
ordinal: 36000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
2026-10-25 użytkownik zostawił w Connect IQ Store opinię 1★: „Nie działa na forerunner 265”. Brak szczegółów i kontaktu. fr265/fr265s są w watch/manifest.xml, więc urządzenie jest formalnie wspierane — trzeba ustalić, co nie działa (crash, brak łączności z companionem, instalacja, pusty ekran). Opinia obniża pierwszą średnią ocenę listingu.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Sprawdzone crash reporty Connect IQ dla fr265/fr265s (skill garminiq, era_fetch.py)
- [ ] #2 Uruchomienie symulatora z fr265 i fr265s przez monkeydo — przejście: lista talii, sesja, ocena; wynik opisany w notatkach
- [x] #3 Zweryfikowane limity pamięci/RAM aplikacji dla fr265 vs reszta wspieranych modeli
- [x] #4 Ustalona przyczyna albo uzasadnienie, że błąd jest niereprodukowalny; poprawka (jeśli potrzebna) wydana wg Version Bump
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Crash reporty: skill garminiq / era_fetch.py (APPID f3cb31bc-...), last-crash-report.json — filtr fr265/fr265s.
2. Build monkeyc dla fr265 i fr265s (+ fr965, fr165 jako referencja) — ostrzeżenia pamięci/zasobów.
3. Przegląd monkey.jungle, resources-*, manifest (uprawnienia, products), qualifiery dla 416x416 / 360x360, wejście (przyciski/dotyk).
4. Limity pamięci: compiler.json z ~/.Garmin/ConnectIQ/Devices dla fr265/fr265s vs reszta; rozmiar .prg / peak memory.
5. Symulator (Xvfb jeśli jest) + monkeydo fr265/fr265s: lista talii, sesja, ocena; jeśli niemożliwe — zapisać ograniczenie.
6. Wnioski: przyczyna lub uzasadnienie niereprodukowalności; ewentualna poprawka z bumpem AppVersion (patch), bez publikacji.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Śledztwo 2026-10-25 (subagent):
AC1 ERA: era_fetch.py z APPID f3cb31bc-f5d9-4818-b730-4c67e98e3b7a, onlyLatestAppVersion=false, login 1Password -> HTTP 200. Jedyny raport w całej historii: Out Of Memory Error, v1.2.2, produkt 006-B4260-00 (= venu3, NIE fr265), fw 17.05, 1 wystąpienie 2026-09-02, lang deu, backtrace CardStore.replaceAll:57 (Storage.setValue ostatniego chunka) <- PhoneLink._assemble:214 <- _onState <- onMessage. Brak raportów dla fr265 (006-B4257-00) i fr265s (006-B4258-00). ~/.claude/skills/garminiq/last-crash-report.json to stary raport garmin-note (fr965, lipiec) — nie dotyczy.
AC2 symulator: na VPS (aarch64) simulator SDK jest x86-64, nie startuje (brak libexpat.so.1 amd64 i reszty stosu GTK/WebKit amd64) — porzucone. Uruchomione na Macu (SDK mac 9.2.0; skopiowano definicje urządzeń fr265/fr265s do ~/Library/Application Support/Garmin/ConnectIQ/Devices). Kopia robocza w scratchpadzie z seedem Storage w onStart (2 talie, 20 kart; oraz stres 400 kart x 300/300 znaków — limit MAX_CARDS i §2). monkeydo fr265 i fr265s: start bez wyjątku, HomeView po seedzie. Pamięć (stres, ścieżka chunki->assemble->replaceAll): peak used ~413 KB z 781 888 B (free ~368 KB) na obu. Przejścia lista talii -> sesja -> ocena NIE wykonano interaktywnie (brak bezpiecznego wstrzykiwania przycisków do okna symulatora na Macu bez przejmowania sesji usera) — do ręcznego sprawdzenia.
AC3 pamięć: compiler.json watchApp memoryLimit: fr265 = fr265s = 786432 B (768 KB) — tyle samo co fr165/fr965/venu3 i wszystkie AMOLED oraz fenix7/8; fr255/fr255s 512 KB; fenix6 Pro 1280 KB. monkeyc -w dla fr265/fr265s: BUILD SUCCESSFUL, prg 145 KB, te same ostrzeżenia typów co fr165, zero ostrzeżeń pamięci/zasobów. fr265/fr265s używają domyślnego profilu fivebtn-amoled (jak fr165/fr965), CIQ 5.2.0 — brak ścieżki kodu specyficznej dla fr265.
AC4: błąd niereprodukowalny dla fr265 jako urządzenia. Najbardziej prawdopodobne: brak/nieskonfigurowany companion (iPhone, brak APK/AnkiDroid/uprawnienia) -> zegarek pokazuje HomeNoData „No data — open the Garmanki app on your phone” = „nie działa”. Druga, słabsza hipoteza: OOM przy synchronizacji dużej talii (jak venu3; ten sam limit 768 KB), ale w symulatorze najgorszy przypadek mieści się z zapasem ~368 KB. Bez zmiany kodu -> bez bumpa wersji. Follow-up: odpowiedź na opinię z prośbą o szczegóły (TASK-32), jasne wymagania w listingu (TASK-30).

Weryfikacja dyrygenta 2026-10-25: git status bez zmian w śledzonych plikach (kodu nie ruszano); era_reports.json potwierdza 1 raport OOM = venu3, brak fr265/fr265s; memoryLimit fr265=fr265s=fr165=fr965=venu3=786432 B odczytane z compiler.json. AC2 otwarte: brakuje ręcznego przejścia lista talii -> sesja -> ocena na fr265/fr265s (symulator lub prawdziwy zegarek). Follow-up (do decyzji usera): OOM na venu3 w CardStore.replaceAll (v1.2.2); komunikat HomeNoData mógłby mówić o wymaganiu Androida i AnkiDroid.
<!-- SECTION:NOTES:END -->
