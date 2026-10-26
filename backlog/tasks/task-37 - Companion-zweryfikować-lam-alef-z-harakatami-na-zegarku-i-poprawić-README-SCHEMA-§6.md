---
id: TASK-37
title: >-
  Companion: zweryfikować lam-alef z harakatami na zegarku i poprawić
  README/SCHEMA §6
status: To Do
assignee: []
created_date: '2026-10-26 12:42'
labels:
  - companion
dependencies: []
references:
  - android/app/src/main/kotlin/app/sypianski/garmanki/data/ArabicShaper.kt
  - README.md
  - SCHEMA.md
priority: low
ordinal: 46000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
README (sekcja Limitations, linie 63-64) i SCHEMA §6 podają jako fakt, że lam-alef jest rysowany jako dwie litery zamiast ligatury. Źródłem jest komentarz w ArabicShaper.kt ('Not handled: lam-alef ligatures'), czyli lista tego, czego shaper nie robi, a nie obserwacja z zegarka. Zmierzone jest tylko, że na fr965 lam-alef bez harakatów (spike PC-003, linia L4) wyświetla się poprawnie, bo firmware składa ligaturę sam. Przy harakatach shaper zamienia litery na formy prezentacyjne (lam początkowy/środkowy + alef końcowy), więc nie wiadomo, czy zegarek narysuje ligaturę. Usunięte już z odpowiedzi do użytkowników (2026-10-26).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Karta z lam-alef i harakatem (np. لَا, سَلَامٌ) wysłana z AnkiDroid i obejrzana na fr965; wynik opisany w notatkach (ligatura / dwie litery / inne)
- [ ] #2 README i SCHEMA §6 zgodne z wynikiem (albo 'not verified', jeśli testu nie da się zrobić)
- [ ] #3 Gdy lam-alef z harakatem wychodzi źle: decyzja, czy dodać ligatury U+FEF5-FEFC do ArabicShaper (nowy tiket), czy zostawić jako znane ograniczenie
<!-- AC:END -->
