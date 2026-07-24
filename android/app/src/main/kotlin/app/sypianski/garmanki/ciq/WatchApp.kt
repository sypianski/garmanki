package app.sypianski.garmanki.ciq

/**
 * Watch app UUID — single source of truth for the Android side.
 *
 * Taken from watch/manifest.xml (`iq:application id="af46d10eb5a3438082598ff6b6c7ffed"`),
 * formatted with dashes because the CIQ SDK parses it via UUID.fromString.
 * If the watch app id ever changes, update it here and nowhere else.
 */
const val WATCH_APP_UUID = "af46d10e-b5a3-4380-8259-8ff6b6c7ffed"
