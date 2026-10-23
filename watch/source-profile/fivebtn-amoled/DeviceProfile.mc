import Toybox.Lang;

// Device profile: five-button watches with an AMOLED display (fr9xx, fenix8
// AMOLED, epix2, marq2, fr2x5 AMOLED, instinct3 AMOLED...). Selected per
// device in monkey.jungle via sourcePath — this variant is the default.
module DeviceProfile {

    // Physical buttons: 2 left (UP/DOWN) + START top-right, same bezel
    // geometry as fr965 (degrees, 0 = 3 o'clock, CCW positive).
    const HAS_UPDOWN_BUTTONS = true;
    const ANG_START = 30;
    const ANG_UP    = 150;
    const ANG_DOWN  = 210;
    // Non-button bezel position used as a status indicator.
    const ANG_LINK  = 180; // 9 o'clock: phone link health warning

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
