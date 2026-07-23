package app.sypianski.garmanki.ciq

/**
 * Watch app UUID — single source of truth for the Android side.
 *
 * Taken from watch/manifest.xml (`iq:application id="b1b0cef8a5b246af9c8ea2951c2d6bba"`),
 * formatted with dashes because the CIQ SDK parses it via UUID.fromString.
 * If the watch app id ever changes, update it here and nowhere else.
 */
const val WATCH_APP_UUID = "b1b0cef8-a5b2-46af-9c8e-a2951c2d6bba"
