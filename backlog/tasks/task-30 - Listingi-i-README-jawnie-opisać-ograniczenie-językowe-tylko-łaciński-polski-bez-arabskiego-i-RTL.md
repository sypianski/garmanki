---
id: TASK-30
title: >-
  Listingi i README: jawnie opisać ograniczenie językowe (tylko łaciński/polski,
  bez arabskiego i RTL)
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-25 10:12'
updated_date: '2026-10-25 11:33'
labels: []
dependencies: []
references:
  - DECYZJE.md
priority: medium
ordinal: 37000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Dwóch użytkowników Connect IQ (27 VII: Forerunner 165; 15 IX: tactix 8) zgłosiło lub zapytało o arabski z harakatami — wbudowane fonty Garmina nie obsługują arabskiego ani RTL (DECYZJE.md D6). Listing nie ostrzega o tym, więc ludzie uczący się arabskiego instalują apkę i trafiają na błąd. Trzeba to napisać wprost tam, gdzie decydują się o instalacji.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Opis w Connect IQ Store zawiera zdanie o obsłudze tylko tekstu łacińskiego/polskiego i braku arabskiego, greki, CJK
- [ ] #2 Opis w Google Play zawiera to samo ograniczenie (en-US i pl-PL, jeśli oba są w listingu)
- [ ] #3 README i strona aplikacji (TASK-23) mają sekcję Limitations spójną z listingami
- [ ] #4 Zmiany w listingach wysłane dopiero po potwierdzeniu usera (Publish Gate)
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-25: README poprawiony i zacommitowany lokalnie (nie wypchnięty). Szkice do listingów (CZEKAJĄ na zgodę usera, AC1/AC2/AC4): CIQ_EN (334 zn.): Limitations & requirements: shows Latin-script and Polish text only. Arabic (including harakat and right-to-left), Greek and CJK are not supported, because Garmin's built-in fonts cannot draw them. Requires an Android phone with the Garmanki companion app and AnkiDroid; there is no iOS version. Without them the watch shows no cards. | PLAY_EN (203 zn.): Latin-script and Polish text only; no Arabic, Greek or CJK (Garmin fonts can't draw them). Needs an Android phone with the Garmanki companion and AnkiDroid; no iOS. Without them the watch shows no cards. | PLAY_PL (205 zn.): Tylko tekst łaciński i polski; bez arabskiego, greki i CJK (fonty Garmina ich nie rysują). Wymaga telefonu z Androidem, aplikacji Garmanki i AnkiDroid; brak wersji na iOS. Bez nich zegarek nie pokaże kart. AC3 otwarte: brak strony aplikacji (TASK-23).

Powiązane: TASK-33 (ekran bez danych na zegarku) — komunikat na zegarku i listing powinny mówić to samo o wymaganiach (Android + companion + AnkiDroid).

2026-10-25 /decidu: user zdecydował czekać z wysyłką tekstów do sklepów (Play i Connect IQ) na wynik spike'u arabskiego (TASK-31 / PC-003). Szkice w notatkach zostają jako gotowe. README już wypchnięty (origin/master 1889216).
<!-- SECTION:NOTES:END -->
