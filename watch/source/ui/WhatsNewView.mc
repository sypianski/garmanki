import Toybox.Lang;
import Toybox.Graphics;
import Toybox.Application.Storage;
import Toybox.WatchUi;

// One-time "What's new" after an update (TASK-44). Shown when the guide was
// already seen and Storage "newsSeen" differs from NOTES_ID; a fresh install
// gets the guide instead, which stamps NOTES_ID. Bump NOTES_ID (and the
// News1-4 strings) only for releases worth announcing — a plain patch keeps
// the old id and shows nothing.
module WhatsNew {

    const NOTES_ID = "1.3.2";

    function pending() as Boolean {
        return !NOTES_ID.equals(Storage.getValue("newsSeen"));
    }

    function markSeen() as Void {
        Storage.setValue("newsSeen", NOTES_ID);
    }
}

class WhatsNewView extends WatchUi.View {

    function initialize() {
        View.initialize();
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.BG, Theme.BG);
        dc.clear();
        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;

        Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);

        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 16 / 100, Graphics.FONT_XTINY,
            Theme.spaced(WatchUi.loadResource(Rez.Strings.WhatsNew) as String),
            Graphics.TEXT_JUSTIFY_CENTER);
        dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 16 / 100 + dc.getFontHeight(Graphics.FONT_XTINY),
            Graphics.FONT_XTINY, "v" + WhatsNew.NOTES_ID,
            Graphics.TEXT_JUSTIFY_CENTER);

        // One line per item, largest font that fits every line's chord.
        var ids = [Rez.Strings.News1, Rez.Strings.News2,
                   Rez.Strings.News3, Rez.Strings.News4];
        var lines = [];
        for (var i = 0; i < ids.size(); i++) {
            lines.add(WatchUi.loadResource(ids[i]) as String);
        }
        var fonts = [Graphics.FONT_TINY, Graphics.FONT_XTINY];
        var font = Graphics.FONT_XTINY;
        var lh = 0;
        var top = 0;
        for (var f = 0; f < fonts.size(); f++) {
            lh = dc.getFontHeight(fonts[f]) * 115 / 100;
            top = h * 56 / 100 - lh * lines.size() / 2;
            var fits = true;
            for (var i = 0; i < lines.size(); i++) {
                if (dc.getTextWidthInPixels(lines[i] as String, fonts[f])
                        > Theme.chordWidth(dc, top + lh * i + lh / 2, 10)) {
                    fits = false;
                    break;
                }
            }
            font = fonts[f];
            if (fits) {
                break;
            }
        }
        dc.setColor(Theme.PAPER, Graphics.COLOR_TRANSPARENT);
        for (var i = 0; i < lines.size(); i++) {
            dc.drawText(cx, top + lh * i, font, lines[i] as String,
                Graphics.TEXT_JUSTIFY_CENTER);
        }
    }
}

// Any of START, tap or BACK moves on to home; BACK must not exit the app here.
class WhatsNewDelegate extends WatchUi.BehaviorDelegate {

    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onSelect() as Boolean {
        _done();
        return true;
    }

    function onBack() as Boolean {
        _done();
        return true;
    }

    private function _done() as Void {
        WhatsNew.markSeen();
        WatchUi.switchToView(new HomeView(), new HomeDelegate(), WatchUi.SLIDE_LEFT);
    }
}
