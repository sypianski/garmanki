---
id: TASK-34
title: >-
  Zegarek: OOM przy zapisie ostatniego kawałka talii (venu3, v1.2.2) —
  zmniejszyć szczyt pamięci
status: To Do
assignee: []
created_date: '2026-10-25 11:33'
labels:
  - zegarek
dependencies: []
references:
  - watch/source/model/CardStore.mc
  - watch/source/sync/PhoneLink.mc
priority: low
type: bug
ordinal: 43000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
W historii crashy Connect IQ (ERA) jest jeden raport „Out Of Memory Error”: wersja 1.2.2, venu3 (006-B4260-00), fw 17.05, 2 IX 2026, backtrace CardStore.replaceAll:57 (Storage.setValue ostatniego chunka) <- PhoneLink._assemble:214 <- _onState <- onMessage. Limit pamięci aplikacji venu3 = 768 KB, jak fr965; w symulatorze najgorszy przypadek (400 kart po 300/300 znaków) daje szczyt ~413 KB, więc nie odtworzono. Raporty ERA trafiają do Garmina tylko po synchronizacji zegarka z Garmin Connect, więc mogą być niedoszacowane. Hipoteza naprawy: zwalniać bufory (_chunks, all) przed zapisem do Storage albo zapisywać kawałki od razu po przyjściu zamiast składać cały rev w RAM.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Zmierzony szczyt pamięci w ścieżce chunki -> assemble -> replaceAll przed i po zmianie (symulator, ten sam seed: 400 kart po 300/300 znaków)
- [ ] #2 Po zmianie szczyt pamięci spada co najmniej o kilkadziesiąt KB albo opisane uzasadnienie, dlaczego się nie da
- [ ] #3 Kontrakt z SCHEMA.md (chunki, ack, idempotencja) nie jest naruszony; build monkeyc przechodzi dla venu3, fr965 i fr255
- [ ] #4 Wersja AppVersion podbita przy wydaniu wg Version Bump
<!-- AC:END -->
