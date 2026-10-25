---
id: TASK-31
title: >-
  Research: czy da się obsłużyć arabski (kształtowanie liter, harakat, RTL) na
  zegarku?
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-25 10:12'
updated_date: '2026-10-25 11:33'
labels: []
dependencies: []
references:
  - DECYZJE.md
  - decyzje/PC-003-arabski-shaping-harakat-rtl-na-zegarku-czy-i-kto.md
priority: low
ordinal: 38000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Powtarzający się temat zgłoszeń (2 osoby w 2 miesiące). D6 mówi, że arabski odpada: brak fontów i RTL w Monkey C. Zanim zamkniemy temat definitywnie, sprawdzić wykonalność: kształtowanie liter (łączenie form izolowana/początkowa/środkowa/końcowa) po stronie companiona i wysyłka gotowego tekstu lub bitmap, font bitmapowy z harakatami, koszt pamięci i rozmiaru paczki. Wynik: decyzja go/no-go zapisana jako PC-NNN.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Opisane co najmniej dwa warianty (pre-render do obrazka w companionie vs własny font + shaping po stronie companiona) z kosztem pamięci i BLE
- [x] #2 Sprawdzone, czy zestaw modeli z manifestu pomieści wybrany wariant (limity RAM)
- [ ] #3 Decyzja go/no-go zapisana w decyzje/ (skill path-chosen) i zaktualizowany D6 w DECYZJE.md
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Research zrobiony, wynik w PC-003 (otwarta, decyduje user). Kluczowy fakt: 20/45 modeli z manifestu (w tym fr165, fenix847mm/fenix8solar51mm = tactix 8, fr965) ma systemowy font wektorowy NotoNaskhArabic (simulator.json); getVectorFont od API 4.2.1. Shaping/bidi firmware NIEzweryfikowane — symulator na Macu segfaultuje przy rysowaniu fontem wektorowym (pusty katalog Fonts), na VPS nie startuje (brak libexpat). Rekomendacja: 1h spike na prawdziwym fr965, potem wariant C albo no-go. D6 nie zmieniony; AC#3 czeka na decyzję usera.

Weryfikacja dyrygenta 2026-10-25: git status — poza PC-003 i plikami backlogu brak zmian (DECYZJE.md, watch/, android/, SCHEMA.md nietknięte); brak danych osobowych w PC-003; NotoNaskhArabic obecny w simulator.json 20 z 45 produktów z manifestu (m.in. fr165, fr265, fr965, fenix8) — potwierdzone grepem. AC3 OTWARTE: decyzja PC-003 czeka na usera (rekomendacja: go warunkowo — 1-godzinny spike na prawdziwym fr965, potem wariant C z fontem systemowym albo no-go); po rozstrzygnięciu zamknąć PC przez pc.py choose i zaktualizować D6. Nie zweryfikowane bez zegarka: shaping i bidi w firmware, pozycja harakat, przepustowość BLE. Drobiazg: CLAUDE.md mówi o 73 wariantach sprzętowych, manifest ma 45 product id.

2026-10-25 /decidu: user wybrał spike na prawdziwym fr965 (sonda .prg z Noto Naskh Arabic); PC-003 zostaje otwarta do wyniku testu. Sonda do ponownego użycia: scratchpad artest/ (nie w repo).
<!-- SECTION:NOTES:END -->
