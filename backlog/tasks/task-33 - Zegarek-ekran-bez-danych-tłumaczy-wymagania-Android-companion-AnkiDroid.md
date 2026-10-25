---
id: TASK-33
title: 'Zegarek: ekran bez danych tłumaczy wymagania (Android, companion, AnkiDroid)'
status: To Do
assignee: []
created_date: '2026-10-25 10:53'
labels:
  - zegarek
dependencies: []
references:
  - watch/source/ui/HomeView.mc
  - watch/resources/strings/strings.xml
priority: medium
ordinal: 40000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Komunikat HomeNoData („No data — open the Garmanki app on your phone”) zakłada, że użytkownik ma już companiona. Nie mówi o wymaganiu telefonu z Androidem, aplikacji Garmanki i AnkiDroid, więc osoba z iPhonem albo bez companiona czyta to jako „apka nie działa” (hipoteza z TASK-29: opinia 1★ na fr265). Zegarek nie może sprawdzić, czy companion jest zainstalowany (Connect IQ tego nie udostępnia), ale może jasno wskazać wymaganie, a w kroku 2 rozróżnić telefon niepołączony od braku talii.
<!-- SECTION:DESCRIPTION:END -->
