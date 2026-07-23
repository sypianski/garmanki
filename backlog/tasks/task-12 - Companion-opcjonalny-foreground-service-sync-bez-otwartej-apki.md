---
id: TASK-12
title: 'Companion: opcjonalny foreground service (sync bez otwartej apki)'
status: To Do
assignee: []
created_date: '2026-07-23 09:33'
labels: []
dependencies: []
ordinal: 12000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Obecnie CiqManager inicjalizuje się dopiero gdy użytkownik otworzy MainActivity — proces może być zabity przez system i zegarek nie może wtedy dostarczyć odpowiedzi ani odebrać nowych kart. Opcjonalny foreground service (z trwałą notyfikacją) trzymałby połączenie CIQ żywe w tle i odpowiadał na hello z zegarka bez interakcji użytkownika. Użytkownik włącza/wyłącza serwis w ustawieniach companiona. Szczególnie ważne przy trybie offline (D3): po powrocie z treningu odpowiedzi powinny zsynchronizować się automatycznie gdy zegarek i telefon znajdą się w zasięgu BLE.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Ustawienie 'Run in background' w companonie włącza/wyłącza foreground service
- [ ] #2 Po włączeniu: service startuje przy starcie systemu (BOOT_COMPLETED) i po otwarciu apki
- [ ] #3 Service rejestruje nasłuch CIQ i odpowiada na hello/answers niezależnie od UI
- [ ] #4 Trwała notyfikacja pokazuje status (np. 'Garmanki — waiting for watch' / 'Syncing...')
- [ ] #5 Po wyłączeniu ustawienia service zatrzymuje się natychmiast, notyfikacja znika
- [ ] #6 Gdy apka jest otwarta — service i UI nie duplikują logiki (jeden SyncEngine, dwa konsumenci)
- [ ] #7 Bez włączonego ustawienia zachowanie identyczne jak przed zmianą (brak regresji)
<!-- AC:END -->
