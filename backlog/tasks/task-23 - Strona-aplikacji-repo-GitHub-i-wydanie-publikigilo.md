---
id: TASK-23
title: 'Strona aplikacji, repo GitHub i wydanie (publikigilo)'
status: To Do
assignee: []
created_date: '2026-10-12 11:05'
updated_date: '2026-10-12 17:16'
labels:
  - premiera
dependencies:
  - TASK-20
  - TASK-21
references:
  - assets/icon-512.png
priority: medium
ordinal: 23000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Dziś pod sypian.ski/garmanki/ istnieje tylko privacy.html. Potrzebne: podstrona informacyjna aplikacji, repozytorium z README na GitHubie, wydanie binarne (companion + pakiet .iq zegarka), karta na stronie głównej sypian.ski oraz animacja demo złożona z prawdziwego UI (zrzuty w docs/screenshots). Procedura: skill publikigilo — wszystko przygotowywane prywatnie (strona za webauth, repo prywatne, wydanie jako szkic); upublicznienie wyłącznie na wyraźne polecenie usera. Licencję i model dystrybucji wybiera user (skill dopytuje). Katalog assets/ (icon-512.png, icon.svg) jest nieśledzony w git — wejdzie do commita w ramach tego zadania. Adres privacy.html musi pozostać dostępny pod dotychczasowym URL (sprawdzić, czy jest linkowany z listingu Play / data safety).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Podstrona sypian.ski/garmanki/ z linkami do Play i Connect IQ przygotowana prywatnie
- [ ] #2 privacy.html dostępne pod niezmienionym adresem
- [ ] #3 Repo GitHub z README przygotowane jako prywatne; wydanie jako szkic; licencja wybrana przez usera
- [ ] #4 Karta na stronie głównej sypian.ski przygotowana
- [ ] #5 Animacja demo z prawdziwego interfejsu aplikacji
- [ ] #6 assets/ zacommitowane do repo
- [ ] #7 Upublicznienie wykonane dopiero po jawnym poleceniu usera
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
UWAGA (2026-10-12, dirigu): repo github.com/sypianski/garmanki jest JUŻ PUBLICZNE (gh repo view: visibility PUBLIC), a nie prywatne jak zakładał opis. AC #3 (repo prywatne) trzeba przeformułować: nie tworzymy repo od zera, tylko porządkujemy istniejące publiczne (README, wydanie, licencja).

2026-10-12 19:16: licencja repo wybrana i dodana poza tym zadaniem (MIT, commit 7b7816f, szczegóły w TASK-27) — kryterium 'licencja wybrana przez usera' z AC #3 spełnione.
<!-- SECTION:NOTES:END -->
