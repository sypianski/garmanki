---
id: TASK-13
title: >-
  Companion: podzial MainScreen na osobne ekrany (Home / Talie / Skroty) z
  Navigation Compose
status: Done
assignee:
  - '@claude'
created_date: '2026-07-23 09:38'
updated_date: '2026-07-23 09:47'
labels: []
dependencies: []
references:
  - android/app/src/main/kotlin/app/sypianski/garmanki/ui/MainScreen.kt
  - android/app/build.gradle.kts
  - android/gradle/libs.versions.toml
ordinal: 13000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Caly UI companiona to dzis jeden dlugi scrollowany LazyColumn w MainScreen.kt (Masthead, WatchCard, AnkiCard, DecksCard z drzewem talii, PushCard, WatchControlsCard z action-mapa/skrotami, CustomStudyFooter). User chce podzial na osobne ekrany zamiast jednego dlugiego scrolla: ekran Home (dashboard) z linkami-wierszami do ekranu Talie i ekranu Skroty/akcje, kazdy z osobnym ekranem i przyciskiem wstecz (wzorzec jak Android Settings, user wybral to explicite nad bottom-nav). Navigation Compose (androidx-navigation-compose 2.8.5) jest juz w gradle/libs.versions.toml jako alias, ale nie jest dodany do app/build.gradle.kts ani nigdzie uzyty - trzeba dodac zaleznosc i zbudowac NavHost od zera. Stan (ciqState, devices, pushStatus, engineUi, settings, decks) jest w App (Application) jako StateFlow-y, kazdy ekran moze niezaleznie collectAsState bez zmiany wlasnosci stanu.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Dodana zaleznosc androidx-navigation-compose do app/build.gradle.kts, dziala NavHost z co najmniej 3 trasami: home, decks, shortcuts
- [x] #2 Ekran Home: Masthead, WatchCard, AnkiCard, PushCard (send to watch + log), CustomStudyFooter, oraz dwa klikalne wiersze-linki 'Talie' i 'Skroty i akcje' nawigujace do odpowiednich ekranow
- [x] #3 Ekran Talie: pelna zawartosc dzisiejszego DecksCard (drzewo '::' z select-all, card limit slider) na osobnym ekranie z TopAppBar i strzalka wstecz do Home
- [x] #4 Ekran Skroty: pelna zawartosc dzisiejszego WatchControlsCard (action map, card actions, guide reset) na osobnym ekranie z TopAppBar i strzalka wstecz do Home
- [x] #5 Zaden istniejacy behavior nie jest utracony: send-to-watch, zaznaczanie talii, kaskada folderow, select-all, zapis action-mapy, guide reset, custom study deep-link - wszystko dziala identycznie jak przed refaktorem
- [x] #6 ./gradlew assembleDebug lint testDebugUnitTest przechodzi
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Dodano androidx-navigation-compose do app/build.gradle.kts. MainScreen.kt: NavHost z trasami home/decks/shortcuts, HomeScreen (Masthead, WatchCard, AnkiCard, LinkRow->Decks (tylko gdy ankiPermitted), LinkRow->Shortcuts (zawsze), PushCard (gdy ankiPermitted), CustomStudyFooter), nowy ScreenScaffold (Scaffold+TopAppBar+strzalka wstecz, reuzywany przez oba nowe ekrany). Wydzielono DecksScreen.kt (DecksCard+DeckTreeRows+DeckDueRow+DueChip, wlasny LaunchedEffect ladujacy decks) i ShortcutsScreen.kt (WatchControlsCard+MappingRow+EVENT_LABELS/CARD_ACTION_LABELS+easeLabel) jako osobne pliki/ekrany. Zweryfikowalem tresc kazdego przeniesionego composable diffem przeciw ostatniemu committed stanowi (git show HEAD) - zlapalem i naprawilem wlasny blad transkrypcji w WatchCard (zle nazwy zmiennych/warunek na appInstalled/polaczenie), reszta (AnkiCard, PushCard, DecksCard+drzewo, WatchControlsCard+MappingRow) zgadza sie 1:1. Podczas pracy user rownolegle dopisal do MainScreen.kt/Theme.kt/SettingsStore.kt/strings.xml niezalezna funkcje wyboru motywu (ThemeCard, eink/dynamic) - nie ingerowalem w to, tylko upewnilem sie ze komponuje sie z NavHost (GarmankiTheme(eink=...) na korzeniu propaguje przez LocalEink do zagniezdzonych GarmankiTheme w ScreenScaffold, komentarz w Theme.kt to potwierdza). ./gradlew assembleDebug lint testDebugUnitTest -> exit 0, DeckTreeTest nadal 8/8.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
MainScreen.kt rozbity na Navigation Compose (NavHost, 3 trasy). Home to teraz krotki dashboard (Masthead, WatchCard, AnkiCard, dwa klikalne wiersze-linki do Talii i Skrotow, PushCard, CustomStudyFooter). DecksScreen.kt i ShortcutsScreen.kt to nowe pelnoekranowe widoki z TopAppBar+strzalka wstecz (wspolny ScreenScaffold), zawierajace bez zmian dawna zawartosc DecksCard (drzewo '::' + select-all) i WatchControlsCard (action-mapa, card actions, guide reset). Zaden behavior nie zginal - zweryfikowane diffem tresci kazdego przeniesionego composable wzgledem ostatniego commita. Build/lint/testy zielone.
<!-- SECTION:FINAL_SUMMARY:END -->
