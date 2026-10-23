import Toybox.Lang;

// Device profile: five-button watches with a 64-color MIP display (fenix7,
// fr255/955, enduro3, fenix8 solar). Same button geometry as fr965; the
// palette is snapped to the safe 64-color grid (channels 0x00/0x55/0xAA/
// 0xFF) so the ink/muted/faint hierarchy survives quantization.
module DeviceProfile {

    const HAS_UPDOWN_BUTTONS = true;
    const ANG_START = 30;
    const ANG_UP    = 150;
    const ANG_DOWN  = 210;
    const ANG_LINK  = 180; // 9 o'clock: phone link health warning

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
