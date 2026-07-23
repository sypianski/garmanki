import Toybox.Lang;
import Toybox.Graphics;
import Toybox.WatchUi;

// Session close: a full amber ring (the day's loop closed), hero count,
// quiet metadata. Ink & paper, nothing shouting.
class SummaryView extends WatchUi.View {

    private var _done as Number;
    private var _minutes as Number;

    function initialize(done as Number, minutes as Number) {
        View.initialize();
        _done = done;
        _minutes = minutes;
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.BG, Theme.BG);
        dc.clear();
        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;

        Theme.ring(dc, cx - 12, 1.0);

        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 16 / 100, Graphics.FONT_XTINY,
            Theme.spaced(WatchUi.loadResource(Rez.Strings.SummaryTitle) as String),
            Graphics.TEXT_JUSTIFY_CENTER);

        dc.setColor(Theme.PAPER, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 26 / 100, Graphics.FONT_NUMBER_THAI_HOT,
            _done.toString(), Graphics.TEXT_JUSTIFY_CENTER);

        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 55 / 100, Graphics.FONT_TINY,
            (WatchUi.loadResource(Rez.Strings.SummaryCards) as String)
                + " · " + _minutes.toString() + " min",
            Graphics.TEXT_JUSTIFY_CENTER);

        var pend = CardStore.pendingCount();
        if (pend > 0) {
            dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 66 / 100, Graphics.FONT_XTINY,
                pend.toString() + " "
                    + (WatchUi.loadResource(Rez.Strings.SummaryQueued) as String),
                Graphics.TEXT_JUSTIFY_CENTER);
        }
    }
}

class SummaryDelegate extends WatchUi.BehaviorDelegate {

    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onSelect() as Boolean {
        WatchUi.popView(WatchUi.SLIDE_RIGHT);
        return true;
    }

    function onBack() as Boolean {
        WatchUi.popView(WatchUi.SLIDE_RIGHT);
        return true;
    }
}
