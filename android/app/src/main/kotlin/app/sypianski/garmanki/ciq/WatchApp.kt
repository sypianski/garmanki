package app.sypianski.garmanki.ciq

/**
 * Watch app UUIDs — single source of truth for the Android side.
 *
 * Two variants of the same app coexist in the wild during the beta→public
 * transition (a Connect IQ beta permanently claims its manifest appID, so the
 * public release had to take a fresh UUID — see CLAUDE.md):
 *   - [WATCH_APP_UUID]      — public release (watch/manifest.xml today)
 *   - [WATCH_APP_UUID_BETA] — the earlier Connect IQ Store beta
 *
 * The companion queries/targets both so it works no matter which one the user
 * still has flashed. Public is primary: preferred when both are present, and
 * used as the default target while an install query is still in flight.
 *
 * Formatted with dashes because the CIQ SDK parses them via UUID.fromString.
 * If the watch app id changes again, update it here and nowhere else.
 */
const val WATCH_APP_UUID = "af46d10e-b5a3-4380-8259-8ff6b6c7ffed"
const val WATCH_APP_UUID_BETA = "b1b0cef8-a5b2-46af-9c8e-a2951c2d6bba"

/** Query/target order — public first so it wins when both are installed. */
val WATCH_APP_UUIDS = listOf(WATCH_APP_UUID, WATCH_APP_UUID_BETA)
