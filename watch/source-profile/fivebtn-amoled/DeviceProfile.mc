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
