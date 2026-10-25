---
id: PC-003
slug: arabski-shaping-harakat-rtl-na-zegarku-czy-i-kto
data: 2026-10-25
typ: inne
proponent: agent
status: do-rozstrzygnięcia
projekt: garmanki
tytul: "Arabski (shaping, harakat, RTL) na zegarku: czy i którym wariantem"
decyzja: "(nierozstrzygnięta)"
dlaczego: ""
decyduje: "Jakub (user)"
rekomendacja: "Go warunkowo: 1-godzinny spike na prawdziwym fr965 (sonda .prg rysuje Unicode logiczny i formy prezentacyjne fontem FONT_MEDIUM i NotoNaskhArabicRegular), potem C; jeśli glifów brak lub harakat nieczytelne — D"
zalezy: "TASK-31 AC#3, D6 w DECYZJE.md, spike na zegarku fr965, ewentualne tikety implementacyjne"
tagi: [zegarek, companion, i18n, arabski, rtl]
zastepuje: null
osiadlo: ""
tiket: TASK-31
---
# PC-003 · Arabski (shaping, harakat, RTL) na zegarku: czy i którym wariantem

**Status.** do rozstrzygnięcia (otwarta 2026-10-25); decyduje: Jakub (user). Research: [TASK-31](../backlog/tasks/task-31%20-%20Research-czy-da-si%C4%99-obs%C5%82u%C5%BCy%C4%87-arabski-kszta%C5%82towanie-liter-harakat-RTL-na-zegarku.md). D6 w DECYZJE.md pozostaje bez zmian do czasu rozstrzygnięcia.

**Pytanie / kontekst.** Dwa zgłoszenia w dwa miesiące (Forerunner 165 i tactix 8; nauka arabskiego z harakatami). D6 zakłada, że arabski „praktycznie odpada”: wbudowane fonty Garmina go nie narysują, nie ma kształtowania liter ani RTL. Research sprawdził, czy to nadal prawda i ile kosztowałoby obejście.

Oznaczenia: **FAKT** = sprawdzone w SDK 9.2.0, plikach urządzeń (`~/.Garmin/ConnectIQ/Devices/<id>/{compiler,simulator}.json`) albo w kodzie; **SZACUNEK** = policzone w tym researchu; **HIPOTEZA** = do sprawdzenia na prawdziwym zegarku.

## Stan obecny (FAKT, kod i SCHEMA.md)

- Tekst karty rysuje `WatchUi.TextArea` (ReviewView.mc): lista fontów wbudowanych `FONT_LARGE…FONT_XTINY`, TextArea sama łamie wiersze i dobiera rozmiar. Łamanie idzie od początku napisu, od lewej do prawej.
- Karta: `front`/`back` ≤ 300 znaków (§2); push stanu w chunkach (`MAX_CHUNK_CHARS = 10 000`, `MAX_CARDS_PER_CHUNK = 60`, StatePayload.kt); zegarek składa **cały** rev w RAM i podmienia store w jednej transakcji (§4); Storage ≤ 32 KB na klucz, twardy limit 400 kart (§5).
- Najgorszy zmierzony szczyt RAM przy syncu: ~413 KB (symulator, 400 kart po 300/300 znaków łacińskich).

## Nowe fakty, które zmieniają D6

1. **FAKT:** wszystkie 45 produktów z `watch/manifest.xml` ma w `compiler.json` język `ara` z fontSet `ww`, czyli firmware umie rysować arabski we własnym UI.
2. **FAKT:** 20 z 45 produktów ma w `simulator.json` systemowy font wektorowy **Noto Naskh Arabic** (`NotoNaskhArabicRegular`/`Bold`). Lista potwierdzona też w `doc/docs/Device_Reference/fr965.html`. Te modele to fr165, fr165m, fr265, fr265s, fr955, fr965, fr970, fr57042mm, fr57047mm, venu3, venu3s, venu441mm, venu445mm, vivoactive6, fenix843mm, fenix847mm, fenix8solar47mm, fenix8solar51mm, fenixe, enduro3. **Oba zgłaszające się modele są na liście** (tactix 8 = `fenix847mm` / `fenix8solar51mm`, `displayName` w compiler.json).
3. **FAKT:** `Graphics.getVectorFont({:face, :size})` istnieje od API 4.2.1; `TextArea` przyjmuje `Graphics.FontType`, a do tego typu należy `VectorFont` (Graphics.html, TextArea.html). Font dostajemy bez kosztu pamięci po stronie paczki .prg.
4. **FAKT (symulator macOS, SDK 9.2.0, fr965):** `getVectorFont(NotoNaskhArabicRegular, 44)` zwraca obiekt. Każdy pomiar albo rysunek tym fontem, nawet tekstu „abc”, kończy się segfaultem symulatora. Katalog `~/Library/Application Support/Garmin/ConnectIQ/Fonts` na Macu jest pusty, więc najpewniej fonty urządzeń nie są pobrane w SDK Managerze (**HIPOTEZA** co do przyczyny). Na VPS symulator nie startuje, bo brakuje `libexpat`. **Symulator nic nie rozstrzygnął.**
5. **NIEZWERYFIKOWANE (najważniejsza niewiadoma):** czy rasteryzer firmware przy `drawText`/`TextArea` sam robi shaping (formy łączone) i bidi. Jeśli tak, wystarczy wysłać zwykły Unicode. Jeśli nie, companion musi wysyłać formy prezentacyjne (U+FE70–FEFC) w kolejności wizualnej. Dokumentacja SDK nie mówi o shapingu ani RTL nic (przeszukane Graphics/Dc/VectorFont/TextArea). Nie wiadomo też, czy wbudowane `FONT_*` w fontSet `ww` zawierają glify arabskie.

## Warianty

### A. Pre-render do bitmapy w companionie

Android rysuje stronę karty przez `StaticLayout`, który robi pełny shaping (Minikin + HarfBuzz) i bidi. Wynik to 1-bit (albo 2-bit AA), przycięty do bounding boxa i wysłany jako Base64 String. Zegarek dekoduje go przez `StringUtil.convertEncodedString` (FAKT, API 3.0.0) do ByteArray, zapisuje w Storage (ByteArray jest dozwolonym ValueType, FAKT) i rysuje biegami `fillRectangle` do `BufferedBitmap` raz na odsłonę karty.

- Rozmiar surowy pasma tekstu 0,8w × 0,6h (SZACUNEK): 454 px → 363×272 = 12,3 KB 1-bit / 24,7 KB 2-bit; 416 → 10,4 KB; 390 → 9,1 KB; 260 → 4,1 KB; 218 → 2,9 KB. Krótka karta słówkowa (1–3 słowa) po przycięciu ma ~1–3 KB na stronę, pełna 300-znakowa do ~12 KB (454 px, 1-bit). Base64 dokłada ×4/3. Deflate odpada: Monkey C nie ma inflate. RLE na 1-bitowym piśmie arabskim daje mało.
- BLE (SZACUNEK; przepustowości phone→watch nie mierzono w tym repo, a zakres 1–4 KB/s to **HIPOTEZA**): 100 kart arabskich × ~3–4 KB ≈ 300–400 KB, czyli ~1,5–7 min syncu przy otwartej apce (dziś sekundy). Najgorszy przypadek (400 kart, dwie pełne strony, 454 px): ~13 MB. To fizycznie niewykonalne i przekracza `appStorageCapacity` = 10 MB (FAKT, simulator.json).
- Wymagana zmiana protokołu: obrazków nie da się składać w RAM razem z rev, jak dziś (§4). Potrzebny osobny kanał (klucz = cid + hash treści, zapis do Storage od razu po odbiorze, GC osieroconych kluczy), a w `hello` rozdzielczość ekranu, więc `pv` 2.
- RAM zegarka (SZACUNEK): sync zostaje tekstowy, plus bufor wiadomości ≤ 16 KB, bieżący obraz ≤ 12–25 KB i BufferedBitmap ~12 KB. Daje to ~+40 KB do szczytu.
- Minusy: brak doboru rozmiaru czcionki na zegarku (sztywny raster per rozdzielczość), kolory i AA ograniczone paletą, długi sync. Plusy: idealna typografia (shaping, harakat, bidi, mieszany tekst), działa na wszystkich 45 modelach.
- Wariant A2 (**HIPOTEZA**): `Communications.makeImageRequest` z lokalnego serwera HTTP w companionie. Firmware sam dekoduje PNG, a `BitmapResource` można trzymać w Storage (FAKT, od 3.0.0). Nie wiadomo, czy GCM wpuści `http://localhost`. To nie skraca czasu BLE.

### B. Własny font bitmapowy + shaping w companionie

Font BMFont `.fnt` jako zasób (FAKT: Resources.html; domyślnie 1-bit, `antialias` opcjonalnie, `filter` ogranicza glify, jeden rozmiar na zasób; per-rozdzielczość przez kwalifikatory katalogów `resources-*`). Źródło: Noto Naskh Arabic (licencja OFL). Companion zamienia litery na formy prezentacyjne (ICU `ArabicShaping` albo mała własna tabela łączenia, ~300 linii; czy `android.icu` udostępnia ArabicShaping, to **HIPOTEZA**). Bidi robi `java.text.Bidi` (FAKT, Android/Java). Zegarek dostaje słowa w kolejności logicznej, sam łamie wiersze przez `getTextWidthInPixels` i każdy wiersz układa od prawej. TextArea się do tego nie nadaje: przy napisie odwróconym w całości łamanie postawiłoby koniec zdania w pierwszym wierszu.

- Glify (SZACUNEK): Presentation Forms-B ~141, harakat i tanwin ~12, cyfry arabsko-indyjskie i interpunkcja ~14, razem ≈ 170. Litery perskie i urdu (Forms-A) to dodatkowe ~30.
- Rozmiar fontu (SZACUNEK): ~100 B na glif w 1-bit przy wysokości ~40 px, czyli ~18 KB na rozmiar. Trzy rozmiary dają ~40 KB na ekranach 454 px i ~12 KB na 218–260 px. AA to ×~4 (~150 KB / ~45 KB). Paczka .iq rośnie o ~45 × 12–40 KB ≈ 0,5–1,8 MB, bo każdy produkt dostaje własny .prg.
- Harakat: bez GPOS. Znak łączący jako glif o zerowym `xadvance` i ujemnym `xoffset` nakłada się na poprzednią literę „średnio” (to, czy kompilator CIQ zachowuje te pola, jest **HIPOTEZĄ**). Lepiej, ale drożej: 2–3 warianty PUA każdego znaku (litera wąska / średnia / szeroka), które companion wybiera wg litery bazowej, co daje ~+30 glifów. Pełne pre-kompozycje forma × haraka to ~2000 glifów, odrzucone.
- RAM (SZACUNEK): font w graphics pool na API ≥ 4 (`FontReference`, **HIPOTEZA**, że pool, a nie heap; pool: fr255 1 MB, fr265/fr965 3 MB, FAKT simulator.json). Na fenix6 Pro (API 3.4, bez poolu) font ląduje w heapie, ale te modele mają 1280 KB. **Haczyk:** formy prezentacyjne zajmują 3 B UTF-8 na znak, łaciński tekst 1 B. Przy limicie 300 znaków i 400 kartach szczyt urósłby z ~413 KB do ~890 KB. Potrzebny limit w **bajtach**, a nie znakach (np. 300 B ≈ 100 znaków arabskich), albo mniej kart dla talii arabskich.
- BLE: 100 kart × ~100 znaków × 3 B ≈ 30 KB, sekundy jak dziś.

### C. Systemowy font wektorowy Noto Naskh Arabic (tylko 20 modeli z fontem)

Architektura jak w B (shaping i bidi w companionie, łamanie wierszy na zegarku), ale font daje firmware: `getVectorFont(["NotoNaskhArabicRegular"], size)`. Jeśli spike pokaże, że firmware sam robi shaping i bidi, wariant upraszcza się do wysyłania zwykłego Unicode (2 B/znak).

- Pamięć paczki i RAM na font: ~0 (SZACUNEK). Ten sam limit bajtowy co w B. BLE jak w B.
- Zasięg: 20/45 produktów, w tym oba zgłaszające się modele i fr965 usera. Pozostałe 25 (fr255*, fenix7*, epix2*, venu2*, vivoactive5, marq2*, instinct3amoled*, fenix6*pro) dostają komunikat „arabski nieobsługiwany na tym modelu”; ewentualnie B później.
- Harakat: Noto Naskh ma znaki łączące z zerowym advance, więc bez GPOS położą się „średnio” nad literą. Czy font zawiera Presentation Forms-B i czy pozycja harakat jest akceptowalna, to **HIPOTEZA** do sprawdzenia na zegarku.

### D. No-go (status quo D6)

Arabski oficjalnie nieobsługiwany. Listing i README już to mówią ([TASK-30](../backlog/tasks/task-30%20-%20Listingi-i-README-jawnie-opisa%C4%87-ograniczenie-j%C4%99zykowe-tylko-%C5%82aci%C5%84ski-polski-bez-arabskiego-i-RTL.md)). Koszt zero, ale temat wraca w zgłoszeniach.

## Pamięć: modele z manifestu (FAKT: `appTypes[watchApp].memoryLimit`)

| grupa | memoryLimit | modele | font wektorowy arabski |
|---|---|---|---|
| fr255, fr255s | 512 KB | 2 | nie |
| reszta API 5.x/6.x | 768 KB | 40 | 20 tak / 20 nie |
| fenix6pro, 6spro, 6xpro (API 3.4) | 1280 KB | 3 | nie (VectorFont od 4.2.1) |

Uwaga: fr255m i fr255sm (wersje music) mają 768 KB, a nie 512 KB.

Zapas przy szczycie ~413 KB (SZACUNEK):

| wariant | 512 KB | 768 KB | 1280 KB |
|---|---|---|---|
| dziś | ~99 KB | ~355 KB | ~867 KB |
| A (+~40 KB, sync bez obrazków w RAM) | ~59 KB | ~315 KB | ~827 KB |
| B 1-bit, 3 rozmiary, limit bajtowy | ~87 KB (font 12 KB, 260 px) | ~315–343 KB | ~855 KB |
| B AA | ~54 KB | ~205 KB | ~717 KB |
| B / C **bez** limitu bajtowego | nie mieści się (~890 KB) | nie mieści się | ~390 KB |
| C | n/d | ~355 KB (tylko 20 modeli) | n/d |

## Koszt pracy (SZACUNEK)

- A: companion ~3 dni (render, raster, protokół obrazków, cache); zegarek ~3 dni (odbiór, Storage, dekoder biegów, integracja z ReviewView); testy na 3 rozdzielczościach plus pomiar BLE. Razem ~6–8 dni.
- B: companion ~2 dni (shaping, bidi, limit bajtowy, flaga talii); generowanie fontów per rozdzielczość ~1 dzień; zegarek ~2 dni (łamacz wierszy RTL, wybór fontu); testy harakat. Razem ~5–6 dni.
- C: jak B bez fontów, ~3–4 dni. Do tego **spike na zegarku ~1 h** przed decyzją.

## Rekomendacja

**Go warunkowo: najpierw 1-godzinny spike na prawdziwym fr965 (jest w grupie z Noto Naskh Arabic), potem C.** Spike polega na wgraniu sondy (sideload .prg), która rysuje (1) Unicode logiczny z harakatami, (2) formy prezentacyjne w kolejności wizualnej, (3) tekst mieszany z cyframi i łaciną, fontem `FONT_MEDIUM` i `NotoNaskhArabicRegular`, i na zrobieniu zdjęcia ekranu.

- Firmware robi shaping i bidi: C w wersji minimalnej (zwykły Unicode plus limit bajtowy).
- Glify są, shapingu nie ma: C z shapingiem w companionie i łamaczem RTL na zegarku.
- Glifów nie ma, font nie działa albo harakat są nieczytelne: **no-go** (D) i aktualizacja D6. A zostaje w zapasie tylko na wypadek, gdyby user zaakceptował minutowe synce.

Odrzucone jako pierwszy krok: **A**, bo koszt BLE rośnie ~100× i trzeba przerobić protokół (§4 transakcyjny rev); **B**, bo najwięcej pracy, utrzymanie fontów per rozdzielczość i najsłabsze harakat, a jego jedyna przewaga nad C to zasięg na 25 modelach, o które nikt nie prosił.

**Ryzyka / czego nie zweryfikowano bez zegarka:** shaping i bidi w firmware; obecność Presentation Forms-B w systemowym Noto Naskh; pozycja harakat bez GPOS; realna przepustowość BLE phone→watch; czy font wektorowy i BMFont siedzą w graphics pool, czy w heapie; zachowanie `xadvance=0` w kompilatorze fontów CIQ; dostępność `ArabicShaping` w `android.icu`.

**Co zależy od rozstrzygnięcia.** Domknięcie [TASK-31](../backlog/tasks/task-31%20-%20Research-czy-da-si%C4%99-obs%C5%82u%C5%BCy%C4%87-arabski-kszta%C5%82towanie-liter-harakat-RTL-na-zegarku.md) (AC#3), aktualizacja D6 w DECYZJE.md, ewentualne tikety implementacyjne (companion: shaping, limit bajtowy, flaga talii; zegarek: łamacz RTL; SCHEMA.md: `pv` / §2 limity), treść listingu i README ([TASK-30](../backlog/tasks/task-30%20-%20Listingi-i-README-jawnie-opisa%C4%87-ograniczenie-j%C4%99zykowe-tylko-%C5%82aci%C5%84ski-polski-bez-arabskiego-i-RTL.md)).

## Powiązane tikiety

- [TASK-31](../backlog/tasks/task-31%20-%20Research-czy-da-si%C4%99-obs%C5%82u%C5%BCy%C4%87-arabski-kszta%C5%82towanie-liter-harakat-RTL-na-zegarku.md)
