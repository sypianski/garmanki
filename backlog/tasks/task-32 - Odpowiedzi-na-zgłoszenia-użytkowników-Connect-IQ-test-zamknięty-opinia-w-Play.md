---
id: TASK-32
title: >-
  Odpowiedzi na zgłoszenia użytkowników (Connect IQ, test zamknięty, opinia w
  Play)
status: Done
assignee:
  - '@claude'
created_date: '2026-10-25 10:12'
updated_date: '2026-10-25 10:33'
labels: []
dependencies: []
priority: medium
ordinal: 39000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Niezałatwione kontakty (dane osobowe celowo pominięte — repo jest publiczne; źródła w skrzynkach himalaya): (1) użytkownik tactix 8, 15 IX — pytanie o arabski (outlook, id 71662); (2) użytkownik FR165, 27 VII — arabskie harakat się nie łączą (outlook, id 67017); (3) tester zamkniętego testu, 3 VIII — błąd przy pobraniu, zrzut ekranu w mailu (dev, id 25); (4) autor opinii 5★ w Play z 21 X (v1.0.4) — pisze, że sync bywa zawodny i część kart źle wyświetla się na zegarku, bez szczegółów. Odpowiedzi mailowe zapisywać jako drafty (postero), nie wysyłać.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Drafty odpowiedzi do Jamesa i Khalisa (arabski: jasna informacja o ograniczeniu, bez obiecywania wsparcia)
- [x] #2 Obejrzany zrzut ekranu Oleksandra, ustalona przyczyna błędu i draft odpowiedzi
- [x] #3 Szkic odpowiedzi w Play Console do José Leonarda z prośbą o szczegóły (model zegarka, przykład źle wyświetlonej karty)
- [x] #4 Żadna wiadomość ani odpowiedź w sklepie nie wysłana bez zgody usera (Publish Gate)
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
2026-10-25. AC1: drafty w outlook/Drafts id 1686 (tactix 8) i 1687 (FR165), In-Reply-To i References ustawione, nadawca konto outlook; treść: odpowiedź „nie” + wyjaśnienie, że fonty Garmina nie kształtują arabskiego, bez obietnic. AC2: zrzut ekranu = strona opt-in Google Play „App not available — A testing version of this app hasn't been published yet or isn't available for this account”; przyczyna (pewność średnia): link otwarty na innym koncie Google niż dodane do testerów albo test jeszcze nieaktywny po stronie Google; draft w dev/Drafts id 3 (przeprasza za zwłokę, wskazuje publiczny listing w Play, prosi o model telefonu, wersję Androida, komunikat i konto). AC3: odpowiedź publiczna PT (293 zn., limit 350): „Olá, José! Muito obrigado pela avaliação e pelas palavras gentis, fico muito feliz que tenha gostado! Para eu corrigir os problemas, pode me dar detalhes? Qual é o modelo do seu relógio, um exemplo de cartão exibido errado e quando a sincronização falha? Escreva para dev@sypian.ski. Obrigado!” — NIE wysłana, czeka na zgodę usera. AC4: dyrygent sprawdził foldery Sent (outlook: bez nowych od 2026-10-16; dev: pusty). Do decyzji usera przed wysyłką: czy w drafcie dla testera zostaje link do listingu w Play i zdanie o zwłoce; zdanie „store description will be updated” zakłada wykonanie TASK-30.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Drafty odpowiedzi do trzech użytkowników zapisane (nic nie wysłano) + szkic publicznej odpowiedzi PT w Play
<!-- SECTION:FINAL_SUMMARY:END -->
