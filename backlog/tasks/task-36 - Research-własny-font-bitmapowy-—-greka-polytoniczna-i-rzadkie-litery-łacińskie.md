---
id: TASK-36
title: >-
  Research: własny font bitmapowy — greka polytoniczna i rzadkie litery
  łacińskie
status: To Do
assignee: []
created_date: '2026-10-25 18:28'
labels:
  - zegarek
dependencies: []
references:
  - decyzje/PC-003-arabski-shaping-harakat-rtl-na-zegarku-czy-i-kto.md
priority: low
ordinal: 45000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Spike PC-003 na fr965: wbudowane fonty (Roboto) nie mają bloku Greek Extended (polytoniczna greka: ἄ ἦ ᾄ ᾳ ῳ ῥ ὕ i znaki łączące U+0313, 0314, 0342, 0345 -> kwadraciki) ani części łaciny (ǫ ǭ ɛ ɔ ʃ ḯ ṻ ḗ ṓ -> kwadraciki). Działają: nowogrecki z tonos, transliteracja łacińska ā ī ū ḥ ṣ ḍ ṭ ẓ ġ š ḫ ǧ ʿ ʾ, ł ő ű ə ŋ ế ộ. Zbadać koszt własnego fontu bitmapowego (BMFont .fnt jako zasób Connect IQ) obejmującego polytoniczną grekę i wybrane rzadkie litery: rozmiar pliku na rozmiar punktowy, wpływ na pamięć i paczkę .iq, które modele się mieszczą, czy font ma być opcjonalny (talie z greką polytoniczną), alternatywa: podmiana znaków po stronie companiona. Wynik: rekomendacja i decyzja usera (PC-NNN).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Opisane co najmniej dwa warianty (własny font bitmapowy vs podmiana znaków w companionie) z kosztem rozmiaru i pamięci
- [ ] #2 Zmierzony lub oszacowany rozmiar fontu dla zestawu: Greek Extended + wybrane znaki łacińskie, dla co najmniej dwóch rozmiarów punktowych
- [ ] #3 Sprawdzone, które modele z manifestu pomieszczą wariant (memoryLimit)
- [ ] #4 Rekomendacja i decyzja usera zapisana w decyzje/ (skill path-chosen)
<!-- AC:END -->
