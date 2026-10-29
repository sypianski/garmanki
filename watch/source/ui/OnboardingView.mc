import Toybox.Lang;
import Toybox.Graphics;
import Toybox.WatchUi;
import Toybox.Math;
import Toybox.Application.Storage;
import Toybox.Attention;
import Toybox.System;
import Toybox.Timer;

// First-run guide (GAR-03) — four "ink & paper" slides teaching the review
// controls. Slides 2 and 3 render from ActionMap, so a remapped config
// (GAR-07) changes the guide without code edits.
//
// Slides 2 and 3 are practice (TASK-39): each button / gesture they show
// lights its grade word and buzzes once; when all have been tried the slide
// moves on by itself. There is no going back — the old UP = previous slide
// contradicted slide 2, where UP is drawn as a grade. Slide 1: any input
// moves on; slide 4: START or tap finishes. BACK = skip. Input arrives
// normalized by GestureDelegate. First run switches to HomeView
// and stamps Storage "onboardingSeen"; re-runs from DeckMenu just pop back.
class OnboardingView extends WatchUi.View {

    const SLIDES = 4;

    // Pause between the last practice input and the next slide, so the
    // lit word is seen.
    const ADVANCE_MS = 700;

    private var _slide as Number = 0;
    private var _firstRun as Boolean;
    private var _tried as Dictionary = {};
    private var _timer as Timer.Timer?;

    function initialize(firstRun as Boolean) {
        View.initialize();
        _firstRun = firstRun;
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.BG, Theme.BG);
        dc.clear();

        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;

        var titles = [Rez.Strings.GuideT1, Rez.Strings.GuideT2,
                      Rez.Strings.GuideT3, Rez.Strings.GuideT4];
        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 12 / 100, Graphics.FONT_XTINY,
            Theme.spaced(WatchUi.loadResource(titles[_slide]) as String),
            Graphics.TEXT_JUSTIFY_CENTER);

        if (_slide == 0) {
            _drawReveal(dc);
        } else if (_slide == 1) {
            _drawButtons(dc);
        } else if (_slide == 2) {
            _drawGestures(dc);
        } else {
            _drawMore(dc);
        }

        _dots(dc);
    }

    // Slide 1: any button (or tap) reveals the answer.
    private function _drawReveal(dc as Dc) as Void {
        if (DeviceProfile.HAS_UPDOWN_BUTTONS) {
            Theme.tick(dc, Theme.ANG_UP, Theme.ACCENT);
            Theme.tick(dc, Theme.ANG_DOWN, Theme.ACCENT);
        }
        Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);
        _centerText(dc, Rez.Strings.Guide1, Theme.PAPER);
    }

    // Slide 2: grade buttons — ticks and words straight from ActionMap.
    private function _drawButtons(dc as Dc) as Void {
        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;
        var cy = h / 2;
        var events = DeviceProfile.HAS_UPDOWN_BUTTONS
            ? ["up", "down", "start"] : ["start"];
        var angles = DeviceProfile.HAS_UPDOWN_BUTTONS
            ? [Theme.ANG_UP, Theme.ANG_DOWN, Theme.ANG_START]
            : [Theme.ANG_START];
        var fontH = dc.getFontHeight(Graphics.FONT_TINY);
        // Label anchor radius, just inside the ticks.
        var lr = cx - Theme.px(dc, 45);

        for (var i = 0; i < events.size(); i++) {
            var ease = ActionMap.easeFor(events[i]);
            if (ease == null) {
                continue;
            }
            var color = Theme.easeColor(ease);
            Theme.tick(dc, angles[i], color);
            var rad = angles[i] * Math.PI / 180.0;
            var lx = cx + (lr * Math.cos(rad)).toNumber();
            var ly = cy - (lr * Math.sin(rad)).toNumber() - fontH / 2;
            // Anchor at the bezel side, run the word inward so it stays
            // inside the chord.
            var just = Math.cos(rad) < 0
                ? Graphics.TEXT_JUSTIFY_LEFT : Graphics.TEXT_JUSTIFY_RIGHT;
            dc.setColor(_tried.hasKey(events[i]) ? color : Theme.MUTED,
                Graphics.COLOR_TRANSPARENT);
            dc.drawText(lx, ly, Graphics.FONT_TINY, _easeWord(ease), just);
        }

        dc.setColor(Theme.PAPER, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, cy - dc.getFontHeight(Graphics.FONT_TINY) / 2,
            Graphics.FONT_TINY, WatchUi.loadResource(Rez.Strings.Guide2) as String,
            Graphics.TEXT_JUSTIFY_CENTER);
    }

    // Slide 3: one row per mapped gesture — arrow/tap glyph plus grade word.
    private function _drawGestures(dc as Dc) as Void {
        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;
        var keys = ["swipeR", "swipeL", "swipeU", "swipeD", "tap"];
        var rows = [];
        for (var i = 0; i < keys.size(); i++) {
            var ease = ActionMap.easeFor(keys[i]);
            if (ease != null) {
                rows.add([keys[i], ease]);
            }
        }
        var fontH = dc.getFontHeight(Graphics.FONT_TINY);
        var rowH = fontH + 4;
        var y = (h - rows.size() * rowH) / 2;
        for (var i = 0; i < rows.size(); i++) {
            var key = rows[i][0] as String;
            var ease = rows[i][1] as Number;
            var color = _tried.hasKey(key) ? Theme.easeColor(ease) : Theme.MUTED;
            var iconY = y + fontH / 2;
            var iconX = cx - Theme.px(dc, 60);
            if ("tap".equals(key)) {
                dc.setColor(color, Graphics.COLOR_TRANSPARENT);
                dc.fillCircle(iconX, iconY, Theme.px(dc, 5));
                dc.drawCircle(iconX, iconY, Theme.px(dc, 9));
            } else {
                _arrow(dc, iconX, iconY, key, color);
            }
            dc.setColor(color, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx - Theme.px(dc, 38), y, Graphics.FONT_TINY,
                _easeWord(ease), Graphics.TEXT_JUSTIFY_LEFT);
            y += rowH;
        }
        // Watches without touch (or with no gesture mapped) leave this slide
        // with a button — say so instead of asking for gestures.
        var hint = _gesturesTryable() ? Rez.Strings.Guide3 : Rez.Strings.GuideNext;
        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 80 / 100, Graphics.FONT_XTINY,
            WatchUi.loadResource(hint) as String, Graphics.TEXT_JUSTIFY_CENTER);
    }

    // Slide 4: BACK opens the options menu (TASK-41).
    private function _drawMore(dc as Dc) as Void {
        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;
        Theme.tick(dc, Theme.ANG_BACK, Theme.ACCENT);
        _centerText(dc, Rez.Strings.Guide4, Theme.PAPER);
        var y = h * 76 / 100;
        var done = WatchUi.loadResource(Rez.Strings.GuideDone) as String;
        var fontH = dc.getFontHeight(Graphics.FONT_XTINY);
        if (dc.getTextWidthInPixels(done, Graphics.FONT_XTINY)
                <= Theme.chordWidth(dc, y + fontH / 2, 8)) {
            dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, y, Graphics.FONT_XTINY, done,
                Graphics.TEXT_JUSTIFY_CENTER);
        }
    }

    // Wrapped body copy in the middle band; width clamped to the chord at
    // the box edges so nothing leaves the circle.
    private function _centerText(dc as Dc, strId, color as Number) as Void {
        var w = dc.getWidth();
        var h = dc.getHeight();
        var top = h * 30 / 100;
        var boxH = h * 40 / 100;
        var maxW = Theme.chordWidth(dc, top, 14);
        var wBot = Theme.chordWidth(dc, top + boxH, 14);
        if (wBot < maxW) {
            maxW = wBot;
        }
        var ta = new WatchUi.TextArea({
            :text => WatchUi.loadResource(strId) as String,
            :color => color,
            :font => [Graphics.FONT_SMALL, Graphics.FONT_TINY, Graphics.FONT_XTINY],
            :locX => (w - maxW) / 2,
            :locY => top,
            :width => maxW,
            :height => boxH,
            :justification => Graphics.TEXT_JUSTIFY_CENTER | Graphics.TEXT_JUSTIFY_VCENTER
        });
        ta.draw(dc);
    }

    // Solid triangle — built-in fonts don't guarantee arrow glyphs.
    private function _arrow(dc as Dc, x as Number, y as Number,
            key as String, color as Number) as Void {
        var s = Theme.px(dc, 9);
        var pts;
        if ("swipeR".equals(key)) {
            pts = [[x - s, y - s], [x + s, y], [x - s, y + s]];
        } else if ("swipeL".equals(key)) {
            pts = [[x + s, y - s], [x - s, y], [x + s, y + s]];
        } else if ("swipeU".equals(key)) {
            pts = [[x - s, y + s], [x, y - s], [x + s, y + s]];
        } else {
            pts = [[x - s, y - s], [x, y + s], [x + s, y - s]];
        }
        dc.setColor(color, Graphics.COLOR_TRANSPARENT);
        dc.fillPolygon(pts);
    }

    private function _dots(dc as Dc) as Void {
        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;
        var spacing = Theme.px(dc, 16);
        var x = cx - spacing * (SLIDES - 1) / 2;
        var y = h * 88 / 100;
        for (var i = 0; i < SLIDES; i++) {
            dc.setColor(i == _slide ? Theme.ACCENT : Theme.FAINT,
                Graphics.COLOR_TRANSPARENT);
            dc.fillCircle(x + i * spacing, y, Theme.px(dc, 3));
        }
    }

    private function _easeWord(ease as Number) as String {
        var ids = [Rez.Strings.WordAgain, Rez.Strings.WordHard,
                   Rez.Strings.WordGood, Rez.Strings.WordEasy];
        return WatchUi.loadResource(ids[ease - 1]) as String;
    }

    // One entry point for every input; event keys as in ActionMap.
    function input(event as String) as Void {
        if (_timer != null) {
            return; // practice slide done, already moving on
        }
        if (_slide == 1) {
            var buttons = _practice(_buttonEvents());
            if (buttons.indexOf(event) >= 0) {
                _try(event, buttons);
            } else if (buttons.size() == 0) {
                _next();
            }
        } else if (_slide == 2) {
            var gestures = _practice(["swipeR", "swipeL", "swipeU", "swipeD", "tap"]);
            // START also moves on: touch can be switched off in the watch
            // settings while isTouchScreen still reports the hardware.
            if (!_gesturesTryable() || "start".equals(event)) {
                _next();
            } else if (gestures.indexOf(event) >= 0) {
                _try(event, gestures);
            }
        } else if (_slide == SLIDES - 1) {
            // Only START / tap finish — a stray swipe or UP/DOWN used to
            // close the guide right after slide 3 moved on.
            if ("start".equals(event) || "tap".equals(event)) {
                finish();
            }
        } else {
            _next();
        }
    }

    function onHide() as Void {
        _stopTimer();
    }

    private function _buttonEvents() as Array {
        return DeviceProfile.HAS_UPDOWN_BUTTONS ? ["up", "down", "start"] : ["start"];
    }

    // The events among `keys` that grade something — what the slide shows.
    private function _practice(keys as Array) as Array {
        var out = [];
        for (var i = 0; i < keys.size(); i++) {
            if (ActionMap.easeFor(keys[i] as String) != null) {
                out.add(keys[i] as String);
            }
        }
        return out;
    }

    private function _gesturesTryable() as Boolean {
        return System.getDeviceSettings().isTouchScreen
            && _practice(["swipeR", "swipeL", "swipeU", "swipeD", "tap"]).size() > 0;
    }

    private function _try(event as String, all as Array) as Void {
        _tried[event] = true;
        if (Attention has :vibrate && System.getDeviceSettings().vibrateOn) {
            Attention.vibrate([new Attention.VibeProfile(40, 60)]);
        }
        WatchUi.requestUpdate();
        for (var i = 0; i < all.size(); i++) {
            if (!_tried.hasKey(all[i] as String)) {
                return;
            }
        }
        _timer = new Timer.Timer();
        _timer.start(method(:onAdvance), ADVANCE_MS, false);
    }

    function onAdvance() as Void {
        _timer = null;
        _next();
    }

    private function _next() as Void {
        _tried = {};
        if (_slide < SLIDES - 1) {
            _slide++;
            WatchUi.requestUpdate();
        }
    }

    private function _stopTimer() as Void {
        if (_timer != null) {
            _timer.stop();
            _timer = null;
        }
    }

    // Also the skip path (BACK): first run always stamps the seen flag.
    function finish() as Void {
        _stopTimer();
        if (_firstRun) {
            Storage.setValue("onboardingSeen", true);
            WhatsNew.markSeen(); // the guide already covers what's new
            WatchUi.switchToView(new HomeView(), new HomeDelegate(),
                WatchUi.SLIDE_LEFT);
        } else {
            WatchUi.popView(WatchUi.SLIDE_RIGHT);
        }
    }
}

class OnboardingDelegate extends GestureDelegate {

    private var _onboarding as OnboardingView;

    function initialize(onboarding as OnboardingView) {
        GestureDelegate.initialize();
        _onboarding = onboarding;
    }

    function onInput(event as String) as Void {
        _onboarding.input(event);
    }

    function onBack() as Boolean {
        _onboarding.finish();
        return true;
    }
}
