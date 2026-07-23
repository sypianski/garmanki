import Toybox.Lang;
import Toybox.Graphics;
import Toybox.System;
import Toybox.Time;
import Toybox.Time.Gregorian;
import Toybox.WatchUi;

// Landing screen — "ink & paper": hero due count inside a session ring,
// amber tick at START (decks). Hold UP (menu) re-sends hello.
class HomeView extends WatchUi.View {

    function initialize() {
        View.initialize();
    }

    function onUpdate(dc as Dc) as Void {
        dc.setColor(Theme.BG, Theme.BG);
        dc.clear();

        var w = dc.getWidth();
        var h = dc.getHeight();
        var cx = w / 2;

        var decks = CardStore.getDecks();
        var stats = CardStore.getStats(); // [doneToday, streak]
        var remaining = 0;
        for (var i = 0; i < decks.size(); i++) {
            remaining += CardStore.cardsForDeck(decks[i][0]).size();
        }

        // Ring shows today's momentum: done / (done + still due).
        var done = stats[0] instanceof Number ? stats[0] : 0;
        var frac = (done + remaining) > 0 ? done.toFloat() / (done + remaining) : 0.0;
        Theme.ring(dc, cx - 12, frac);

        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 12 / 100, Graphics.FONT_XTINY,
            Theme.spaced("Garmanki"), Graphics.TEXT_JUSTIFY_CENTER);
        dc.setColor(Theme.FAINT, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, h * 89 / 100, Graphics.FONT_XTINY,
            "v" + (WatchUi.loadResource(Rez.Strings.AppVersion) as String),
            Graphics.TEXT_JUSTIFY_CENTER);

        if (decks.size() == 0) {
            var ta = new WatchUi.TextArea({
                :text => WatchUi.loadResource(Rez.Strings.HomeNoData) as String,
                :color => Theme.PAPER,
                :font => [Graphics.FONT_SMALL, Graphics.FONT_TINY, Graphics.FONT_XTINY],
                :locX => w / 6,
                :locY => h * 34 / 100,
                :width => w * 2 / 3,
                :height => h * 30 / 100,
                :justification => Graphics.TEXT_JUSTIFY_CENTER
            });
            ta.draw(dc);
        } else {
            // Hero: cards still due today.
            dc.setColor(Theme.PAPER, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 24 / 100, Graphics.FONT_NUMBER_THAI_HOT,
                remaining.toString(), Graphics.TEXT_JUSTIFY_CENTER);
            dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 52 / 100, Graphics.FONT_XTINY,
                Theme.spaced(WatchUi.loadResource(Rez.Strings.HomeDue) as String),
                Graphics.TEXT_JUSTIFY_CENTER);

            // Build the info line richest-first, then drop trailing segments
            // until it fits the chord of the round screen at this height.
            var base = decks.size().toString() + " "
                + (WatchUi.loadResource(Rez.Strings.HomeDecks) as String).toLower();
            var streak = stats[1] instanceof Number ? stats[1] : 0;
            var pend = CardStore.pendingCount();
            var candidates = [];
            var full = base;
            if (streak > 0) {
                full += " · " + streak.toString() + " "
                    + (WatchUi.loadResource(Rez.Strings.HomeStreak) as String);
            }
            if (pend > 0) {
                candidates.add(full + " · " + pend.toString() + " "
                    + (WatchUi.loadResource(Rez.Strings.HomePending) as String));
            }
            candidates.add(full);
            candidates.add(base);
            var lineY = h * 62 / 100;
            var fontH = dc.getFontHeight(Graphics.FONT_XTINY);
            var maxW = Theme.chordWidth(dc, lineY + fontH / 2, 8);
            var line = candidates[candidates.size() - 1];
            for (var i = 0; i < candidates.size(); i++) {
                if (dc.getTextWidthInPixels(candidates[i], Graphics.FONT_XTINY) <= maxW) {
                    line = candidates[i];
                    break;
                }
            }
            dc.drawText(cx, lineY, Graphics.FONT_XTINY, line,
                Graphics.TEXT_JUSTIFY_CENTER);
        }

        var link = Link.get();
        var status = link.status;
        // Treat status as expired after STATUS_TTL_MS, even without a timer
        // firing — avoids relying on Garmin's potentially-suspended timers.
        var statusFresh = status.length() > 0
            && (System.getTimer() - link.statusSetMs) < link.STATUS_TTL_MS;
        if (statusFresh) {
            dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 72 / 100, Graphics.FONT_XTINY, status,
                Graphics.TEXT_JUSTIFY_CENTER);
        } else {
            var syncT = CardStore.getLastSyncTime();
            dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 72 / 100, Graphics.FONT_XTINY,
                syncT instanceof Number
                    ? _syncLine(syncT as Number)
                    : (WatchUi.loadResource(Rez.Strings.SyncNever) as String),
                Graphics.TEXT_JUSTIFY_CENTER);
        }

        Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);
    }

    private function _syncLine(syncT as Number) as String {
        var nowSecs = Time.now().value();
        var diff = nowSecs - syncT;
        if (diff < 300) {
            return WatchUi.loadResource(Rez.Strings.SyncJustNow) as String;
        }
        var si = Gregorian.info(new Time.Moment(syncT), Time.FORMAT_SHORT);
        var timeStr = si.hour.format("%02d") + ":" + si.min.format("%02d");
        var daysDiff = (nowSecs / 86400) - (syncT / 86400);
        if (daysDiff == 0) {
            return (WatchUi.loadResource(Rez.Strings.SyncedAt) as String) + " " + timeStr;
        }
        if (daysDiff == 1) {
            return (WatchUi.loadResource(Rez.Strings.SyncYesterday) as String) + " " + timeStr;
        }
        if (daysDiff <= 7) {
            var dow = ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"];
            return dow[(si.day_of_week as Number) - 1] + " " + timeStr;
        }
        var mon = ["Jan","Feb","Mar","Apr","May","Jun",
                   "Jul","Aug","Sep","Oct","Nov","Dec"];
        return si.day.toString() + " " + mon[(si.month as Number) - 1] + " " + timeStr;
    }
}

class HomeDelegate extends WatchUi.BehaviorDelegate {

    function initialize() {
        BehaviorDelegate.initialize();
    }

    function onSelect() as Boolean {
        DeckMenu.push();
        return true;
    }

    function onMenu() as Boolean {
        Link.get().hello();
        Link.get().setStatus(WatchUi.loadResource(Rez.Strings.SyncSent) as String);
        return true;
    }
}
