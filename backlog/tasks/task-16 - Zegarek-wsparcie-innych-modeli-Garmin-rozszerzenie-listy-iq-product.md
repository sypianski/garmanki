---
id: TASK-16
title: 'Zegarek: wsparcie innych modeli Garmin (rozszerzenie listy iq:product)'
status: Done
assignee:
  - '@claude'
created_date: '2026-07-24 15:08'
updated_date: '2026-07-24 15:21'
labels: []
dependencies: []
ordinal: 16000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Manifest watch/manifest.xml deklaruje tylko fr965. Rozszerzyć na inne klasy zegarków z CIQ >= 4.2.1: AMOLED round (fenix8, epix2, venu3, fr265, vivoactive5...) i ocenić MIP (fr955, fenix7). Wymaga: analizy devices.xml/compiler.json, audytu hardkodów 454x454 w źródłach, ew. wariantów resources per rozdzielczość, buildów testowych per klasa urządzenia, re-uploadu do CIQ Store.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Manifest zawiera zweryfikowaną listę urządzeń (CIQ >= 4.2.1, watch-app, BLE)
- [x] #2 monkeyc builduje się bez błędów dla przedstawiciela każdej klasy rozdzielczości
- [x] #3 Layout nie zakłada 454x454 (używa getWidth/getHeight lub wariantów resources)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Profile urządzeń przez monkey.jungle sourcePath (3 profile: 5btn-amoled default, touch-amoled dla venu/vivoactive, 5btn-mip dla fenix7/fr255/fr955/enduro3/fenix8solar) — DeviceProfile.mc z kątami przycisków i paletą. 2. Proporcjonalizacja stałych px (ReviewView 44x3 i inset 15, OnboardingView 45/60/38/9/16, Theme tick 5/7). 3. Checkmark rysowany polygonem zamiast glifu ✓ w foncie numerycznym. 4. Manifest: dodanie ~35 urządzeń (8 klas rozdzielczości, CIQ>=4.2.1, bez Instinct 128KB). 5. minApiLevel zostaje 4.2.1 (obniżanie nieopłacalne: stare fenix6/instinct2 mają 96-128KB RAM). 6. Buildy weryfikacyjne monkeyc po jednym na klasę.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zaimplementowano multi-device: (1) trzy profile DeviceProfile.mc w watch/source-profile/{fivebtn-amoled,touch-amoled,fivebtn-mip} wybierane per-device w monkey.jungle przez sourcePath — kąty przycisków, flaga HAS_UPDOWN_BUTTONS, paleta (MIP: siatka 64-kolorowa). (2) Theme.mc aliasuje DeviceProfile + helper Theme.px() skalujący px z odniesienia 454. (3) Proporcjonalizacja stałych px w ReviewView/OnboardingView/Theme; ticki UP/DOWN/LINK gate'owane flagą (touch: tylko START). (4) HomeView: checkmark rysowany liniami zamiast glifu ✓. (5) SummaryView: drabinka fontów hero (THAI_HOT→HOT→MEDIUM→MILD wg chordWidth). (6) manifest.xml: 45 urządzeń w 8 klasach (AMOLED 454/416/390/360, MIP 280/260/240/218), minApiLevel bez zmian 4.2.1, bez Instinct. Buildy monkeyc OK (14): fr965, venu3, fr265s, fr165, vivoactive5, fenix7, fenix7s, fenix7x, fr255s, venu2, instinct3amoled50mm, fenixe, enduro3, marq2. Żadnego urządzenia nie usunięto z listy. Do weryfikacji w symulatorze: layout na 218-260px MIP, slajd 2 onboardingu na touch (tekst Guide2 mówi o przyciskach), czytelność palety MIP na sprzęcie.

Ikona launchera przegenerowana 60x60 -> 70x70 (downscale z androidowego 192px, ten sam motyw) — 70px to max wymagany w flocie (venu2/3); monkeyc skaluje w dół dla reszty. Po zmianie ikony przebudowano przedstawicieli wszystkich 8 klas: fr965, venu3, fr265, fr165, fr265s, fenix7x, fenix7, fenix7s, fr255s — wszystkie BUILD SUCCESSFUL.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Rozszerzono manifest z fr965 na 45 modeli (8 klas: AMOLED 454/416/390/360 + MIP 280/260/240/218; minApiLevel bez zmian 4.2.1, Instinct 128KB poza zakresem). Trzy profile urządzeń przez monkey.jungle sourcePath (fivebtn-amoled domyślny, touch-amoled venu/vivoactive bez ticków UP/DOWN, fivebtn-mip z paletą 64-kolorową). Stałe px zamienione na Theme.px() (proporcja od 454), checkmark rysowany figurą, fallback fontu hero w SummaryView, ikona 70x70. Weryfikacja: monkeyc BUILD SUCCESSFUL dla przedstawiciela każdej z 8 klas + 5 przypadków brzegowych (14 buildów agenta + 9 po zmianie ikony). Do obejrzenia w symulatorze: gęstość layoutu na MIP 218-260px, kontrast palety MIP na sprzęcie, tekst Guide2 na touch.
<!-- SECTION:FINAL_SUMMARY:END -->
