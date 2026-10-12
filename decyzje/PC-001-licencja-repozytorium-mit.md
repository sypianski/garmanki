---
id: PC-001
slug: licencja-repozytorium-mit
data: 2026-10-12
typ: inne
proponent: wspólnie
status: przyjęta
projekt: garmanki
tytul: "Licencja repozytorium: MIT"
decyzja: "Repozytorium garmanki zostaje wydane na licencji MIT (Copyright (c) 2026 Jakub Sypiański), z osobną notką OFL-1.1 dla dołączonej czcionki Space Mono."
dlaczego: "MIT jest najprostszą licencją permisywną i nie utrudnia planowanej wersji na iOS (App Store źle znosi copyleft), a zależności (AndroidX/Kotlin Apache-2.0, AnkiDroid wyłącznie przez ContentProvider) niczego nie narzucają."
tagi: [licencja, open-source, ios]
zastepuje: null
osiadlo: "LICENSE, THIRD-PARTY-NOTICES.md, README.md (commit 7b7816f)"
---
# PC-001 · Licencja repozytorium: MIT

**Kontekst.** Repo było publiczne bez licencji (kod widoczny, ale nie open source), a wpis w wiki AnkiDroid i komentarz maintainera nazywały projekt open source; żadne z 23 publicznych repo właściciela nie miało licencji. Agent zarekomendował MIT, user wybrał MIT spośród MIT / Apache-2.0 / GPL-3.0.

**Decyzja.** Repozytorium garmanki zostaje wydane na licencji MIT (Copyright (c) 2026 Jakub Sypiański), z osobną notką OFL-1.1 dla dołączonej czcionki Space Mono.

**Rozważane alternatywy.** Apache-2.0 (klauzula patentowa, wymóg NOTICE) — odrzucone na rzecz prostoty. GPL-3.0 (copyleft, zgodna z duchem ekosystemu Anki) — odrzucone, bo komplikuje dystrybucję w App Store i przyszłe wkłady. Zmiana wpisu w wiki na 'source available' bez licencji — odrzucone na rzecz dodania prawdziwej licencji.

**Uzasadnienie.** MIT jest najprostszą licencją permisywną i nie utrudnia planowanej wersji na iOS (App Store źle znosi copyleft), a zależności (AndroidX/Kotlin Apache-2.0, AnkiDroid wyłącznie przez ContentProvider) niczego nie narzucają.

**Konsekwencje / gdzie osiadło.** LICENSE, THIRD-PARTY-NOTICES.md, README.md (commit 7b7816f)

