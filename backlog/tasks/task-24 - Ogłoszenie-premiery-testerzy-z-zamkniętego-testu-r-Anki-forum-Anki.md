---
id: TASK-24
title: 'Ogłoszenie premiery: testerzy z zamkniętego testu, r/Anki, forum Anki'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-12 11:05'
updated_date: '2026-10-12 17:11'
labels:
  - premiera
dependencies:
  - TASK-20
  - TASK-21
priority: low
ordinal: 24000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Test zamknięty prowadzony był z testerami z r/Anki i forum Anki (feedback mailem, installed audience 9). Po premierze warto im podziękować i ogłosić publiczną dostępność. Wysyłka jest działaniem zewnętrznym: Claude przygotowuje szkic, user akceptuje, dopiero wtedy publikacja (Publish Gate — każdy post/mail osobne OK). Przed postem na r/Anki sprawdzić regulamin subreddita dotyczący autopromocji; forum: forums.ankiweb.net. Treść po angielsku, z linkami do Play i Connect IQ oraz zrzutami z docs/screenshots.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Szkic ogłoszenia (EN) na r/Anki i forum Anki z linkami do Play i Connect IQ; regulamin r/Anki sprawdzony
- [x] #2 Szkic wiadomości do testerów z zamkniętego testu (podziękowanie + informacja o publicznej wersji)
- [ ] #3 Każda wiadomość pokazana userowi i wysłana/opublikowana dopiero po jego jawnym OK
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-12: user chce ogłosić dostępność w Play na r/Anki (NIE na r/Garmin — tam jest tylko wątek z prośbą o Fenix 6: https://www.reddit.com/r/Garmin/comments/1voczqe/comment/pekn1of/). Weryfikacja stanu: Play listing dev.sypianski.garmanki publiczny (HTTP 200, Install, 1.0.4/vc14), Connect IQ 1.2.3 na żywo. Regulamin r/Anki NIEsprawdzony: Reddit blokuje odczyt z VPS (HTTP 403 na /about/rules.json) i z retumilo (old.reddit.com -> strona Blocked); nie obchodzono. User musi sam przeczytać reguły r/Anki (sidebar, autopromocja) albo wkleić je do rozmowy. Post wymaga jawnego OK usera przed publikacją (Publish Gate).

2026-10-12: user opublikował nowy post na r/Anki (flair Add-ons): https://www.reddit.com/r/Anki/comments/1x42t0d/garmanki_is_now_on_google_play_review_your/ (stary post: .../1v59o80/anki_for_garmin/). Nie zweryfikowano treści ani stanu posta — Reddit blokuje odczyt z VPS i retumilo. Pozostało: dopisek UPDATE w starym poście r/Anki i w wątku r/Garmin (1voczqe) z linkiem do nowego posta; wiadomość do testerów z zamkniętego testu (AC #2); regulamin r/Anki nadal niesprawdzony przez Claude (AC #1).

2026-10-12 16:23 (dirigu): szkice w scratchpad/announcement-drafts.md (A r/Anki, B forum Anki, C mail do testerów, D regulaminy). Weryfikacja dyrygenta: wszystkie linki (Play, Connect IQ, GitHub, forum guidelines, forum t/70300) HTTP 200; 3 zrzuty z docs/screenshots istnieją; ograniczenia/wymagania/'tested only on Forerunner 965'/Connect IQ 4.2.1+ zgodne z README; kategorie forum zweryfikowane w site.json (22 AnkiDroid (Android) bez rodzica; 11 Add-ons i 12 Development pod Anki 21; precedens #70300 w kat. 12). POPRAWKI dyrygenta: (1) 'delete a card' -> miękkie usunięcie (suspend + tag garmanki-delete, nigdy hard-delete); (2) 'went public today' -> companion dziś w Play, zegarek w Connect IQ od 2026-07-24; (3) mail C: usunięte niepotwierdzone zdania o wpływie feedbacku i 'fixes we discussed'. NIEZROBIONE: regulamin r/Anki — Reddit blokuje także przeglądarkę retumilo ('blocked by network security', wymaga logowania); lista kontrolna 5 punktów w sekcji D, user sprawdza ręcznie na https://www.reddit.com/r/Anki/about/rules/ + wybiera flair przy publikacji. AC1 zostaje otwarte (szkice A/B gotowe, regulamin niezweryfikowany), AC3 czeka na jawne OK usera per wiadomość. Proponowana kolejność: mail do testerów -> forum Anki (jeden temat, bez cross-postu) -> r/Anki po sprawdzeniu regulaminu.

2026-10-12 19:11: r/Anki — panel boczny strony subreddita (odczyt bierny z już otwartej karty retumilo): REGULAMIN = 6 reguł (1 Keep your posts on-topic, 2 Be polite and kind, 3 Do not spam, 4 Do not remove answered questions, 5 No research for other products/services, 6 No low-effort AI content); żadna nie zakazuje autopromocji wprost (szczegółowe treści reguł nie były rozwinięte); flairy: Discussion, Resources, Add-ons, Experiences, Development, Fluff. User opublikował post sam (flair Add-ons) ok. 3 h przed 2026-10-12 ~19:00. Odpowiedzi na komentarze (David, Smooth-Put5476, moofish2842) wkleja user ręcznie — wybrał tę opcję zamiast publikacji przez retumilo (ryzyko oznaczenia konta jako automatyzowanego przez Reddit). Mail do testerów i post na forum Anki: NIE wysłane (czekają na OK, AC3 otwarte).
<!-- SECTION:NOTES:END -->
