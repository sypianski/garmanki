# garmanki — decyzje do podjęcia

> **PODJĘTE 2026-07-23** (przegląd UX po testach beta 0.1.0; szczegóły
> ticketów w BACKLOG.md):
> **D13:** domyślna para ocen na przyciskach: **DOWN (lewy dolny) = Good,
> START (prawy górny) = Again**; przed odsłonięciem każdy z przycisków
> oceny odsłania odpowiedź. Konsekwencja: menu „więcej" schodzi z przycisku
> START na standardowe `onMenu` (przytrzymanie UP) + długie dotknięcie.
> **D14:** UP (lewy środkowy) = Easy; Hard dostępne swipe'em w dół i w menu
> „więcej" (BACK wychodzi z sesji, LIGHT nieprzechwytywalny).
> **D15:** tap na ekranie: przed odsłonięciem = pokaż odpowiedź, po
> odsłonięciu = Good.
> **D16:** swipe po odsłonięciu: → Good, ← Again, ↑ Easy, ↓ Hard
> (przed odsłonięciem swipe odsłania). Jeśli swipe → konfliktuje
> z systemowym back na fr965 — do rozstrzygnięcia przy implementacji.
> **D17:** „ściąga za mała" z dyktowanego feedbacku = **czcionka** w
> companionie (przyjęte; redesign na neutralny M3 w GAR-08).
> Całe mapowanie konfigurowalne z companiona (GAR-04/GAR-07).

> **PODJĘTE 2026-07-22** („na razie tylko łaciński, reszta zgoda"):
> D1: A (AnkiDroid) · D2: A (BLE + ContentProvider, transport wymienny) ·
> D3: B (offline z kolejką) · D4: rekomendacja (suspend/bury natywnie,
> flaga=tag `garmanki-flag`, usuwanie=suspend+tag `garmanki-delete`) ·
> D5: A (mapowanie klawiszy + interwały w ActionMenu) ·
> **D6: A — tylko łacina/polski, bez greki w v1** ·
> D7: B (due+zapas, domyślnie 100/talia) · D8: A+C · D9: B (liczniki+dziś+streak) ·
> D10: B (fr965 na start) · D11: sideload w dev, Store później, APK przez /sforna ·
> D12: garmanki (robocza).

Klient Anki na zegarek Garmin (Connect IQ) + companion Android.
Poniżej wyniki researchu (2026-07-22) i lista decyzji. Przy każdej jest
rekomendacja — wpisz swoją decyzję w linii `➤ DECYZJA:` (albo odpowiedz
w rozmowie numerami, np. „D1: B, D2: A…").

---

## Co ustalił research (skrót)

- **Nie ma żadnego gotowego klienta Anki na Garmina** — ani open-source, ani
  w Connect IQ Store. Jedyny bliski koncept („Anki On My Wrist") to prywatny,
  nieopublikowany projekt. Budujemy od zera zasadnie.
- **Scheduler zostaje w Anki.** Zegarek nie implementuje FSRS/SM-2 — pokazuje
  kartę, 4 przyciski oceny (Znów/Trudne/Dobre/Łatwe) z etykietami interwałów
  i odsyła odpowiedź. AnkiDroid liczy resztę i sam synchronizuje z AnkiWeb.
- **AnkiDroid ma publiczne API (ContentProvider)** zaprojektowane kiedyś
  właśnie pod zegarki (AnkiDroid-Wear): lista talii z licznikami, kolejka
  due z interwałami na przyciski, odpowiadanie przez prawdziwy scheduler,
  suspend i bury. **Nie ma** endpointu do flag ani usuwania kart.
- **Ograniczenia Garmina:** apka watch-app ma ~768 KB–1 MB RAM (FR255+);
  Storage max 32 KB na klucz (karty trzeba chunkować); wiadomości BLE
  z telefonu docierają tylko do **otwartej** apki (limit praktyczny ~16 KB,
  chunking z ack); `makeWebRequest` działa przez internet telefonu
  (limit odpowiedzi ~16–25 KB bezpiecznie, HTTPS). Brak HTML — czysty tekst
  przez `TextArea`. Polskie znaki działają; **alfabety niełacińskie
  (greka, arabski, CJK) NIE** — wymagają własnych fontów bitmapowych.
- Masz już cały pipeline: szkielet watch+android w `notes_and_codes`,
  SDK 9.2.0 na VPS, klucz deweloperski, upload do Store przez `/garminiq`,
  dystrybucję APK przez `/sforna`.

---

## D1. Twoje środowisko Anki (pytanie o stan faktyczny)

Od tego zależy architektura. Czego używasz na co dzień?

- **A. AnkiDroid na Androidzie** (+ ewentualnie desktop/AnkiWeb) — companion
  może gadać z AnkiDroid lokalnie na telefonie. Najprostsza droga.
- **B. Głównie Anki desktop** — trzeba by mostka AnkiConnect (działa tylko
  gdy komputer włączony) albo serwera pośredniczącego.
- **C. Nie mam jeszcze Anki / zaczynam** — wybieramy dowolnie, rekomendowany
  AnkiDroid.

**Rekomendacja:** A (jeśli tylko masz AnkiDroid — wszystko inne się upraszcza).

➤ DECYZJA:

## D2. Architektura synchronizacji (najważniejsza decyzja)

- **A. Companion ↔ AnkiDroid ContentProvider, transport BLE (CIQ Mobile SDK).**
  Zegarek rozmawia z companionem przez Bluetooth; companion czyta/pisze do
  AnkiDroid. Zero serwera, zero kosztów, dane nie opuszczają telefonu.
  Minusy: transfer tylko przy otwartej apce na zegarku, BLE bywa kapryśne
  (znany bug podwójnych callbacków), telefon musi być w zasięgu przy syncu.
  Wzorzec sprawdzony przez AnkiDroid-Wear.
- **B. Serwer na VPS + `makeWebRequest`.** Zegarek pobiera karty i odsyła
  odpowiedzi przez HTTPS (internet telefonu, bez własnego companiona po
  stronie transferu); companion Android czyta AnkiDroid i wymienia dane
  z serwerem. Transport najstabilniejszy na platformie (wzorzec
  notes_and_codes), działa też gdy telefon daleko (byle miał internet…
  a bez telefonu i tak nie ma internetu na FR). Minusy: trzeci ruchomy
  element (serwer), dane wychodzą na VPS, więcej kodu.
- **C. Hybryda:** BLE jako główny kanał, serwer później jeśli BLE dokuczy.

**Rekomendacja:** A na start (mniej ruchomych części, prywatność, precedens
AnkiDroid-Wear); architektura ma mieć warstwę transportu wymienną, żeby
B dało się dołożyć bez przebudowy.

➤ DECYZJA:

## D3. Tryb offline (zegarek bez telefonu)

Anki nie umie przyjąć odpowiedzi „z datą wsteczną". Odtwarzanie zaległych
odpowiedzi jest OK dla kart powtórkowych (FSRS liczy w dniach), ale lekko
zaburza karty w nauce (kroki 1 min/10 min) i kolejka może się zdążyć zmienić.

- **A. Online-only:** sesja tylko z telefonem w zasięgu, odpowiedź natychmiast
  trafia do AnkiDroid. Zero zniekształceń, zero kolejki.
- **B. Offline z kolejką:** karty siedzą w Storage zegarka, odpowiedzi
  buforowane i odtwarzane przy następnym kontakcie. Można ćwiczyć na
  spacerze bez telefonu. Koszt: lekkie zaburzenie kroków minutowych,
  walidacja stanu karty przy odtwarzaniu.
- **C. B z ograniczeniem:** offline tylko karty powtórkowe, nowe/uczone
  tylko online.

**Rekomendacja:** B — to główna przewaga zegarka (sesja w kolejce, na
spacerze); zniekształcenie w praktyce pomijalne przy krótkich sesjach.

➤ DECYZJA:

## D4. Akcje na karcie na zegarku

Ocena 4 przyciskami jest oczywista. Reszta z Twojej listy:

- **Zawieszenie (suspend)** — API wspiera wprost. Brać.
- **Odłożenie (bury)** — API wspiera wprost. Brać? (tania opcja)
- **Flagowanie** — API AnkiDroid **nie ma** endpointu flag. Obejścia:
  - A. zamiast flagi dodajemy **tag** (np. `garmanki-flag`) do notatki —
    API to umie; w Anki filtrujesz po tagu;
  - B. flaga trzymana tylko w companionie jako lista „oflagowane
    z zegarka" do ręcznego przejrzenia;
  - C. rezygnujemy z flagowania w v1.
- **Usuwanie** — destrukcyjne i przez API niepewne. Obejścia:
  - A. „usuń" = suspend + tag `garmanki-delete`, kasujesz potem w Anki
    jednym filtrem (bezpieczne, odwracalne);
  - B. prawdziwe usuwanie notatki przez companion (ryzyko: usuwa
    wszystkie karty notatki, nieodwracalne z zegarka);
  - C. bez usuwania w v1.

**Rekomendacja:** suspend + bury natywnie; flagowanie = tag (A);
usuwanie = suspend+tag (A). Wszystko przez ActionMenu pod długim SELECT.

➤ DECYZJA:

## D5. Sterowanie i układ oceny (przyciski + dotyk)

- **A. Mapowanie klawiszy:** pokaż odpowiedź = SELECT; potem UP=Znów,
  DOWN=Dobre, SELECT=ActionMenu z pełną czwórką + akcje. Szybkie dwoma
  kciukami, działa na FR255 bez dotyku.
- **B. ActionMenu zawsze:** po odsłonięciu odpowiedzi wyskakuje natywne
  menu 4 ocen (działa i na przyciskach, i na dotyku). Spójne, ale
  1 klik więcej na kartę.
- **C. Strefy dotykowe** (4 ćwiartki ekranu) + fallback przyciskowy —
  najszybsze na Venu, najwięcej własnego kodu.

Dodatkowo: pokazywać przewidywane interwały („10 min / 3 dni…") przy
ocenach? (API je daje; drobny koszt miejsca na ekranie).

**Rekomendacja:** A z interwałami w ActionMenu; strefy dotykowe jako
usprawnienie w v2.

➤ DECYZJA:

## D6. Treść kart: co zegarek umie wyświetlić

Karty Anki to HTML. Companion będzie je spłaszczał do tekstu.

- Cloze `{{c1::…}}` → pokazujemy `[…]`, odpowiedź odsłania tekst — OK.
- Obrazki, audio, TTS, MathJax — **poza zasięgiem v1** (pomijamy; karta
  dostaje znacznik „⚠ media pominięte"?).
- **Kluczowe pytanie: jakie talie chcesz ćwiczyć?** Jeśli słówka z greką
  (Hunayn: grc?) albo arabskim — wbudowane fonty Garmina ich **nie
  narysują**. Własne fonty bitmapowe są możliwe (koszt pamięci, brak
  RTL dla arabskiego — arabski praktycznie odpada).
  - A. talie łacińskie/polskie — zero problemu;
  - B. potrzebna greka → dokładamy customowy font grecki (v1 lub v2?);
  - C. potrzebny arabski → uczciwie: nie na tej platformie.

**Rekomendacja:** v1 tekst łaciński+polski; jeśli greka jest w Twoich
taliach — powiedz, zaplanuję font od razu w architekturze zasobów.

➤ DECYZJA:

## D7. Zakres synchronizacji talii

W companionie zaznaczasz talie do wysyłki na zegarek. Ile kart per talia
trafia na zegarek przy syncu?

- **A. Tylko dzisiejsza kolejka due** (typowo 20–100 kart) — mało miejsca,
  zawsze aktualne.
- **B. Kolejka due + zapas** (np. due + 50 następnych) — sensowniejsze
  przy offline.
- Limit techniczny: chunk Storage <32 KB ≈ ~100–300 krótkich kart; BLE
  ~8–16 KB na wiadomość, więc sync 100 kart = kilkanaście pakietów z ack.

**Rekomendacja:** B z limitem konfigurowalnym w companionie
(domyślnie 100 kart/talia).

➤ DECYZJA:

## D8. „Skończyły się karty na dziś"

Odpowiednik „custom study / zwiększ dzisiejszy limit". API AnkiDroid
nie wystawia custom study wprost.

- **A. v1 minimal:** zegarek pokazuje „0 na dziś ✓"; w companionie przycisk
  otwierający ekran custom study w AnkiDroid (deep link) — limit zwiększasz
  tam, kolejny sync zabiera nowe karty.
- **B. Companion sam manipuluje limitami/tworzy filtered deck** — do
  zbadania, API może nie pozwolić; ryzykowne grzebanie w kolekcji.
- **C. Tryb „nadprogramowy" na zegarku:** po wyczerpaniu kolejki ćwiczysz
  dalej bez wpływu na scheduler (przegląd bez oceniania). Tanie, bez ryzyka.

**Rekomendacja:** A + C w v1; B tylko jeśli research API pokaże czystą drogę.

➤ DECYZJA:

## D9. Statystyki

- **Zegarek:** liczniki talii (nowe/uczone/powtórki), postęp sesji
  („12/34, 8 min"), zrobione dziś. Tanio, brać.
- **Companion:** wykres dzienny, streak, retencja (liczone z revlog
  AnkiDroid). Ile z tego w v1?
  - A. tylko liczniki (v1 chudy);
  - B. liczniki + zrobione dziś + streak;
  - C. pełne mini-statystyki z retencją.

**Rekomendacja:** B — streak motywuje, retencja to v2.

➤ DECYZJA:

## D10. Urządzenia docelowe

- **A. Jak notes_and_codes:** fr255/265/265s/955/965, fenix7/7s/7x, epix2,
  venu2/3 (minApiLevel 4.2.1) — masz przetestowany zestaw i pokrycie
  przycisków+dotyku.
- **B. Tylko fr965 na start** (Twój zegarek), reszta po okrzepnięciu.
- **C. A + nowe System 8** (fenix8, FR570/970…) — więcej pamięci, więcej
  testowania.

**Rekomendacja:** B do pierwszego działającego builda, potem rozszerzenie
do A przed publikacją.

➤ DECYZJA:

## D11. Dystrybucja

- **Zegarek:** sideload na czas developmentu; publikacja w Connect IQ Store
  przez `/garminiq` gdy dojrzeje? (Store = auto-aktualizacje, ale publiczna
  widoczność apki.)
- **Companion:** APK przez `/sforna` → `~/apps/` (Syncthing), bez Play
  Store — zakładam, że tak jak zawsze.

➤ DECYZJA:

## D12. Nazwa

Robocza: **garmanki** (Garmin + Anki). Zostaje? (W Store nie może sugerować
oficjalności — opis musi jasno mówić „nieoficjalny klient Anki"; „Anki" jest
znakiem towarowym Ankitects — w nazwie apki bezpieczniej coś w stylu
„Garmanki — flashcards for Anki").

➤ DECYZJA:

---

## Co NIE wymaga decyzji (przyjmuję z góry)

- Watch-app (nie widget), Monkey C, minApiLevel 4.2.1, szkielet z
  notes_and_codes; kontrakt danych w SCHEMA.md od pierwszego dnia.
- Companion: Kotlin + Compose M3 + CIQ Mobile SDK (jak notes_and_codes),
  uprawnienie `com.ichi2.anki.permission.READ_WRITE_DATABASE`.
- Karty chunkowane w Storage (<32 KB/klucz), transfer chunkowany z ack.
- UI po angielsku (decyzja usera 2026-07-22); treść kart może być polska
  (wbudowane fonty mają diakrytyki).
- Po skończonej talii i po każdej sesji companion wysyła intent
  `DO_SYNC` do AnkiDroid (rate-limit 5 min) — AnkiWeb zawsze świeże.
