import Toybox.Lang;
import Toybox.Graphics;
import Toybox.Math;

// Visual identity — "ink & paper on OLED".
//
// True black ground (AMOLED), cool paper-white ink, a single pale-blue accent
// (palette shared with the Android companion), and color ticks on the bezel
// aligned with the physical buttons: the hint system IS the hardware. Grade
// colors mirror Anki: red=Again, green=Good.
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

    // Segmented progress ring for the home screen: today's work in three arcs
    // clockwise from 12 o'clock — done (ACCENT), still-new (PAPER), still-review
    // (MUTED) — so the ring reads without color. `remaining` is the real count
    // of cards left; the new/rev counts come from the deck list and may not add
    // up to it, so they are scaled to fit `remaining` instead of trusted.
    function ringSplit(dc as Dc, r as Number, done as Number, remaining as Number,
            nNew as Number, nRev as Number) as Void {
        var cx = dc.getWidth() / 2;
        var cy = dc.getHeight() / 2;
        dc.setPenWidth(3);
        dc.setColor(FAINT, Graphics.COLOR_TRANSPARENT);
        dc.drawCircle(cx, cy, r);
        var total = done + remaining;
        if (total <= 0) {
            dc.setPenWidth(1);
            return;
        }
        var newPart = 0;
        if (remaining > 0) {
            var sum = nNew + nRev;
            newPart = sum > 0 ? remaining * nNew / sum : remaining;
        }
        var revPart = remaining - newPart;
        dc.setPenWidth(5);
        var f0 = 0.0;
        var parts = [done, newPart, revPart];
        var colors = [ACCENT, PAPER, MUTED];
        for (var i = 0; i < 3; i++) {
            var n = parts[i] as Number;
            if (n <= 0) {
                continue;
            }
            var f1 = f0 + n.toFloat() / total;
            _arc(dc, cx, cy, r, f0, f1, colors[i] as Number);
            f0 = f1;
        }
        dc.setPenWidth(1);
    }

    // Clockwise arc between two fractions of the circle (0 = 12 o'clock),
    // with a ~1.5 degree gap at each end so neighbours stay distinguishable.
    function _arc(dc as Dc, cx as Number, cy as Number, r as Number,
            f0 as Float, f1 as Float, color as Number) as Void {
        dc.setColor(color, Graphics.COLOR_TRANSPARENT);
        if (f1 - f0 >= 0.999) {
            dc.drawCircle(cx, cy, r);
            return;
        }
        var a0 = 360.0 * f0 + 1.5;
        var a1 = 360.0 * f1 - 1.5;
        if (a1 <= a0) {
            return;
        }
        var s = 90 - a0.toNumber();
        var e = 90 - a1.toNumber();
        while (s < 0) { s += 360; }
        while (e < 0) { e += 360; }
        dc.drawArc(cx, cy, r, Graphics.ARC_CLOCKWISE, s, e);
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
