---
id: TASK-27
title: >-
  AnkiDroid wiki: dodać Garmanki (FAQ + Third-Party-Apps) i ujednolicić liczbę
  modeli
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-12 16:44'
updated_date: '2026-10-12 17:16'
labels:
  - premiera
dependencies: []
priority: medium
ordinal: 27000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Maintainer AnkiDroid (David_AnkiDroid) zaprosił w komentarzu pod postem na r/Anki (2026-10-12) do dodania Garmanki do wiki: uzupełnienie odpowiedzi w FAQ (Is there a version for Android Wear?) oraz nowa sekcja w Third-Party-Apps (sekcje alfabetycznie, Garmanki między Dokuen a GlyphAnki). Edycja wiki to publikacja zewnętrzna: wymaga jawnego OK usera na dokładną treść (Publish Gate). Ujednolicenie liczby modeli: lista zgodności w Connect IQ Store = 45 modeli (urządzeń) i 73 numery katalogowe (warianty sprzętowe); post na r/Anki mówi o ~45 modelach, README tylko o 73 wariantach. Wszędzie podawać: 45 modeli (73 warianty sprzętowe).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Wpis dla FAQ i sekcja dla Third-Party-Apps przygotowane w stylu istniejących wpisów wiki; treść zatwierdzona przez usera
- [x] #2 Zmiany wprowadzone w wiki AnkiDroid dopiero po jawnym OK usera i potwierdzone na żywych stronach
- [x] #3 README.md podaje liczbę modeli i wariantów spójnie (45 modeli / 73 warianty sprzętowe)
- [ ] #4 Post na r/Anki poprawiony na 45 modeli (73 warianty) po jawnym OK usera na dokładną treść
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-12 18:46: AC3 — README.md poprawiony i scommitowany lokalnie (BEZ pushu, czeka na OK): 'The app is built for 45 watch models (73 hardware variants, counted by Garmin part number)' oraz tabela Layout '45 models / 73 hw variants'. Źródło liczb: TASK-26 (publiczny listing 1.2.3: Compatible Devices = 73 numery katalogowe / 45 urządzeń) i manifest (45 iq:product). Wiki AnkiDroid: przygotowane kopie z wprowadzonymi zmianami (scratchpad: wiki_FAQ.new.md, wiki_Third-Party-Apps.new.md; oryginały pobrane anonimowo przez HTTPS z raw.githubusercontent.com/wiki/ankidroid/Anki-Android/). FAQ: dopisany akapit pod 'Is there a version for Android Wear?'; Third-Party-Apps: nowa sekcja Garmanki między Dokuen a GlyphAnki (sekcje alfabetyczne; styl: nagłówek-link + jeden akapit). Linki zweryfikowane HTTP 200. Uprawnienia do wiki: repo ankidroid/Anki-Android — nasze konto sypianski ma permissions.push=false na repo; czy wiki jest otwarte do edycji — nieznane, wyjdzie przy próbie. Czeka na jawne OK usera na dokładną treść (wiki + edycja posta na r/Anki + push README). Uwaga: jedno polecenie (ticket + klon wiki przez SSH + rm -rf) zostało odrzucone — nie powtarzano, rozbite na mniejsze kroki.

2026-10-12 18:54: OK usera na 3 publikacje (wiki, edycja posta, push README). (1) README: push wykonany (343471c, 39a0009..343471c) — AC3 spełnione. (2) Wiki AnkiDroid: wpis nałożony na świeży klon (ec3f6f6, TYLKO lokalnie w scratchpad/ankidroid-wiki, bez nowych URL-i bez opisu), ale git push ODRZUCONY ('Could not read from remote repository') mimo poprawnego uwierzytelnienia jako sypianski (ssh -T OK; odczyt/klon działa) — najpewniej wiki ma 'Restrict editing to collaborators'; konto nie ma prawa zapisu (repo permissions.push=false). Przeglądarka retumilo nie jest zalogowana do GitHuba (0 ciasteczek), a nawet po zalogowaniu uprawnienia pewnie by nie wystarczyły. AC2 NIEZREALIZOWANE: trzeba poprosić maintainera (David_AnkiDroid) o dodanie wpisu/uprawnienia — odpowiedź na Reddicie wymaga OK usera. (3) Edycja posta na r/Anki (~45 -> 45): NIE wykonana — Reddit zwraca 'blocked by network security' dla kart otwieranych automatem, mimo że retumilo ma sesję (reddit_session ważna ~180 dni; to ta sama instancja c61a1a82… z profilem retumilo/profile, potwierdzone na Macu). Nie obchodzę blokady antybotowej; user edytuje ręcznie (zmiana jednego znaku). AC4 otwarte. Szkice maila/forum (TASK-24): gołe URL-e zamienione na linki z opisem.

2026-10-12 19:06: wiki AnkiDroid dodane przez usera. Weryfikacja na żywo (raw.githubusercontent wiki + strony renderowane HTTP 200): FAQ — akapit o Garmanki pod 'Is there a version for Android Wear?' (linia 23); Third-Party-Apps — sekcja Garmanki między Dokuen a GlyphAnki. Wpis zawiera 'open source' — patrz sprawdzenie licencji repo w kolejnym wpisie, jeśli dotyczy.

2026-10-12 19:06: UWAGA — repo sypianski/garmanki nie ma licencji (gh api: license=brak, brak pliku LICENSE; README mówi tylko 'The source is public'). Wpis w wiki AnkiDroid (FAQ i Third-Party-Apps) oraz komentarz maintainera używają określenia 'open source', co bez licencji nie jest ścisłe (domyślnie wszystkie prawa zastrzeżone). Błąd po mojej stronie: sformułowanie 'open source' trafiło do tekstu wpisu bez sprawdzenia licencji. Do decyzji usera: (a) dodać licencję (np. MIT/Apache-2.0/GPL-3.0 — wybór usera; powiązane z TASK-23, gdzie licencja jest już w kryteriach), albo (b) zmienić wpis w wiki na 'source available'. Szkice odpowiedzi na komentarze r/Anki (David, Smooth-Put5476, moofish2842, hight996) przygotowane; fakty zweryfikowane w SCHEMA §2/§6, ReviewView.mc (TextArea z 5 rozmiarami czcionki, brak przewijania), ActionMap.mc (domyślne mapowanie).

2026-10-12 19:11: odpowiedź dla David_AnkiDroid (potwierdzenie dodania wpisów do wiki) wklejona przez usera. Zostają: odpowiedzi dla Smooth-Put5476 i moofish2842 (user wkleja ręcznie), poprawka '~45'->'45' w poście (AC4), decyzja o licencji repo (albo zmiana wpisu wiki na 'source available').

2026-10-12 19:16: LICENCJA — user wybrał MIT (rekomendacja dyrygenta; powody: najprostsza, nie utrudnia przyszłego iOS z TASK-19 — App Store źle znosi copyleft; żadne z 23 publicznych repo usera nie miało licencji, więc brak wzorca). Dodane i WYPCHNIĘTE (7b7816f, po zgodzie usera): LICENSE (MIT, Copyright (c) 2026 Jakub Sypiański), THIRD-PARTY-NOTICES.md (Space Mono, SIL OFL 1.1 — pełny tekst upstream OFL.txt bez zmian; w plikach TTF copyright 'Google Inc.', upstream 'The Space Mono Project Authors'), sekcja License w README. Przegląd zależności: AndroidX/Compose/kotlinx = Apache-2.0 (nie w repo), Garmin CIQ Mobile SDK = zewnętrzny AAR z Mavena (nie w repo), AnkiDroid tylko przez ContentProvider (bez linkowania kodu), watch bez cudzego kodu. Określenie 'open source' w wiki AnkiDroid jest teraz prawdziwe — nie trzeba zmieniać wpisu.
<!-- SECTION:NOTES:END -->
