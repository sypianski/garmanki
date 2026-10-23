---
id: TASK-28.6
title: 'Zegarek i companion: tryb ticków oceny (kolor / interwał / słowo)'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-23 18:00'
updated_date: '2026-10-23 18:13'
labels:
  - watch
  - ux
dependencies: []
references:
  - decyzje/PC-002-etykiety-interwalow-przy-tickach-ocen-na-zegarku.md
parent_task_id: TASK-28
ordinal: 34000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Tick z etykietą interwału (nt1..nt4) jako domyślny. Ustawienie trybu w companionie, nowy klucz w SCHEMA.md §8, zapis na zegarku obok actionMap. Zapisać rekord decyzji (odstępstwo od TASK-2) przez /path-chosen. Sprawdzić, czy potrzebne podbicie pv.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Ustawienie w companionie zmienia tryb ticków na zegarku
- [ ] #2 SCHEMA.md §8 opisuje klucz, rekord decyzji PC zapisany
- [ ] #3 Build monkeyc (fr965, fenix7, venu3) i gradle assembleRelease testDebugUnitTest przechodzą
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zegarek: cfg.tm -> Storage tickMode (domyślnie 1); Theme.tickLabel/easeWord; ReviewView rysuje etykietę interwału (nt) lub słowo przy tickach, a pasmo odpowiedzi jest zawężone między rzędy etykiet (góra 30/150°, dół 210°), pole dodatkowe pod dolnym rzędem. Companion: SettingsStore.tickMode, SyncEngine cfg.tm, ShortcutsScreen (3 radio), strings; test SyncEngineTest (cfg.tm). SCHEMA §8 + tabela Storage; pv bez zmian (additive). Rekord decyzji PC-002. Build monkeyc fr965/fenix7/venu3 OK; gradle testDebugUnitTest + compileReleaseKotlin + lintDebug OK (exit 0). Koszt: przy włączonych etykietach długa odpowiedź dostaje niższe pasmo (~33% wys. zamiast 58%). Niesprawdzone na zegarku ani w aplikacji na telefonie.
<!-- SECTION:NOTES:END -->
