---
id: PC-002
slug: etykiety-interwalow-przy-tickach-ocen-na-zegarku
data: 2026-10-23
typ: inne
proponent: user
status: przyjęta
projekt: garmanki
tytul: "Etykiety interwałów przy tickach ocen na zegarku (odstępstwo od TASK-2)"
decyzja: "Na ekranie odpowiedzi obok kolorowych ticków pojawia się etykieta następnego interwału z AnkiDroid (nt1..nt4); tryb (kolor / kolor+interwał / kolor+słowo) jest ustawieniem w companionie, domyślnie interwał."
dlaczego: "Sam kolor nie mówi, co da przycisk (czerwony i zielony obok siebie), a interwały są już na karcie i do tej pory widać je tylko w menu po przytrzymaniu UP; user wybrał ten wariant w selektorze /ux mimo oznaczenia go jako decyzji wbrew TASK-2."
tagi: [zegarek, ux, companion]
zastepuje: null
osiadlo: "SCHEMA.md §8 (cfg.tm), SettingsStore/SyncEngine/ShortcutsScreen, PhoneLink._applyCfg, ReviewView, TASK-28.6"
tiket: TASK-28.6
---
# PC-002 · Etykiety interwałów przy tickach ocen na zegarku (odstępstwo od TASK-2)

**Kontekst.** [TASK-2](../backlog/tasks/task-2%20-%20Zegarek-usunac-tekstowe-podpowiedzi-przyciskow-zostawic-ticki-na-bezelu.md) usunął tekstowe podpowiedzi przycisków, bo wychodziły poza okrąg; ticki na bezelu zostały jedynym stałym systemem. Selektor wariantów /ux z 2026-10-23 (artefakt Q5tJVEV4naf2yv7bvCXUzE) pokazał trzy tryby ticków.

**Decyzja.** Na ekranie odpowiedzi obok kolorowych ticków pojawia się etykieta następnego interwału z AnkiDroid (nt1..nt4); tryb (kolor / kolor+interwał / kolor+słowo) jest ustawieniem w companionie, domyślnie interwał.

**Rozważane alternatywy.** Sam kolor (stan sprzed zmiany) — odrzucony; kolor + słowo (again/good/easy) jako domyślne — odrzucone, bo to dokładnie to, co usunął [TASK-2](../backlog/tasks/task-2%20-%20Zegarek-usunac-tekstowe-podpowiedzi-przyciskow-zostawic-ticki-na-bezelu.md), a interwał niesie nową informację. Wybór trybu przeniesiony do ustawień companiona.

**Uzasadnienie.** Sam kolor nie mówi, co da przycisk (czerwony i zielony obok siebie), a interwały są już na karcie i do tej pory widać je tylko w menu po przytrzymaniu UP; user wybrał ten wariant w selektorze /ux mimo oznaczenia go jako decyzji wbrew [TASK-2](../backlog/tasks/task-2%20-%20Zegarek-usunac-tekstowe-podpowiedzi-przyciskow-zostawic-ticki-na-bezelu.md).

**Konsekwencje / gdzie osiadło.** SCHEMA.md §8 (cfg.tm), SettingsStore/SyncEngine/ShortcutsScreen, PhoneLink._applyCfg, ReviewView, [TASK-28.6](../backlog/tasks/task-28.6%20-%20Zegarek-i-companion-tryb-tick%C3%B3w-oceny-kolor-interwa%C5%82-s%C5%82owo.md)

## Powiązane tikiety

- [TASK-28.6](../backlog/tasks/task-28.6%20-%20Zegarek-i-companion-tryb-tick%C3%B3w-oceny-kolor-interwa%C5%82-s%C5%82owo.md)
