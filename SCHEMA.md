# SCHEMA.md — garmanki data contract (v1)

Single source of truth for the watch ↔ companion protocol. Change this
file first, then both clients. Decisions behind it: `DECYZJE.md`.

Watch app UUID: `b1b0cef8a5b246af9c8ea2951c2d6bba`.

## §1 Principles

- Protocol marker `p:1` on every message. Consumers MUST ignore messages
  with an unknown `p`.
- Transport v1 is Connect IQ phone-app messaging (BLE). Message shapes are
  transport-agnostic so an HTTP path can be added later (D2).
- The scheduler lives in AnkiDroid. The watch never computes intervals —
  it shows the labels it was given and reports (ease, timeTaken).
- Anki ids (card/note) are 64-bit epoch-ms values → always sent as
  **decimal strings** (`cid`, `nid`). Monkey C Number is 32-bit.
- Short keys everywhere: BLE payload budget is ~16 KB per message.

## §2 Card record

`[cid, nid, ord, deckIdx, front, back, [nt1, nt2, nt3, nt4]]`

- `cid`, `nid` — String (see §1); `ord` — Int (card template ordinal).
- `deckIdx` — Int index into the `decks` array of the same state rev.
- `front`, `back` — plain text, companion-rendered from card HTML
  (see §6), each ≤ 300 chars (companion truncates with `…`).
- `nt1..nt4` — next-interval labels for Again/Hard/Good/Easy
  (AnkiDroid `NEXT_REVIEW_TIMES`), e.g. `"<1 min"`, `"3 d"`. Always 4
  entries; empty string when unknown.

## §3 Messages, watch → phone

| msg | shape |
|---|---|
| hello | `{p:1, t:"h", rev:<Int or null>, pend:<Int>}` |
| answers | `{p:1, t:"a", batch:<Int>, ans:[…], act:[…]}` |
| state ack | `{p:1, ack:<rev>, ok:<Bool>, err?:<String>}` |

- `hello` — sent on app start and on manual sync. `rev` = state revision
  currently held (null = none), `pend` = queued answer+action count.
- `answers.ans` — `[[cid, nid, ord, ease, timeMs, epochSec]…]`;
  `ease` 1–4 (Again/Hard/Good/Easy), `timeMs` capped at 60000,
  `epochSec` = watch Unix time at answer (informational only; AnkiDroid
  applies at replay time — see D3 in DECYZJE.md).
- `answers.act` — `[[cid, nid, ord, action]…]`,
  `action ∈ {"susp","bury","flag","del"}`.
- `batch` — watch-side monotonic Int. The companion MUST treat a batch id
  it has already applied as a duplicate: re-ack, do not re-apply.

## §4 Messages, phone → watch

State push, chunked; every chunk:
`{p:1, t:"s", rev:<Int>, seq:<k 1..n>, of:<n>, cards:[<§2>…]}`

Chunk `seq:1` additionally carries:
- `decks: [[deckIdx, deckId, name, nNew, nLrn, nRev]…]` — `deckId` as
  String, counts are today's due counts; max 8 decks.
- `stats: [doneToday, streak]` — companion-computed (§7).
- `cfg: {…}` — optional watch-UI configuration (§8). Omitted `cfg`
  changes nothing on the watch (config is sticky).

Rules (mirrors notes_and_codes PhoneSync): a fresh `seq:1` discards any
partial set; incomplete sets expire after 60 s; watch acks the assembled
rev with `{p:1, ack:rev, ok:…}` and replaces its whole store
transactionally (no incremental merge in v1).

Answers ack: `{p:1, t:"aa", batch:<Int>, ok:<Bool>, applied:<Int>,
stale:<Int>, err?:<String>}`. On `ok:true` the watch drops queued rows
with id ≤ batch. After a successful replay the companion SHOULD push a
fresh state rev.

## §5 Watch storage (Application.Storage)

| key | value |
|---|---|
| `rev` | Int state revision |
| `decks` | decks array as received (§4) |
| `stats` | `[doneToday, streak]` |
| `cards_<i>` | Array of §2 records, re-chunked ≤ ~24 KB per key |
| `cards_n` | Int — number of `cards_<i>` keys |
| `q` | Array of queued rows: `["a",…ans row]` / `["x",…act row]` |
| `batch` | Int — last batch id assigned |
| `actionMap` | Dictionary — event→ease overrides, §8 `am` stored verbatim |
| `cardActions` | Array — enabled card actions, §8 `ca` stored verbatim |
| `guideReset` | Int — last §8 `gr` value seen |
| `onboardingSeen` | Bool — first-run guide already shown (GAR-03) |

Caps: 8 decks × 100 cards default (companion setting), hard cap 400
cards total on watch; storage value limit is 32 KB/key.

## §6 Companion HTML → text

Applied to AnkiDroid's rendered question/answer HTML, in order:
strip `[sound:…]` and `[anki:tts]…[/anki:tts]`; drop `<img…>` and
`<style>`/`<script>` blocks with their content; `<br>` and `<div>`/`<p>`
(opening or closing) → `\n`; strip remaining tags; decode entities; collapse blank lines and
trim. Cloze prompts arrive already rendered as `[...]` — kept as-is.
If `front` ends up empty → skip the card and count it as `skipped`
(surfaced in companion UI, never sent to the watch).

## §7 Companion behavior

- AnkiDroid via `FlashCardsContract` (permission
  `com.ichi2.anki.permission.READ_WRITE_DATABASE`): decks + due queue
  (`schedule` with `deckID`/`limit` selection) + per-card Q/A render.
- Replay: ease 1–4 → `EASE_1..EASE_4` with `TIME_TAKEN`; a card that
  fails to apply counts as `stale` (best effort, no retry).
- Actions: `susp` → `SUSPEND=1`; `bury` → `BURY=1`;
  `flag` → append note tag `garmanki-flag`;
  `del` → `SUSPEND=1` + append tag `garmanki-delete` (soft delete —
  the user hard-deletes in Anki by tag filter; D4).
- After replay: broadcast `com.ichi2.anki.DO_SYNC` (AnkiDroid rate-limits
  to 1/5 min).
- Stats: companion keeps a local log of applied answers; `doneToday` =
  answers applied today (watch-originated only), `streak` = consecutive
  days with ≥1 applied answer. AnkiDroid's revlog is not exposed via the
  API — these are companion-local numbers by design.
- Push triggers: manual button; auto-response to `hello` while the app
  process is alive. (BLE cannot reach a closed watch app; the watch
  cannot be reached push-style — v1 accepts this.)

## §8 Watch UI configuration (GAR-07)

Rides on chunk `seq:1` of a state push as `cfg`:

`cfg: {am: {<event>: <ease>…}, ca: [<action>…], gr: <Int>}`

- `am` — action map (GAR-04/05). Keys: `"up"`, `"down"`, `"start"`,
  `"tap"`, `"swipeR"`, `"swipeL"`, `"swipeU"`, `"swipeD"`. Values:
  ease 1–4 (Again/Hard/Good/Easy) or **0 = no grade on this event**.
  Omitted keys keep the watch defaults (DECYZJE.md D13–D16). Stored on
  the watch verbatim and overlaid onto the defaults, so a partial map
  from an older companion stays valid.
- `ca` — enabled card actions, subset of `["susp","bury","flag","del"]`;
  the review action menu lists only these. Omitted = all four.
- `gr` — guide-reset counter, companion-monotonic. The watch remembers
  the last value seen; a *different* value clears `onboardingSeen`
  (guide replays on next launch). The first value ever seen is stored
  without replaying the guide.

All of `am`/`ca`/`gr` are individually optional. A watch build that
predates this section ignores `cfg` entirely; the companion always
sends the full current config (small, ~150 chars). The companion MUST
keep at least one event mapped to Again and one to Good (validated in
its settings UI).
