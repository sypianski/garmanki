---
id: TASK-38
title: 'Zegarek: wsparcie Vivoactive 4 / 4S (Connect IQ 3.3)'
status: In Progress
assignee:
  - '@claude'
created_date: '2026-10-27 17:18'
updated_date: '2026-10-29 18:39'
labels: []
dependencies: []
priority: medium
ordinal: 47000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Prośba użytkownika (e-mail Erik Weineisen, icloud, 2026-10-27, 'Garmin app for vivoactive 4??'). Vivoactive 4 i 4S mają Connect IQ API 3.3.1/3.3.6 (SDK 9.2.0 compiler.json), a manifest wymaga minApiLevel 3.4.0 (obniżone w TASK-26 dla fenix6 Pro) — rodzina jest wykluczona. RAM nie jest problemem: watchApp memoryLimit = 1 048 576 B. Ekran MIP dotykowy (260 px / 218 px) z dwoma przyciskami — żaden istniejący profil (touch-amoled, fivebtn-mip) nie łączy dotyku z paletą MIP.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Próbny build (minApiLevel 3.3.0 + vivoactive4/4s) pokazuje, czy kod używa API spoza 3.3; wynik zapisany w notatkach
- [x] #2 Manifest zawiera vivoactive4 i vivoactive4s; minApiLevel obniżone do 3.3.0 (jeśli build przechodzi) i monkey.jungle mapuje je na profil dotykowy z paletą MIP
- [x] #3 Eksport monkeyc -e buduje się bez błędów dla wszystkich urządzeń z manifestu
- [x] #4 Obniżenie minApiLevel nie zmienia działania na dotychczasowych urządzeniach (fr965 sprawdzony przed publikacją)
- [x] #5 Odpowiedź dla zgłaszającego przygotowana jako szkic w wątku (icloud / Drafts)
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. Próbny build w scratchpadzie: kopia watch/, minApiLevel 3.3.0, vivoactive4/4s na touch-amoled — sprawdzić błędy API. 2. Zależnie od wyniku: profil touch-mip albo obejście brakujących API. 3. Dopiero potem zmiany w repo, eksport -e, szkic odpowiedzi.
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Próbny build (scratchpad, minApiLevel 3.3.0, vivoactive4/4s na touch-amoled): BUILD SUCCESSFUL, lint -l 3 identyczny jak fr965/fenix6pro (84 = 84) — ALE monkeyc NIE sprawdza minimalnego API symboli. Przegląd 'Since' w doc SDK 9.2.0 dla wszystkich użytych symboli Toybox: 4 API wymagają 3.4.0 — WatchUi.ActionMenu/ActionMenuItem/ActionMenuDelegate + showActionMenu (ReviewView.mc:402-428, 612), WatchUi.showToast (ReviewView.mc:483, DeckMenu.mc:53), WatchUi.getCurrentView (OnboardingView.mc:258, ReviewView.mc:532). Na API 3.3 to byłby crash 'Symbol Not Found' w runtime. Reszta (Menu2 3.0, TextArea 3.1, Storage 2.4, drawArc/fillPolygon 1.x) OK. RAM vivoactive4 = 1 MB. Potrzebne: fallbacki (has :ActionMenu → Menu2; has :showToast → zamiennik; getCurrentView → referencja widoku w delegacie) + profil touch z paletą MIP.

Zrobione (niezacommitowane): source/ui/Compat.mc — showMenu/toast + ReviewMenuDelegate w dwóch wariantach (:modern = ActionMenu/showToast, :legacy = Menu2 z popView przed handleMenu + ToastView z Timerem 1,5 s); getCurrentView usunięte — ReviewDelegate/OnboardingDelegate dostają widok w konstruktorze (GarmankiApp.mc, DeckMenu.mc). Nowy profil source-profile/touch-mip (przyciski touch-amoled + paleta fivebtn-mip). monkey.jungle: base.excludeAnnotations = legacy; vivoactive4/4s → touch-mip + excludeAnnotations = modern. manifest: minApiLevel 3.3.0 + vivoactive4, vivoactive4s. Weryfikacja: buildy vivoactive4/4s/fr965/venu2/fenix6pro OK, 0 ostrzeżeń; ToastView tylko w debug.xml vivoactive4 (wycięcie działa); lint -l 3 = 84 na fr965 i vivoactive4 (jak przed zmianą); eksport -e 77/77 (scratchpad, dist/ nietknięte). NIE zrobione: test w symulatorze/na zegarku (VPS aarch64 bez symulatora), bump wersji, README/CLAUDE.md (liczby urządzeń, minApiLevel) — przy wydaniu.

Wydanie 1.3.1 (2026-10-27, za zgodą usera, bez testu runtime — świadomie): AppVersion 1.3.0 → 1.3.1; eksport -e 77/77, dist/garmanki.iq sha256 84a4a1e393a9100d0b0973626ccea1a5b0026ba54697ef907caf512e4620a273, 2 047 955 B. Upload przez /update (Chrome na Macu, CDP 9227; Pi offline): Latest app version było 1.3.0, PUT ciq-developerservices → 204, redirect na Manage Your App. What's New: 'Added support for vívoactive 4 and vívoactive 4S.' Status Preview — czeka na zatwierdzenie Garmina (do 3 dni). AC #4 (fr965) nadal niesprawdzone.

2026-10-29: 1.3.1 opublikowane w Store (publiczne API: status APPROVED, latestExternalVersion 1.3.1, part numbers vivoactive4/4s w compatibleDevicePartNumbers). Szkic do Erika w icloud/Drafts (id 8676) — niewysłany. AC #4 (fr965) wciąż niesprawdzone — teraz już na wersji produkcyjnej.

2026-10-29: fr965 przeklikany na buildzie 1.3.2-rc (TASK-39/40/41/43/44) — działa.
<!-- SECTION:NOTES:END -->
