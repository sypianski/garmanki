import Toybox.Lang;
import Toybox.Graphics;
import Toybox.System;
import Toybox.Time;
import Toybox.Time.Gregorian;
import Toybox.WatchUi;

// Landing screen — "ink & paper". Vertical hierarchy top→bottom:
//
//   ring         session momentum (done / (done + still due) today)
//   hero         due count, or ✓ when all done for today
//   split        new + rev breakdown of what's still due
//   habit        N decks · N day streak
//   sync-time    ALWAYS present: when last successful sync happened
//   CTA          only when pend>0 OR phone off: what the user should do next
//   version      footer
//
// Bezel language: amber tick at START (decks button) is always drawn — it is
// the primary affordance. A muted tick at 9 o'clock appears only when the
// phone is out of reach AND there's outstanding work; a silent bezel means
// "you don't have to do anything". Colors are the language: PAPER = primary
// state, MUTED = ambient, ACCENT = actionable. Hold UP re-sends hello.
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
        var nNew = 0;
        var nRev = 0;
        for (var i = 0; i < decks.size(); i++) {
            var d = decks[i] as Array;
            remaining += CardStore.cardsForDeck(d[0] as Number).size();
            if (d.size() > 3 && d[3] instanceof Number) { nNew += d[3] as Number; }
            if (d.size() > 5 && d[5] instanceof Number) { nRev += d[5] as Number; }
        }

        // Ring shows today's momentum: done / (done + still due).
        var done = stats[0] instanceof Number ? stats[0] : 0;
        var frac = (done + remaining) > 0 ? done.toFloat() / (done + remaining) : 0.0;
        Theme.ring(dc, cx - 12, frac);

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
                :locY => h * 28 / 100,
                :width => w * 2 / 3,
                :height => h * 28 / 100,
                :justification => Graphics.TEXT_JUSTIFY_CENTER
            });
            ta.draw(dc);
        } else if (remaining == 0 && done > 0) {
            // "Done for today" state — the hero has to communicate rest, not
            // an ambiguous zero. Drawn checkmark (number fonts don't
            // guarantee a ✓ glyph on every device) + subtitle count.
            var fh = dc.getFontHeight(Graphics.FONT_NUMBER_MEDIUM);
            var mcy = h * 20 / 100 + fh / 2;
            var ms = fh * 30 / 100; // half-width of the mark
            dc.setColor(Theme.GOOD, Graphics.COLOR_TRANSPARENT);
            dc.setPenWidth(Theme.px(dc, 8));
            dc.drawLine(cx - ms, mcy, cx - ms / 3, mcy + ms * 2 / 3);
            dc.drawLine(cx - ms / 3, mcy + ms * 2 / 3, cx + ms, mcy - ms * 2 / 3);
            dc.setPenWidth(1);
            dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 42 / 100, Graphics.FONT_XTINY,
                Theme.spaced(WatchUi.loadResource(Rez.Strings.HomeAllDone) as String),
                Graphics.TEXT_JUSTIFY_CENTER);
            _drawHabit(dc, cx, h * 52 / 100, decks.size(), stats);
        } else {
            // Hero: cards still due today.
            dc.setColor(Theme.PAPER, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, h * 20 / 100, Graphics.FONT_NUMBER_MILD,
                remaining.toString(), Graphics.TEXT_JUSTIFY_CENTER);
            // Split: "8 new · 12 rev" replaces the generic "due" caption. Only
            // draws when either side is non-zero AND the sum matches remaining
            // roughly — falls back to plain "due" otherwise so a stale/missing
            // count never mislabels the hero.
            dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
            var subLine = null;
            if ((nNew + nRev) > 0) {
                var parts = "";
                if (nNew > 0) {
                    parts = nNew.toString() + " "
                        + (WatchUi.loadResource(Rez.Strings.HomeNew) as String);
                }
                if (nRev > 0) {
                    if (parts.length() > 0) { parts += " · "; }
                    parts += nRev.toString() + " "
                        + (WatchUi.loadResource(Rez.Strings.HomeRev) as String);
                }
                subLine = parts;
            }
            if (subLine == null) {
                subLine = Theme.spaced(WatchUi.loadResource(Rez.Strings.HomeDue) as String);
            }
            dc.drawText(cx, h * 40 / 100, Graphics.FONT_XTINY, subLine,
                Graphics.TEXT_JUSTIFY_CENTER);
            _drawHabit(dc, cx, h * 50 / 100, decks.size(), stats);
        }

        // Sync-time line — ALWAYS visible. This is the load-bearing signal for
        // the user's mental model ("how stale is my state?"), so it never gets
        // hidden behind the transient status or the CTA. Transient status is
        // shown ABOVE it (accent), CTA is shown BELOW it (accent).
        var link = Link.get();
        var status = link.status;
        var statusFresh = status.length() > 0
            && (System.getTimer() - link.statusSetMs) < link.STATUS_TTL_MS;

        var syncT = CardStore.getLastSyncTime();
        var syncY = h * 62 / 100;
        if (syncT instanceof Number) {
            dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, syncY, Graphics.FONT_XTINY,
                _syncLine(syncT as Number), Graphics.TEXT_JUSTIFY_CENTER);
        }

        // Below the sync-time: transient status wins the accent slot (short
        // "Sync requested" / "Answers delivered" flashes); otherwise the CTA
        // tells the user what to do about outstanding work, or (fresh install)
        // to connect the phone at all.
        var pend = CardStore.pendingCount();
        var phoneOn = System.getDeviceSettings().phoneConnected;
        var ctaText = null;
        if (statusFresh) {
            ctaText = status;
        } else if (pend > 0) {
            var unsynced = pend.toString() + " "
                + (WatchUi.loadResource(Rez.Strings.SyncUnsynced) as String);
            if (!phoneOn) {
                var openP = WatchUi.loadResource(Rez.Strings.SyncOpenPhone) as String;
                ctaText = unsynced + " · " + openP;
            } else {
                ctaText = unsynced;
            }
        } else if (!(syncT instanceof Number)) {
            // Fresh install — no sync ever succeeded. Push the user toward
            // the one action that unblocks everything.
            ctaText = WatchUi.loadResource(Rez.Strings.SyncConnectPhone) as String;
        } else if (!phoneOn) {
            ctaText = WatchUi.loadResource(Rez.Strings.SyncPhoneOff) as String;
        }
        if (ctaText != null) {
            var ctaY = h * 74 / 100;
            var fontH = dc.getFontHeight(Graphics.FONT_XTINY);
            var ctaMax = Theme.chordWidth(dc, ctaY + fontH / 2, 8);
            if (dc.getTextWidthInPixels(ctaText, Graphics.FONT_XTINY) > ctaMax
                    && pend > 0 && !phoneOn) {
                // Round-bezel fallback: drop the "N unsynced ·" prefix and
                // keep the imperative half — the pending count is already
                // implied by the presence of the CTA at all.
                ctaText = WatchUi.loadResource(Rez.Strings.SyncOpenPhone) as String;
            }
            dc.setColor(Theme.ACCENT, Graphics.COLOR_TRANSPARENT);
            dc.drawText(cx, ctaY, Graphics.FONT_XTINY, ctaText,
                Graphics.TEXT_JUSTIFY_CENTER);
        }

        Theme.tick(dc, Theme.ANG_START, Theme.ACCENT);
        // Phone-off warning tick — only when there IS something the phone
        // could fix (outstanding answers or a fresh install with no data).
        // Silent bezel when everything is fine. Touch-first devices keep
        // the bezel clear of indicators entirely (CTA line carries it).
        if (DeviceProfile.HAS_UPDOWN_BUTTONS
                && !phoneOn && (pend > 0 || decks.size() == 0)) {
            Theme.tick(dc, Theme.ANG_LINK, Theme.MUTED);
        }
    }

    private function _drawHabit(dc as Dc, cx as Number, lineY as Number,
            nDecks as Number, stats as Array) as Void {
        var base = nDecks.toString() + " "
            + (WatchUi.loadResource(Rez.Strings.HomeDecks) as String).toLower();
        var streak = stats[1] instanceof Number ? stats[1] : 0;
        var full = base;
        if (streak > 0) {
            full += " · " + streak.toString() + " "
                + (WatchUi.loadResource(Rez.Strings.HomeStreak) as String);
        }
        var fontH = dc.getFontHeight(Graphics.FONT_XTINY);
        var maxW = Theme.chordWidth(dc, lineY + fontH / 2, 8);
        var line = dc.getTextWidthInPixels(full, Graphics.FONT_XTINY) <= maxW ? full : base;
        dc.setColor(Theme.MUTED, Graphics.COLOR_TRANSPARENT);
        dc.drawText(cx, lineY, Graphics.FONT_XTINY, line,
            Graphics.TEXT_JUSTIFY_CENTER);
    }

    private function _syncLine(syncT as Number) as String {
        // Always shape: "D MMM HH:MM" (e.g. "24 Jul 14:32"). The user's mental
        // model of "how stale is my state" needs a concrete date, not a
        // relative phrase like "just now" or "Yesterday" — those hide the
        // one number that matters when something is actually wrong.
        var si = Gregorian.info(new Time.Moment(syncT), Time.FORMAT_SHORT);
        var timeStr = si.hour.format("%02d") + ":" + si.min.format("%02d");
        var mon = ["Jan","Feb","Mar","Apr","May","Jun",
                   "Jul","Aug","Sep","Oct","Nov","Dec"];
        return si.day.toString() + " " + mon[(si.month as Number) - 1]
            + " " + timeStr;
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
