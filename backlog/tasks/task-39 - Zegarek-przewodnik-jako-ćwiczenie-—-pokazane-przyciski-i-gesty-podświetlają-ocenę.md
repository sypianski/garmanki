---
id: TASK-39
title: >-
  Zegarek: przewodnik jako ćwiczenie — pokazane przyciski i gesty podświetlają
  ocenę
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-29 15:58'
updated_date: '2026-10-29 16:44'
labels: []
dependencies: []
priority: medium
ordinal: 48000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Zgłoszenie usera z testu 1.3.1 na fr965 (2026-10-29): w przewodniku 'nie działał dotyk i niektóre przyciski, reagował tylko prawy górny'. Przyczyna (nie regresja, błąd projektu od TASK-3): slajd 2 rysuje oceny przy przyciskach (UP=Łatwe, DOWN=Dobre, START=Powtórz), a w przewodniku UP cofa slajd; slajd 3 pokazuje gesty, a swipe w prawo/dół cofa. Na slajdzie 1 cofanie nic nie robi, więc dotyk wyglądał na martwy. Decyzja usera: tryb ćwiczenia — naciśnięcie pokazanego przycisku/gestu podświetla jego ocenę (kolor + krótka wibracja), po wypróbowaniu wszystkich slajd sam przechodzi dalej; BACK = wyjście; bez cofania slajdów.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Slajd 1 (odsłanianie): każdy przycisk, dotknięcie i przesunięcie przechodzi dalej
- [ ] #2 Slajd 2: każdy przycisk z oceną w ActionMap podświetla swoją etykietę i wibruje krótko; po wypróbowaniu wszystkich pokazanych slajd przechodzi dalej sam
- [ ] #3 Slajd 3: analogicznie dla gestów; zegarki bez ekranu dotykowego mają wyjście przyciskiem (nie utkną)
- [x] #4 Slajd 4: dowolne wejście kończy przewodnik; BACK zawsze kończy (pierwsze uruchomienie stawia flagę onboardingSeen)
- [x] #5 Teksty PL i EN mówią, by wypróbować każdy przycisk / gest
- [ ] #6 Buildy fr965 i vivoactive4 bez ostrzeżeń, lint -l 3 bez nowych wpisów; przewodnik przeklikany na fr965
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. OnboardingView: centralne input(event) z kluczami jak ActionMap/ReviewDelegate; stan _tried + timer auto-przejścia (700 ms), bez cofania. 2. Rysowanie: niewypróbowane etykiety MUTED, wypróbowane w kolorze oceny; krótka wibracja. 3. Slajd 3: brak dotyku (isTouchScreen=false) albo brak zmapowanych gestów → przyciski przechodzą dalej + podpowiedź. 4. Teksty PL/EN. 5. Buildy fr965/vivoactive4/venu2/fr255, lint, sideload na fr965.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zrobione (niezacommitowane): OnboardingView — input(event) z kluczami ActionMap; slajd 1 każde wejście = dalej; slajd 2 przyciski z oceną podświetlają słowo (MUTED → kolor oceny) + wibracja 40/60 ms, po wszystkich Timer 700 ms → dalej; slajd 3 to samo dla gestów, START zawsze dalej (dotyk może być wyłączony w ustawieniach), bez dotyku/gestów każde wejście = dalej + podpowiedź GuideNext; slajd 4 każde wejście kończy; BACK kończy; cofanie usunięte. Teksty: Guide2 zmieniony, nowe Guide3, GuideNext (EN/PL). Buildy fr965/vivoactive4/venu2/fr255 OK, 0 ostrzeżeń, lint -l 3 = 84. Build fr965 na Macu: ~/ciq-dl/prg/garmanki-t39-fr965.prg (send_garmanki_t39.py). Czeka na test na zegarku.

Test na fr965 (2026-10-29): (1) 'Easy' na slajdzie 2 przyciemnia ekran i nie da się przejść dalej — ANG_UP=150 to pozycja przycisku LIGHT (nie dociera do apki); simulator.json SDK: up=180, down=210, enter≈24-30, esc≈330 na fr965/fr265/fr255/fenix7/fenix6pro/epix2pro47mm. Błąd od TASK-16, dotyczy też ticków w powtórce. Poprawka: fivebtn-amoled i fivebtn-mip ANG_UP=180, ANG_LINK 180→90 (kolidował z UP). (2) 'gesty w ogóle nie działają' — do zbadania: simulator.json fr965 mapuje tap→onSelect, swipeUp→nextPage, swipeDown→previousPage, swipeRight(krawędź)→onBack; niepotwierdzone, czy na sprzęcie onTap/onSwipe w BehaviorDelegate w ogóle dochodzą, ani czy dotyk jest włączony w ustawieniach zegarka.

2026-10-29 user: 'gesty działają losowo, przyciski wychodzą z samouczka'. Zamiast zgadywać — sonda wejścia (scratchpad sonda/, UUID f5cf732c8722429aa191acb84f4a35e0, nazwa 'Sonda wejscia', tylko fr965): loguje na ekranie surowe onKey/onKeyPressed/onKeyReleased/onTap/onHold/onRelease/onSwipe/onFlick/onDrag(start/stop) (zwracają false) i zachowania onSelect/onNextPage/onPreviousPage/onNextMode/onPreviousMode/onMenu/onBack (żółte); BACK 2x w 2 s = wyjście. Na Macu: ~/ciq-dl/prg/sonda-fr965.prg, wgrywanie: send_any.py <plik> <NAZWA.PRG>. Hipotezy do sprawdzenia: swipe w prawo od krawędzi → onBack → finish(); slajd 3 START + slajd 4 dowolne wejście = 2 naciśnięcia do wyjścia; progi swipe vs drag/flick.

Sonda zapisuje log bez zdjęć: System.println → GARMIN/APPS/LOGS/SONDA.TXT (plik musi istnieć; utworzony pusty przez MTP, LOGS = 16777349, APPS = 16777230). Narzędzie MTP na Macu: ~/ciq-dl/mtp.py ls|mkdir|put|get|rm (źródło w scratchpadzie). Nowa sonda (z println) wgrana jako SONDA.PRG. W LOGS jest też CIQ_LOG.BAK — stary watchdog Spuri z 2026-09-25, nie garmanki.

Wynik sondy (fr965, 96 zdarzeń, ~/ciq-dl/logs/SONDA.TXT na Macu): przyciski = keyDn → (100-160 ms) zachowanie (ENTER→SELECT, UP→PREV page, DOWN→NEXT page, ESC→BACK), LIGHT nic; przytrzymanie przycisku = samo keyDn; TAP nigdy nie wywołuje onTap — przychodzi wyłącznie jako onSelect bez keyDn; przesunięcie = zawsze DRAG start/stop (+RELEASE/FLICK), onSwipe tylko 1/9 (od krawędzi), przeciągnięcie w dół czasem daje PREV page. Konsekwencja w wydanym 1.3.1 (powtórka): tap ocenia jako START (Again zamiast Good), przeciągnięcie w dół czasem jako UP (Easy). Poprawka: nowa klasa GestureDelegate (źródło: source/ui/GestureDelegate.mc) — zachowanie = przycisk tylko gdy keyDn właściwego klawisza < 600 ms wcześniej, inaczej touch (SELECT→tap, strony ignorowane); kierunek z wektora drag (próg 15% szerokości, dominująca oś), onSwipe jako fallback z deduplikacją 400 ms, onTap jako fallback z deduplikacją wobec onSelect. ReviewDelegate i OnboardingDelegate dziedziczą po GestureDelegate (onInput). Slajd 4: kończą tylko START/tap. Buildy fr965/vivoactive4/venu2/fr255/fenix6pro OK, lint 84. Niezbadane: przytrzymanie UP (onMenu) — w logu go nie było.

Hold UP nie działał nigdy (także w 1.3.1): fr965 nie wysyła onMenu; przytrzymanie = samo onKeyPressed bez onKeyReleased. GestureDelegate: KEY_UP pressed → Timer 700 ms; onKeyReleased(UP) lub onPreviousPage kasuje; timer → onHoldUp() (dedupe 1 s z onMenu dla zegarków, które go wysyłają). ReviewDelegate.onHoldUp = menu karty; HomeDelegate też przepięty na GestureDelegate (start/tap → DeckMenu, onHoldUp = ręczna synchronizacja — też wcześniej martwa na fr965). Buildy 5 urządzeń OK, lint 84.
<!-- SECTION:NOTES:END -->
