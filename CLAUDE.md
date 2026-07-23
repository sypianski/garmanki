# CLAUDE.md — garmanki

Unofficial Anki client for Garmin watches: Connect IQ watch-app +
Android companion that bridges to **AnkiDroid** (ContentProvider API)
over Connect IQ BLE messaging. The scheduler stays in AnkiDroid — the
watch only shows cards and reports (ease, timeTaken).

**The data contract lives in [SCHEMA.md](SCHEMA.md) — read it before
touching either client.** Product decisions: [DECYZJE.md](DECYZJE.md).
Architecture patterns are deliberately mirrored from
`~/utensili/notes_and_codes` (chunked BLE push + ack, rev-based state).

## Layout

| Dir | What | Stack |
|---|---|---|
| `watch/` | Connect IQ app (v1 target: fr965; minApiLevel 4.2.1) | Monkey C, SDK 9.x |
| `android/` | companion — AnkiDroid bridge + BLE push | Kotlin, Compose M3, CIQ Mobile SDK 2.4.0 |

## Commands

```bash
# watch (SDK not on PATH)
export PATH="$HOME/connectiq-sdk/connectiq-sdk-lin-9.2.0/bin:$PATH"
cd watch && monkeyc -f monkey.jungle -d fr965 -o bin/garmanki.prg -y developer_key

# android
cd android && ./gradlew assembleRelease lint testDebugUnitTest
```

## Connect IQ Store (beta)

- Beta 0.1.0 uploaded 2026-07-22: store app id
  `cc2be8ee-1ea5-4797-b8b0-eb7109d93855`
  (https://apps.garmin.com/apps/cc2be8ee-1ea5-4797-b8b0-eb7109d93855) —
  visible only to the developer account, "Beta Apps" section of the
  dashboard; install via the Connect IQ app on the phone.
- ⚠️ Garmin: a beta upload permanently claims the manifest appID. Public
  release later = NEW UUID in `watch/manifest.xml` + `android/…/ciq/WatchApp.kt`.

## Gotchas

- `watch/developer_key` and `android/keystore.properties` are gitignored.
- Watch app UUID `b1b0cef8a5b246af9c8ea2951c2d6bba` appears in
  `watch/manifest.xml` AND `android/…/ciq/*` — keep in sync.
- Anki ids travel as **strings** (64-bit epoch-ms; Monkey C Number is
  32-bit) — never parse them to Int on the watch.
- v1 is Latin/Polish text only (D6): built-in Garmin fonts, no Greek/
  Arabic/CJK. Companion must strip HTML per SCHEMA.md §6.
- Deleting from the watch is a soft delete (suspend + tag
  `garmanki-delete`) — never hard-delete notes from the companion.

<!-- BACKLOG.MD GUIDELINES START -->
<!-- backlog.md-instructions-version: 1.48.0 -->
<CRITICAL_INSTRUCTION>

## Backlog.md Workflow

This project uses Backlog.md for task and project management.

**For every user request in this project, run `backlog instructions overview` before answering or taking action.**

Use the overview to decide whether to search, read, create, or update Backlog tasks.

Before task lifecycle actions, read the matching detailed guide:
- `backlog instructions task-creation` before creating or splitting tasks
- `backlog instructions task-execution` before planning, changing status or assignee, adding a plan or implementation notes, or implementing task work
- `backlog instructions task-finalization` before checking acceptance criteria, writing final summaries, or moving tasks to terminal statuses

Use `backlog <command> --help` before running unfamiliar commands. Help shows options, fields, and examples.

Do not edit Backlog task, draft, document, decision, or milestone markdown files directly. Use the `backlog` CLI so metadata, relationships, and history stay consistent.

</CRITICAL_INSTRUCTION>
<!-- BACKLOG.MD GUIDELINES END -->
