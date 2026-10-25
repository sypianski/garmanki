---
id: TASK-31
title: >-
  Research: czy da się obsłużyć arabski (kształtowanie liter, harakat, RTL) na
  zegarku?
status: Done
assignee:
  - '@claude'
created_date: '2026-10-25 10:12'
updated_date: '2026-10-25 18:29'
labels: []
dependencies: []
references:
  - DECYZJE.md
  - decyzje/PC-003-arabski-shaping-harakat-rtl-na-zegarku-czy-i-kto.md
priority: low
ordinal: 38000
---

## Description

<!-- SECTION:DESCRIPTION:BEGIN -->
Powtarzający się temat zgłoszeń (2 osoby w 2 miesiące). D6 mówi, że arabski odpada: brak fontów i RTL w Monkey C. Zanim zamkniemy temat definitywnie, sprawdzić wykonalność: kształtowanie liter (łączenie form izolowana/początkowa/środkowa/końcowa) po stronie companiona i wysyłka gotowego tekstu lub bitmap, font bitmapowy z harakatami, koszt pamięci i rozmiaru paczki. Wynik: decyzja go/no-go zapisana jako PC-NNN.
<!-- SECTION:DESCRIPTION:END -->

## Acceptance Criteria
<!-- AC:BEGIN -->
- [x] #1 Opisane co najmniej dwa warianty (pre-render do obrazka w companionie vs własny font + shaping po stronie companiona) z kosztem pamięci i BLE
- [x] #2 Sprawdzone, czy zestaw modeli z manifestu pomieści wybrany wariant (limity RAM)
- [x] #3 Decyzja go/no-go zapisana w decyzje/ (skill path-chosen) i zaktualizowany D6 w DECYZJE.md
<!-- AC:END -->

## Implementation Notes

<!-- SECTION:NOTES:BEGIN -->
Research zrobiony, wynik w PC-003 (otwarta, decyduje user). Kluczowy fakt: 20/45 modeli z manifestu (w tym fr165, fenix847mm/fenix8solar51mm = tactix 8, fr965) ma systemowy font wektorowy NotoNaskhArabic (simulator.json); getVectorFont od API 4.2.1. Shaping/bidi firmware NIEzweryfikowane — symulator na Macu segfaultuje przy rysowaniu fontem wektorowym (pusty katalog Fonts), na VPS nie startuje (brak libexpat). Rekomendacja: 1h spike na prawdziwym fr965, potem wariant C albo no-go. D6 nie zmieniony; AC#3 czeka na decyzję usera.

Weryfikacja dyrygenta 2026-10-25: git status — poza PC-003 i plikami backlogu brak zmian (DECYZJE.md, watch/, android/, SCHEMA.md nietknięte); brak danych osobowych w PC-003; NotoNaskhArabic obecny w simulator.json 20 z 45 produktów z manifestu (m.in. fr165, fr265, fr965, fenix8) — potwierdzone grepem. AC3 OTWARTE: decyzja PC-003 czeka na usera (rekomendacja: go warunkowo — 1-godzinny spike na prawdziwym fr965, potem wariant C z fontem systemowym albo no-go); po rozstrzygnięciu zamknąć PC przez pc.py choose i zaktualizować D6. Nie zweryfikowane bez zegarka: shaping i bidi w firmware, pozycja harakat, przepustowość BLE. Drobiazg: CLAUDE.md mówi o 73 wariantach sprzętowych, manifest ma 45 product id.

2026-10-25 /decidu: user wybrał spike na prawdziwym fr965 (sonda .prg z Noto Naskh Arabic); PC-003 zostaje otwarta do wyniku testu. Sonda do ponownego użycia: scratchpad artest/ (nie w repo).

2026-10-25 spike na prawdziwym fr965 (relacja usera, bez zdjęcia; sonda ar-draw-fr965.prg): na ekranie mieszczą się wszystkie linie L1-L6; poprawnie wyświetlają się L1 (Naskh, zapis logiczny), L2 (Naskh, formy prezentacyjne), L4 (lam-alef), L5 (tekst mieszany) i L6 (FONT_MEDIUM, zapis logiczny). L3 (harakat) nie wymieniona jako poprawna — do doprecyzowania. Wniosek wstępny: firmware fr965 sam łączy litery i obsługuje RTL, także wbudowanym fontem; problemem są prawdopodobnie tylko harakat (zgodne ze zgłoszeniem FR165: słowo bez znaków OK, z harakatami rozsypane). Niezweryfikowane: fr165/tactix 8, wersje firmware, przepływ end-to-end w Garmanki (companion, limity bajtów, CardStore).

Doprecyzowanie L3 (user, fr965): harakat wstawiają spacje między literami (łączenie liter się rozpada). Czyli: arabski bez harakatów działa poprawnie na fr965 (także wbudowanym FONT_MEDIUM), z harakatami nie. Pasuje do zgłoszenia FR165. Konsekwencja: README (wypchnięte 1889216), drafty maili (outlook 1686/1687) i szkice listingów w TASK-30 twierdzą, że arabski w ogóle nie działa — do poprawy; drafty nie wysyłać do czasu korekty.

2026-10-25 sonda v2 (ar2) na fr965, zdjęcie ~/dumpado/2026-10-25_13-24-34.jpg (niska rozdzielczość, ocena z powiększeń): H1 (logiczny+harakat), H3 (ZWJ), H4 (tatweel; widać kreskę, litery nadal osobno) i H6 (FONT_MEDIUM) — litery w formach izolowanych, odstępy między nimi; harakat widoczne, ale łączenie rozpada się. H5 (jeden znak na pierwszej literze): reszta wyrazu wygląda na połączoną, więc rozpada się łączenie przy literze z harakatem. H2 (formy prezentacyjne + harakat): jedyna linia z literami wyglądającymi na połączone; do potwierdzenia przez usera na żywo, czy wszystkie trzy harakat stoją na swoich miejscach. Wniosek roboczy: ZWJ i tatweel nie pomagają; wariant 'companion zamienia na formy prezentacyjne i zostawia harakat' wygląda obiecująco (koszt: 3 B UTF-8 na znak, limit w bajtach — patrz PC-003). Alternatywa tania: opcja usuwania harakatów w companionie.

2026-10-25 sonda v3 (ar3) na fr965, zdjęcia ~/dumpado/2026-10-25_19-06-4*.jpg, ekrany 1-7 (8-10 bez zdjęć). ARABSKI: ekran 1 — zwykłe wyrazy i zdanie łączą się i idą RTL poprawnie zarówno w Naskh, jak i wbudowanym FONT_LARGE; ekran 2 — harakat: zapis logiczny rozsypuje litery (Naskh i FONT_LARGE), formy prezentacyjne + harakat łączą litery (Naskh); widoczny dammatan, kasra/fatha tylko częściowo rozpoznawalne na zdjęciu; ekran 3 — tekst mieszany 'abc <ar> 123' wyświetla się jako 'abc 123 <ar>', co odpowiada algorytmowi dwukierunkowości Unicode przy bazowym kierunku od lewej (firmware go stosuje). GRECKI: ekran 4 — nowożytny z tonos poprawny (wbudowany i wektorowy Roboto); ekran 5 — polytoniczny precomposed (blok Greek Extended: ἄ ἦ ᾄ ᾳ ῳ ῥ ὕ) = kwadraciki z pytajnikiem w obu fontach; ekran 6 — znaki łączące: U+0300, U+0301, U+0308 renderują się, U+0313, U+0314, U+0342, U+0345 = kwadraciki. Wniosek: polytoniczna greka wymaga własnego fontu bitmapowego (albo podmiany glifów). ŁACINA: ekran 7 — transliteracja precomposed (ā ī ū ḥ ṣ ḍ ṭ ẓ ġ š ḫ ʿ ʾ ǧ) poprawna w obu fontach. Brak zdjęć ekranów 8 (łacina combining), 9 (rzadkie litery), 10 (stacked marks). README (lokalnie, niezacommitowane) zaktualizowane.

2026-10-25 sonda v4 (ar4) na fr965, zdjęcia ~/dumpado/2026-10-25_19-25-4*.jpg. EKRAN 11 (arabski z harakatami, duży Naskh): formy prezentacyjne + harakat = litery połączone, wszystkie trzy znaki widoczne na swoich miejscach (kasra pod pierwszą literą, fatha nad drugą, dammatan na końcu; ocena z powiększenia); zapis logiczny = litery osobno. => wariant 'companion zamienia litery na formy prezentacyjne, gdy tekst ma harakat' ZWERYFIKOWANY na fr965. EKRAN 9 (rzadkie litery łacińskie), oba fonty (wbudowany i wektorowy Roboto) mają to samo pokrycie: renderują się ł ő ű ə ŋ ế ộ; kwadraciki: ǫ (U+01EB), ǭ (U+01ED), ɛ (U+025B), ɔ (U+0254), ʃ (U+0283), ḯ (U+1E2F), ṻ (U+1E7B). EKRAN 10 (znaki łączące na jednej literze): ǽ ǿ ǘ OK; ḗ (U+1E17) i ṓ (U+1E53) kwadraciki; stos 'e+macron+acute', 'o+dot below+circumflex', 'a+ring+acute' renderuje się niepełnie (z powiększenia: widać akut, kropkę, pierścień; nie widać makronu ani cyrkumfleksu) — łączenie wielu znaków niepewne. EKRAN 8 (łacina z osobnymi znakami łączącymi): zdjęcie rozmazane, NIEZWERYFIKOWANE — do powtórki. Wnioski: (1) arabski z harakatami: go, po stronie companiona; (2) companion powinien normalizować do NFC (składać do znaków złożonych), bo znaki łączące są niepewne; (3) polytoniczna greka i rzadkie litery (IPA, część Latin Extended Additional) wymagają własnego fontu bitmapowego albo podmiany; osobna decyzja.

2026-10-25 zamknięte: PC-003 rozstrzygnięta (arabski go, kształtowanie w companionie; font własny osobno). D6 w DECYZJE.md zaktualizowany, CLAUDE.md projektu poprawiony. Tickety wynikowe: TASK-35 (companion), TASK-36 (research fontu). Niezweryfikowane: ekran 8 spike'u (łacina z osobnymi znakami łączącymi), FR165 i tactix 8.
<!-- SECTION:NOTES:END -->

## Final Summary

<!-- SECTION:FINAL_SUMMARY:BEGIN -->
Spike na fr965 pokazał, że arabski działa w firmware; harakat wymagają kształtowania w companionie (go), greka polytoniczna i rzadkie litery łacińskie wymagają własnego fontu (osobny research)
<!-- SECTION:FINAL_SUMMARY:END -->
