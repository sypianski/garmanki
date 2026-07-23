import Toybox.Lang;
import Toybox.Graphics;
import Toybox.WatchUi;
import Toybox.Math;
import Toybox.Application.Storage;

// First-run guide (GAR-03) — four "ink & paper" slides teaching the review
// controls. Slides 2 and 3 render from ActionMap, so a remapped config
// (GAR-07) changes the guide without code edits.
//
// Navigation: START/tap = next (finishes on the last slide), UP/DOWN and
// vertical swipes = prev/next, BACK = skip. First run switches to HomeView
// and stamps Storage "onboardingSeen"; re-runs from DeckMenu just pop back.
class OnboardingView extends WatchUi.View {

    const SLIDES = 4;

    private var _slide as Number = 0;
    private var _firstRun as Boolean;

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
        Theme.tick(dc, Theme.ANG_UP, Theme.ACCENT);
        Theme.tick(dc, Theme.ANG_DOWN, Theme.ACCENT);
        Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);
        _centerText(dc, Rez.Strings.Guide1, Theme.PAPER);
    }

    // Slide 2: grade buttons — ticks and words straight from ActionMap.
    private function _drawButtons(dc as Dc) as Void {
        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;
        var cy = h / 2;
        var events = ["up", "down", "start"];
        var angles = [Theme.ANG_UP, Theme.ANG_DOWN, Theme.ANG_START];
        var fontH = dc.getFontHeight(Graphics.FONT_TINY);
        var lr = cx - 45; // label anchor radius, just inside the ticks

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
            dc.setColor(color, Graphics.COLOR_TRANSPARENT);
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
            var color = Theme.easeColor(ease);
            var iconY = y + fontH / 2;
            if ("tap".equals(key)) {
                dc.setColor(color, Graphics.COLOR_TRANSPARENT);
                dc.fillCircle(cx - 60, iconY, 5);
                dc.drawCircle(cx - 60, iconY, 9);
            } else {
                _arrow(dc, cx - 60, iconY, key, color);
            }
            dc.setColor(color, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx - 38, y, Graphics.FONT_TINY, _easeWord(ease),
                Graphics.TEXT_JUSTIFY_LEFT);
            y += rowH;
        }
    }

    // Slide 4: card-action menu and leaving a session.
    private function _drawMore(dc as Dc) as Void {
        var h = dc.getHeight();
        var cx = dc.getWidth() / 2;
        Theme.tick(dc, Theme.ANG_UP, Theme.ACCENT);
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
        var s = 9;
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
        var spacing = 16;
        var x = cx - spacing * (SLIDES - 1) / 2;
        var y = h * 88 / 100;
        for (var i = 0; i < SLIDES; i++) {
            dc.setColor(i == _slide ? Theme.ACCENT : Theme.FAINT,
                Graphics.COLOR_TRANSPARENT);
            dc.fillCircle(x + i * spacing, y, 3);
        }
    }

    private function _easeWord(ease as Number) as String {
        var ids = [Rez.Strings.WordAgain, Rez.Strings.WordHard,
                   Rez.Strings.WordGood, Rez.Strings.WordEasy];
        return WatchUi.loadResource(ids[ease - 1]) as String;
    }

    function next() as Void {
        if (_slide < SLIDES - 1) {
            _slide++;
            WatchUi.requestUpdate();
        }
    }

    function prev() as Void {
        if (_slide > 0) {
            _slide--;
            WatchUi.requestUpdate();
        }
    }

    function nextOrFinish() as Void {
        if (_slide < SLIDES - 1) {
            next();
        } else {
            finish();
        }
    }

    // Also the skip path (BACK): first run always stamps the seen flag.
    function finish() as Void {
        if (_firstRun) {
            Storage.setValue("onboardingSeen", true);
            WatchUi.switchToView(new HomeView(), new HomeDelegate(),
                WatchUi.SLIDE_LEFT);
        } else {
            WatchUi.popView(WatchUi.SLIDE_RIGHT);
        }
    }
}

class OnboardingDelegate extends WatchUi.BehaviorDelegate {

    function initialize() {
        BehaviorDelegate.initialize();
    }

    private function _view() as OnboardingView {
        return WatchUi.getCurrentView()[0] as OnboardingView;
    }

    function onSelect() as Boolean {
        _view().nextOrFinish();
        return true;
    }

    function onNextPage() as Boolean {
        _view().next();
        return true;
    }

    function onPreviousPage() as Boolean {
        _view().prev();
        return true;
    }

    function onBack() as Boolean {
        _view().finish();
        return true;
    }

    function onTap(evt as WatchUi.ClickEvent) as Boolean {
        _view().nextOrFinish();
        return true;
    }

    // Swallow every direction so a right-swipe can't pop the guide without
    // stamping the seen flag.
    function onSwipe(evt as WatchUi.SwipeEvent) as Boolean {
        var dir = evt.getDirection();
        if (dir == WatchUi.SWIPE_UP || dir == WatchUi.SWIPE_LEFT) {
            _view().next();
        } else {
            _view().prev();
        }
        return true;
    }
}
