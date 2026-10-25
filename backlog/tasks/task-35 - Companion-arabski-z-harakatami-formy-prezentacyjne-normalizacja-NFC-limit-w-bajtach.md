---
id: TASK-35
title: >-
  Companion: arabski z harakatami (formy prezentacyjne) + normalizacja NFC +
  limit w bajtach
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-25 18:28'
updated_date: '2026-10-25 20:22'
labels:
  - companion
dependencies: []
references:
  - SCHEMA.md
  - decyzje/PC-003-arabski-shaping-harakat-rtl-na-zegarku-czy-i-kto.md
priority: medium
ordinal: 44000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Wynik spike'u PC-003 na fr965: zwykły arabski łączy się i idzie RTL poprawnie (nawet wbudowanym fontem), ale harakat rozbijają łączenie liter. Formy prezentacyjne (Arabic Presentation Forms-B) z harakatami wyglądają poprawnie. Znaki łączące w łacinie i polytonicznej grece są niepewne (część kwadraciki, stosy niepełne). Companion ma: (1) gdy tekst karty zawiera harakat, zamienić litery arabskie na formy prezentacyjne, zostawiając harakat po literze bazowej; zwykły arabski zostaje bez zmian; (2) normalizować tekst do NFC (składać znaki łączące do znaków złożonych), gdy zegarek ma glif; (3) liczyć limit pola w bajtach UTF-8 zamiast w znakach, bo formy prezentacyjne to 3 B na znak (dziś 300 znaków; szczyt RAM rośnie ok. 413 -> 890 KB przy 3 B/znak i limicie 300 znaków). Sprawdzone tylko na Forerunner 965; FR165 i tactix 8 niezweryfikowane.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Karta z harakatami wysłana z AnkiDroid wyświetla się na zegarku z połączonymi literami i harakatami na swoich miejscach (zdjęcie z fr965)
- [x] #2 Karta z arabskim bez harakatów wyświetla się bez zmian względem dziś
- [x] #3 Znaki łączące w łacinie (np. a + makron) są składane do znaków złożonych po stronie companiona; test jednostkowy z kilkoma przykładami
- [x] #4 Limit długości pola liczony w bajtach UTF-8; SCHEMA.md §2 i §5 oraz StatePayload.kt zaktualizowane, protocolVersion podbity jeśli kontrakt się zmienia
- [ ] #5 Szczyt pamięci zegarka z kartami o maksymalnej długości w bajtach zmierzony i mieści się w limicie najsłabszych wspieranych modeli
- [ ] #6 Wersja podbita przy wydaniu wg Version Bump
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. ArabicShaper (Forms-B, harakat -> kształtowanie całego pola). 2. HtmlToText: NFC -> 300 zn. -> shaping -> limit 600 B UTF-8. 3. StatePayload: budżet paczki w bajtach. 4. SCHEMA §2/§6. 5. Testy JVM; AC1 wymaga zdjęcia z fr965 (APK na telefon).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Spike: łacina z osobnymi znakami łączącymi (ekran 8) pozostaje niezweryfikowana (nieostre zdjęcie, ponowne odpuszczone 2026-10-25) — dlatego companion normalizuje do NFC.

Wdrożone 2026-10-25: ArabicShaper.kt, HtmlToText (NFC, shaping, MAX_BYTES=600), StatePayload (budżet paczki w bajtach UTF-8, MAX_CHUNK_BYTES), SCHEMA §2/§6. testDebugUnitTest 47/0 błędów, assembleDebug i lint OK. AC2: test 'plain arabic without harakat is untouched'. AC3: test NFC (a+U+0304 itd.). AC4: limit 300 zn. ORAZ 600 B; protocolVersion BEZ zmiany (watch nie parsuje tekstu, kształt rekordu §2 ten sam). AC5 (rozumowanie, nie pomiar): najgorszy przypadek pola = 600 B = dzisiejszy najgorszy (300 zn. x 2 B polskie/arabskie), więc RAM zegarka nie rośnie; pomiar na urządzeniu zostaje. AC1 czeka na zdjęcie z fr965 (potrzebny APK z tą zmianą na telefonie). Bez ligatur lam-alef i bez liter perskich spoza U+0621-064A.
<!-- SECTION:NOTES:END -->
