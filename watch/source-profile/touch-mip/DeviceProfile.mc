import Toybox.Lang;

// Device profile: touch-first watches with a 64-color MIP display
// (vivoactive4/4s, TASK-38) — button layout of touch-amoled (two buttons on
// the right, no UP/DOWN pair), palette of fivebtn-mip.
module DeviceProfile {

    const HAS_UPDOWN_BUTTONS = false;
    const ANG_START = 30; // top-right action button
    // Angles below are unused when HAS_UPDOWN_BUTTONS is false; kept so
    // shared code referencing them compiles.
    const ANG_UP    = 150;
    const ANG_DOWN  = 210;
    const ANG_LINK  = 180;

    // "Companion" palette on the 64-color grid (channels 0x00/0x55/0xAA/0xFF):
    // pastel grades keep their hue after quantization (TASK-28.2).
    const BG     = 0x000000;
    const PAPER  = 0xFFFFFF; // pure white "ink" — max MIP contrast
    const MUTED  = 0xAAAAAA;
    const FAINT  = 0x555555;
    const ACCENT = 0xAAAAFF; // pale blue
    const AGAIN  = 0xFFAAAA;
    const HARD   = 0xFFAA55; // muted orange (Anki convention)
    const GOOD   = 0xAAFFAA;
    const EASY   = 0x55AAFF; // calm blue (Anki convention)
}
