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

    // "Ink & paper" palette — full 24-bit color on AMOLED.
    const BG     = 0x000000;
    const PAPER  = 0xF5EFE3; // warm off-white "ink"
    const MUTED  = 0x8A8478; // warm gray
    const FAINT  = 0x2E2B26; // hairlines / idle ring
    const ACCENT = 0xFFB300; // amber
    const AGAIN  = 0xFF5A48;
    const HARD   = 0xFF8C42; // muted orange (Anki convention)
    const GOOD   = 0x4CD07D;
    const EASY   = 0x4FA8D8; // calm blue (Anki convention)
}
