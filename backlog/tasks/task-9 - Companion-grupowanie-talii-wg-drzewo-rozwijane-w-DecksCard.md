---
id: TASK-9
title: 'Companion: grupowanie talii wg :: (drzewo rozwijane w DecksCard)'
status: Done
assignee:
  - '@claude'
created_date: '2026-07-23 08:43'
updated_date: '2026-07-23 08:49'
labels: []
dependencies: []
references:
  - android/app/src/main/kotlin/app/sypianski/garmanki/ui/MainScreen.kt
  - android/app/src/main/kotlin/app/sypianski/garmanki/anki/AnkiDroidClient.kt
  - android/app/src/main/kotlin/app/sypianski/garmanki/data/SettingsStore.kt
  - SCHEMA.md
ordinal: 9000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Anki traktuje '::' jako separator hierarchii nazw talii (np. HAKADO::vim jest podtalia HAKADO). Dzis DecksCard (android/app/src/main/kotlin/app/sypianski/garmanki/ui/MainScreen.kt) renderuje plaska liste wszystkich talii z AnkiDroidClient.listDecks() bez zadnego parsowania nazwy, a wybor jest plaskim Set<String> w SettingsStore. User chce drzewo rozwijane: nazwy talii z '::' grupowane pod wspolnym naglowkiem-rodzicem (rozwijanym/zwijanym), ale wybor nadal per-podtalia jak dzis (kazda podtalia to nadal osobny wpis liczony do limitu 8 na SCHEMA.md WatchDeck). To zmiana czysto UI/UX listy wyboru w companionie, nie zmienia kontraktu SCHEMA.md ani watch app.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Nazwy talii z '::' sa parsowane na segmenty i pogrupowane pod wspolnym naglowkiem-rodzicem (ostatni segment jako label podtalii, pelna nazwa jako klucz)
- [x] #2 Naglowek grupy jest rozwijany/zwijany (domyslny stan do ustalenia przy implementacji)
- [x] #3 Talie bez '::' w nazwie wyswietlaja sie jak dotychczas, plasko, bez grupowania
- [x] #4 Zaznaczanie/odznaczanie dziala per-podtalia identycznie jak obecnie (checkbox na kazdej podtalii), limit wybranych talii i limit 8 WatchDeck bez zmian
- [x] #5 Rodzic bez wlasnych kart wlasnych (istnieje tylko jako implikowany kontener podtalii) nie pojawia sie jako osobny checkbox do zaznaczenia
- [x] #6 SCHEMA.md i DECYZJE.md nie wymagaja zmian (protokol watch<->phone bez zmian); jesli implementacja pokaze ze jednak trzeba - zaktualizowac SCHEMA.md przed kodem watcha
<!-- AC:END -->

## Implementation Plan

<!-- SECTION:PLAN:BEGIN -->
1. W MainScreen.kt dodac prywatna strukture DeckNode (segment, fullName, deck: AnkiDeck?, children: List<DeckNode>) i funkcje buildDeckTree(decks: List<AnkiDeck>): List<DeckNode> ktora dzieli deck.name po '::' i buduje drzewo (LinkedHashMap zachowuje kolejnosc z listy AnkiDroid). Wezel bez wlasnego AnkiDeck (sam implikowany kontener) ma deck=null.
2. W DecksCard zamienic plaski decks.forEach na: val tree = remember(decks) { buildDeckTree(decks ?: emptyList()) } + rekurencyjny composable DeckTreeRows(nodes, depth, settings, expandedState, onToggleDeck).
3. Stan rozwiniecia: remember { mutableStateMapOf<String, Boolean>() } keyed po fullName, domyslnie rozwiniete (isExpanded = expanded[fullName] ?: true) - wszystko widoczne od razu jak dzis, user moze zwinac.
4. Rendering wezla z dziecmi (grupa): wiersz z strzalka tekstowa '▾'/'▸' (bez nowej zaleznosci ikon), nazwa segmentu bold, wciecie depth*20.dp; jesli node.deck != null - dodac Checkbox + DeckDueRow tak jak dla lisci (rodzic z wlasnymi kartami nadal wybieralny); klik na caly wiersz poza checkboxem toggle'uje expanded.
5. Rendering wezla-lisc (bez dzieci): identycznie jak dzisiejszy Row (Checkbox + nazwa segmentu + DeckDueRow), z wcieciem depth*20.dp. Etykieta to node.segment (ostatni czlon), nie pelna nazwa.
6. Zbudowac android (./gradlew assembleDebug lub compileDebugKotlin) zeby zlapac bledy kompilacji; testDebugUnitTest jesli sa testy dotykajace DecksCard/AnkiDeck (raczej nie, to czysty UI).
<!-- SECTION:PLAN:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Zaimplementowano DeckNode/buildDeckTree + DeckTreeRows w MainScreen.kt (grupowanie po '::', drzewo rozwijane, wybor per-podtalia bez zmian). ./gradlew assembleRelease lint testDebugUnitTest -q przeszlo bez bledow.

Refaktor: DeckNode/buildDeckTree wydzielone do anki/DeckTree.kt (bez zaleznosci od Compose), zeby dalo sie unit-testowac. Dodano DeckTreeTest.kt (5 testow): plaska talia bez '::' zostaje lisciem top-level, subtalie grupuja sie pod wspolnym parentem, implied parent (bez wlasnych kart) ma deck=null wiec nie renderuje sie jako checkbox, parent bedacy realna talia zachowuje wlasny deck obok dzieci, zagniezdzenie >1 poziom dziala. Wszystkie 5 przeszly (TEST-app.sypianski.garmanki.DeckTreeTest.xml: tests=5 failures=0 errors=0). ./gradlew assembleRelease lint testDebugUnitTest -q -> exit 0.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
DecksCard w MainScreen.kt renderuje teraz drzewo talii zbudowane z nazw '::' (logika w nowym anki/DeckTree.kt: buildDeckTree + DeckNode), z rozwijanymi/zwijanymi naglowkami grup (domyslnie rozwiniete) i wyborem per-podtalia bez zmian w SettingsStore/limicie 8 WatchDeck. Talie plaskie (bez '::') renderuja sie jak wczesniej. Implied parent bez wlasnych kart nie ma checkboxa (deck=null), realny parent-deck ma checkbox obok dzieci. Zweryfikowane 5 unit testami w DeckTreeTest.kt (5/5 pass) + ./gradlew assembleRelease lint testDebugUnitTest zielone. SCHEMA.md/DECYZJE.md bez zmian - protokol watch<->phone nietkniety.
<!-- SECTION:FINAL_SUMMARY:END -->
