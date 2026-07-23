import Toybox.Lang;
import Toybox.Graphics;
import Toybox.Math;

// Visual identity — "ink & paper on OLED".
//
// True black ground (AMOLED), warm paper-white ink, a single amber accent,
// and color ticks on the bezel aligned with the physical buttons: the hint
// system IS the hardware. Grade colors mirror Anki: red=Again, green=Good.
module Theme {

    const BG     = 0x000000;
    const PAPER  = 0xF5EFE3; // warm off-white "ink"
    const MUTED  = 0x8A8478; // warm gray
    const FAINT  = 0x2E2B26; // hairlines / idle ring
    const ACCENT = 0xFFB300; // amber
    const AGAIN  = 0xFF5A48;
    const HARD   = 0xFF8C42; // muted orange (Anki convention)
    const GOOD   = 0x4CD07D;
    const EASY   = 0x4FA8D8; // calm blue (Anki convention)

    // Physical button angles on FR965 (degrees, 0 = 3 o'clock, CCW positive).
    // NOTE: fr965-specific — parameterize per device before adding targets.
    const ANG_START = 30;
    const ANG_UP    = 150;
    const ANG_DOWN  = 210;

    // Bezel tick / caption color for an Anki ease (1=Again … 4=Easy).
    function easeColor(ease as Number) as Number {
        if (ease == 1) { return AGAIN; }
        if (ease == 2) { return HARD; }
        if (ease == 4) { return EASY; }
        return GOOD;
    }

    // Short arc segment on the bezel pointing at a physical button.
    function tick(dc as Dc, angle as Number, color as Number) as Void {
        var cx = dc.getWidth() / 2;
        var cy = dc.getHeight() / 2;
        var r = cx - 5;
        dc.setColor(color, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(7);
        dc.drawArc(cx, cy, r, Graphics.ARC_COUNTER_CLOCKWISE, angle - 7, angle + 7);
        dc.setPenWidth(1);
    }

    // Progress ring: faint full circle + accent sweep clockwise from top.
    function ring(dc as Dc, r as Number, frac as Float) as Void {
        var cx = dc.getWidth() / 2;
        var cy = dc.getHeight() / 2;
        dc.setPenWidth(3);
        dc.setColor(FAINT, Graphics.COLOR_TRANSPARENT);
        dc.drawCircle(cx, cy, r);
        if (frac > 0.0) {
            dc.setPenWidth(5);
            dc.setColor(ACCENT, Graphics.COLOR_TRANSPARENT);
            if (frac >= 1.0) {
                dc.drawCircle(cx, cy, r);
            } else {
                var endDeg = 90 - (360.0 * frac).toNumber();
                if (endDeg < 0) {
                    endDeg += 360;
                }
                dc.drawArc(cx, cy, r, Graphics.ARC_CLOCKWISE, 90, endDeg);
            }
        }
        dc.setPenWidth(1);
    }

    // Usable text width on a round screen: chord length at the vertical
    // center of a text line (yCenter), keeping `margin` px off the bezel.
    // Returns 0 when yCenter falls outside the circle.
    function chordWidth(dc as Dc, yCenter as Number, margin as Number) as Number {
        var cx = dc.getWidth() / 2;
        var cy = dc.getHeight() / 2;
        var r = cx - margin;
        var dy = yCenter - cy;
        if (dy < 0) {
            dy = -dy;
        }
        if (dy >= r) {
            return 0;
        }
        return (2.0 * Math.sqrt((r * r - dy * dy).toFloat())).toNumber();
    }

    // Sparse uppercase caption, e.g. "D U E  T O D A Y".
    function spaced(s as String) as String {
        var chars = s.toUpper().toCharArray();
        var out = "";
        for (var i = 0; i < chars.size(); i++) {
            out += chars[i].toString();
            if (i < chars.size() - 1) {
                out += " ";
            }
        }
        return out;
    }
}
