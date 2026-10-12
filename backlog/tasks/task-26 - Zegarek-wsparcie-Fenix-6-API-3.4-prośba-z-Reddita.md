---
id: TASK-26
title: 'Zegarek: wsparcie Fenix 6 (API 3.4, prośba z Reddita)'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-12 11:09'
updated_date: '2026-10-12 12:57'
labels: []
dependencies: []
references:
  - 'https://developer.garmin.com/connect-iq/compatible-devices/'
ordinal: 26000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Użytkownik z Fenix 6 nie może zainstalować apki ze Store. Rodzina fenix6 to Connect IQ API 3.4, a manifest wymaga minApiLevel 4.2.1, więc całą rodzinę wyklucza. Dane z SDK 9.2.0 (compiler.json): watchApp memoryLimit = 1 310 720 B dla fenix6pro / fenix6spro / fenix6xpro oraz 131 072 B (128 KB) dla fenix6 / fenix6s. TASK-16 odrzucił Fenix 6 ze względu na RAM bez pomiaru — dotyczy to tylko wariantów 128 KB. Próbny build w scratchpadzie (minApiLevel 3.4.0 + 5 wariantów, profil fivebtn-mip): wszystkie kompilują się, eksport monkeyc -e = 81/81 urządzeń OK. Ryzyko 128 KB: PhoneLink buforuje w pamięci cały push stanu (do MAX_CARDS=400 kart) zanim zapisze go do Storage, a CardStore.cardsForDeck ładuje chunk do ~24 KB — wymaga pomiaru lub limitu kart per urządzenie (zmiana kontraktu SCHEMA.md).
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [ ] #1 Manifest zawiera fenix6pro, fenix6spro, fenix6xpro; minApiLevel obniżone do 3.4.0; monkey.jungle mapuje je na profil fivebtn-mip
- [ ] #2 Eksport monkeyc -e (jak dist/garmanki.iq) buduje się bez błędów dla wszystkich urządzeń z manifestu
- [ ] #3 Obniżenie minApiLevel nie zmienia działania na dotychczasowych urządzeniach (sprawdzone na fr965 przed publikacją)
- [ ] #4 Decyzja co do fenix6 / fenix6s (128 KB) zapisana w DECYZJE.md: wspierane z limitem kart albo świadomie wykluczone, z uzasadnieniem
- [ ] #5 Layout i przyciski sprawdzone na Fenix 6 (symulator albo tester z prawdziwym zegarkiem)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. manifest.xml: minApiLevel 4.2.1 -> 3.4.0 (rodzina fenix6 = API 3.4); dodać fenix6pro (MIP 260), fenix6spro (MIP 240), fenix6xpro (MIP 280) do istniejących klas rozdzielczości — layout już przetestowany na fenix7/7s/7x. 2. monkey.jungle: te trzy urządzenia -> source-profile/fivebtn-mip (5 przycisków, paleta 64 kolory, jak fenix7). 3. NIE dodawać fenix6 / fenix6s (128 KB RAM) — czekają na decyzję użytkownika (limit kart per urządzenie wymaga zmiany SCHEMA.md i companiona). 4. Weryfikacja: monkeyc -e na realnym repo (wyjście w scratchpadzie) + build pojedynczych urządzeń; regresja fr965 sprawdzana przez użytkownika na zegarku. 5. Bez bumpu wersji i bez publikacji — to osobny krok z Publish Gate.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zrobione (niezacommitowane): watch/manifest.xml — minApiLevel 3.4.0 + fenix6pro (MIP 260), fenix6spro (MIP 240), fenix6xpro (MIP 280); watch/monkey.jungle — te 3 na fivebtn-mip. Weryfikacja (jednorazowy klucz DER, wyjście w scratchpadzie): monkeyc -e = 73/73 urządzeń OK (było 61/61; +12 = suma partNumbers trzech modeli w SDK); pojedyncze buildy fenix6pro/6spro/6xpro/fr965 OK. Pomiary SDK 9.2.0: fenix6* = API 3.4; watchApp memoryLimit pro/spro/xpro = 1 310 720 B, fenix6/fenix6s = 131 072 B. Build dev .prg = 138 KB, ale build z -r (release) = ~32 KB — reszta to dane debugowe, więc sam kod mieści się w 128 KB; realne ryzyko to sterta przy odbiorze pushu (PhoneLink._chunks trzyma do 400 kart naraz). Uwaga niezależna od taska: monkeyc -r dla pojedynczego urządzenia fr965/fenix7 kończy się critical error także na niezmienionym repo (eksport -e działa). NIE zrobione: fenix6/fenix6s (czeka na decyzję), test w symulatorze/na zegarku, bump wersji, publikacja.

Bump 1.2.2 -> 1.2.3 (strings.xml AppVersion). Eksport watch/dist/garmanki.iq podpisany developer_key: 73/73 urządzeń, 1 704 650 B. NIE wgrane do Store (czeka na zgodę, Publish Gate). Pomiar pamięci fenix6/fenix6s: NIEMOŻLIWY na VPS — maszyna aarch64, simulator to binarka x86-64 bez 29 bibliotek (GTK3, WebKit2GTK, glib…), Mac nie odpowiada (ssh: connection reset). Zamiast pomiaru przygotowano sondę watch/bin/memtest/{memtest-fenix6,fenix6s,fenix6pro}.prg + MemTest.mc (tylko skompilowana, NIGDY nie uruchomiona): odtwarza PhoneLink._chunks -> replaceAll -> cardsForDeck dla 100/200/400 kart (30/100 znaków) oraz 100/400 kart (300/300 = max z SCHEMA §2) i drukuje usedMemory/freeMemory; OOM jest fatalny, więc przed krokiem wypisuje TRY. Uruchomienie: simulator + monkeydo memtest-fenix6.prg fenix6. Oszacowanie bez pomiaru (tylko payload, bez narzutu obiektów): karta typowa ~178 B, 400 kart ~71 KB; karta max ~658 B, 400 kart ~263 KB. Przy ~34 KB kodu i limicie 131 072 B zostaje < 100 KB sterty, więc 400 kart na 128 KB się NIE zmieści; ~100 kart prawdopodobnie tak. Wniosek roboczy: warianty 128 KB wymagają limitu kart zależnego od urządzenia albo zapisu chunków do Storage w locie.

Upload 1.2.3 do Store ZABLOKOWANY (2026-10-12, po zgodzie usera): Pi bokhtisho offline w tailnecie od 23 dni (ssh timeout), Mac jakubs-macbook-air online (tailscale ping OK, direct), ale sshd zrywa połączenie przed wymianą nagłówka (kex_exchange_identification: Connection reset by peer). Gotowe do wgrania: watch/dist/garmanki.iq (sha256 23036b9e82a1516f40e1f8e05cac7c949d3082fcc119f572d8fab8472754ab8a, 1 704 650 B, wersja 1.2.3, 73 urządzenia). Whats New: Added support for Fenix 6 Pro, 6S Pro and 6X Pro (including Sapphire, Solar and Dual Power editions). Fenix 6 / 6S without Pro are not supported yet. Commit lokalny wstrzymany do udanego uploadu (release commit ma opisywać faktyczne wydanie).

Upload 1.2.3 przez retumilo (2026-10-12): user zalogował się w retumilo (karta EC3C9978 na /developer/.../update, Latest app version 1.2.2). Krok attach+wersja+Upload and publish ZABLOKOWANY przez klasyfikator uprawnień Claude Code (auto mode) — bez podanego powodu; nie obchodzono. Do zrobienia: albo user wgrywa ręcznie (plik na Macu: /tmp/garmanki.iq, wersja 1.2.3, What new jak wyżej, Submit), albo dodaje regułę uprawnień Bash i prosi o ponowienie. Po wgraniu: zweryfikować wersję na publicznym listingu, poprawić CLAUDE.md (store version), zrobić lokalny commit release(watch): v1.2.3, zamknąć tunel 19222.

WYSŁANE 2026-10-12 (po zgodzie usera, przez retumilo/CDP): 1.2.3 do Store (garmanki.iq 1 704 650 B, 73 urządzenia, wersja 1.2.3, What new jak wyżej). Formularz: Latest app version 1.2.2 -> Upload and publish -> krok 2 (kategoria bez zmian, 202) -> Submit -> przekierowanie na apps.garmin.com/apps/f3cb31bc-... (sygnał sukcesu wg skilla garminiq). Niepotwierdzone niezależnie: status weryfikacji — panel deweloperski nie wyrenderował się (spinner), a publiczna strona nadal pokazuje 1.2.2 do zatwierdzenia przez Garmin (~3 dni). Do sprawdzenia za kilka dni: czy publiczny listing pokazuje 1.2.3 i czy Compatible Devices zawiera fenix6pro/6spro/6xpro. Zmiana CLAUDE.md: minApiLevel 3.4.0, wpis o 1.2.3. Task zostaje In Progress: AC #3 (fr965), #4 (128 KB), #5 (layout na Fenix 6) czekają.
<!-- SECTION:NOTES:END -->
