---
id: TASK-17
title: 'Companion: wykrywaj obie apki zegarka (beta + public UUID)'
status: Done
assignee: []
created_date: '2026-07-25 14:30'
updated_date: '2026-07-25 14:34'
labels: []
dependencies: []
ordinal: 17000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Nowy companion (WATCH_APP_UUID=af46d10e…, publiczny release v1.0.0) pyta zegarek tylko o publiczny UUID przez getApplicationInfo. Użytkownicy z okresu beta mają na zegarku wariant b1b0cef8… (beta ze Store), więc getApplicationInfo zwraca onApplicationNotInstalled i companion błędnie twierdzi, że apki nie ma. Companion ma odpytywać oba UUID i akceptować którykolwiek jest zainstalowany, kierując push/send do faktycznie obecnego wariantu. Tło: przejście UUID było wymuszone (beta na trwałe zawłaszcza appID w manifeście — patrz CLAUDE.md).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 queryAppInstalled odpytuje zarówno publiczny jak i beta UUID
- [ ] #2 appInstalled=false tylko gdy zegarek zgłosił brak OBU wariantów; true gdy którykolwiek obecny; null dopóki odpowiedzi w locie
- [ ] #3 push i send kierują wiadomości do UUID faktycznie zainstalowanego na danym zegarku
- [ ] #4 registerForAppEvents nasłuchuje obu wariantów, by hello/answers docierały niezależnie od wersji na zegarku
- [ ] #5 companion buduje się i przechodzi lint + testDebugUnitTest
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Przyczyna: nowy companion (release v1.0.0) pytał getApplicationInfo tylko o publiczny UUID af46d10e…; zegarki z okresu bety mają wariant b1b0cef8… → onApplicationNotInstalled → fałszywe 'nie zainstalowano'. UUID w kodzie były zgodne (manifest==companion), więc nie był to desync, tylko przejście beta→public.

Zmiany:
- WatchApp.kt: dodano WATCH_APP_UUID_BETA i listę WATCH_APP_UUIDS (public first).
- WatchDevice: pola installedAppUuid + notInstalled; appInstalled to teraz pochodny tri-state (true gdy którykolwiek wariant obecny, false tylko gdy oba zgłoszone jako brak, null w locie) — UI/canPush/pickTarget bez zmian API.
- queryAppInstalled odpytuje oba UUID; refreshDevices rejestruje app-events dla obu.
- pickTarget zwraca (device, uuid); push/send adresują faktycznie zainstalowany wariant (fallback public gdy query w locie).

Weryfikacja: ./gradlew assembleRelease lint testDebugUnitTest — BUILD SUCCESSFUL, lint i testy przeszły. Test na fizycznym zegarku z betą pozostaje po stronie usera.
<!-- SECTION:NOTES:END -->
