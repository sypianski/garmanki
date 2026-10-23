import Toybox.Lang;

// Device profile: touch-first AMOLED watches (venu2/3, vivoactive5/6) —
// two buttons on the right side, no UP/DOWN pair. Bezel ticks for UP/DOWN
// and the LINK indicator are suppressed; grading happens via touch.
module DeviceProfile {

    const HAS_UPDOWN_BUTTONS = false;
    const ANG_START = 30; // top-right action button
    // Angles below are unused when HAS_UPDOWN_BUTTONS is false; kept so
    // shared code referencing them compiles.
    const ANG_UP    = 150;
    const ANG_DOWN  = 210;
    const ANG_LINK  = 180;

    // "Companion" palette — cool grays on true black, pastel grades shared with
    // the Android app (TASK-28.2). Full 24-bit color on AMOLED.
    const BG     = 0x000000;
    const PAPER  = 0xE6EBF2; // cool off-white "ink"
    const MUTED  = 0x8C97A6; // cool gray
    const FAINT  = 0x25303B; // hairlines / idle ring
    const ACCENT = 0xA9C7E4; // pale blue (M3 container tone)
    const AGAIN  = 0xEF9A9A;
    const HARD   = 0xFFB74D; // muted orange (Anki convention)
    const GOOD   = 0x81C784;
    const EASY   = 0x64B5F6; // calm blue (Anki convention)
}
