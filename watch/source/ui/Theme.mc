import Toybox.Lang;
import Toybox.Graphics;
import Toybox.Math;

// Visual identity — "ink & paper on OLED".
//
// True black ground (AMOLED), warm paper-white ink, a single amber accent,
// and color ticks on the bezel aligned with the physical buttons: the hint
// system IS the hardware. Grade colors mirror Anki: red=Again, green=Good.
//
// Palette and button angles come from DeviceProfile — one variant per
// device family, selected in monkey.jungle via sourcePath.
module Theme {

    const BG     = DeviceProfile.BG;
    const PAPER  = DeviceProfile.PAPER;
    const MUTED  = DeviceProfile.MUTED;
    const FAINT  = DeviceProfile.FAINT;
    const ACCENT = DeviceProfile.ACCENT;
    const AGAIN  = DeviceProfile.AGAIN;
    const HARD   = DeviceProfile.HARD;
    const GOOD   = DeviceProfile.GOOD;
    const EASY   = DeviceProfile.EASY;

    const ANG_START = DeviceProfile.ANG_START;
    const ANG_UP    = DeviceProfile.ANG_UP;
    const ANG_DOWN  = DeviceProfile.ANG_DOWN;
    // Non-button bezel positions used as status indicators (drawn only when
    // there's something to say — a silent bezel means "everything's fine").
    const ANG_LINK  = DeviceProfile.ANG_LINK;

    // Scale a pixel measure designed on the 454x454 reference (fr965) to
    // the current display, rounded, never below 1 px.
    function px(dc as Dc, v as Number) as Number {
        var s = (dc.getWidth() * v + 227) / 454;
        return s < 1 ? 1 : s;
    }

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
        var r = cx - px(dc, 5);
        dc.setColor(color, Graphics.COLOR_TRANSPARENT);
        dc.setPenWidth(px(dc, 7));
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
