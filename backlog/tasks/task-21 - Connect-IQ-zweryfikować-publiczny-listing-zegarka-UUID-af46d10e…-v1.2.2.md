---
id: TASK-21
title: 'Connect IQ: zweryfikować publiczny listing zegarka (UUID af46d10e…, v1.2.2)'
status: Done
assignee:
  - '@claude'
created_date: '2026-10-12 11:05'
updated_date: '2026-10-12 11:13'
labels:
  - premiera
dependencies: []
priority: medium
ordinal: 21000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
README i CLAUDE.md linkują wyłącznie do bety w Connect IQ Store (store id cc2be8ee-1ea5-4797-b8b0-eb7109d93855, UUID b1b0cef8…). Aktualna apka zegarka ma publiczny UUID af46d10e… (watch/manifest.xml) i wersję 1.2.2 (AppVersion w watch/resources/strings/strings.xml), ale w repo nie ma store id ani potwierdzenia statusu publicznego listingu. Przed premierą companiona trzeba wiedzieć, czy użytkownicy Play znajdą w Connect IQ Store właściwą, publiczną apkę. Companion rozpoznaje oba UUID (TASK-17), więc użytkownicy bety nie są zagrożeni. Procedura: skill garminiq (CDP na Raspberry Pi). Jeśli upload/submit nowej wersji okaże się potrzebny — to działanie zewnętrzne, wymaga OK usera i bumpu wersji jako pierwszego kroku.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Ustalony store id i status (Published / Preview / Beta) listingu z UUID af46d10e… w dashboardzie Connect IQ
- [x] #2 Wersja w store jest równa AppVersion z strings.xml lub nowsza; przy rozjeździe — opisane, co trzeba wgrać
- [x] #3 Store id i URL publicznego listingu zapisane w CLAUDE.md repo (sekcja Connect IQ Store)
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-12 (dirigu): AC1 ustalone z PUBLICZNEJ strony listingu, nie z dashboardu (CDP niedostępny: Pi bokhtisho offline ~23 dni, sshd Maca resetuje połączenia). Dowody zweryfikowane niezależnie przez dyrygenta: https://apps.garmin.com/apps/f3cb31bc-f5d9-4818-b730-4c67e98e3b7a -> HTTP 200 bez logowania, status APPROVED, betaApp false, latestExternalVersion 1.2.2, firstApprovalDate 2026-07-24; beta cc2be8ee-… anonimowo HTTP 404 (widoczna tylko dla konta dewelopera). Powiązanie store id f3cb31bc… z UUID manifestu af46d10e… potwierdzone w tabeli profili skilla garminiq (SKILL.md, linie 41-42). AC2: store 1.2.2 = AppVersion 1.2.2, nic do wgrania. AC3: CLAUDE.md repo — sekcja Connect IQ Store zaktualizowana (store id, URL, status, wersja; ostrzeżenie o UUID bety zachowane). OTWARTE (niezweryfikowane, wymaga dashboardu — zakładka Compatible Devices): agent zgłosił, że listing deklaruje ok. 25 part numberów (~23 urządzenia), a manifest w HEAD ma 43 wiersze iq:product; fileSizeInfo ma 60 device type IDs. Jeśli to prawda, użytkownicy części zegarków (np. fr265, fr955, fr165, vivoactive5, fenix7pro) nie zobaczą apki jako zgodnej. Do sprawdzenia po naprawie dostępu do Maca/Pi. Uwaga: README.md linia 20 nadal linkuje do bety, która daje 404 — zakres TASK-22.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Publiczny listing Connect IQ (store id f3cb31bc-f5d9-4818-b730-4c67e98e3b7a) jest Published/APPROVED w wersji 1.2.2 = AppVersion; store id i URL zapisane w CLAUDE.md. Weryfikacja z publicznej strony, bez dashboardu.
<!-- SECTION:FINAL_SUMMARY:END -->
